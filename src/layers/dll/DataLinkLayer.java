package layers.dll;

import layers.ILayer;
import layers.apl.IApplicationLayer;
import layers.exceptions.ConnectionException;
import layers.exceptions.UnexpectedChatException;
import layers.phy.IPhysicalLayer;
import layers.phy.settings.PhysicalLayerSettings;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class DataLinkLayer implements IDataLinkLayer {
    private IApplicationLayer apl;
    private IPhysicalLayer phy;

    private Queue<byte[]> queueToSend = new ConcurrentLinkedQueue<>();
    private AtomicBoolean wasACK = new AtomicBoolean(true);


    private Thread sendingThread = new Thread(this::sendingThreadJob);
    private boolean sendingActive = false;

    private List<Consumer<Exception>> onErrorListeners = new LinkedList<>();

    private void sendingThreadJob() {
        while (sendingActive) {
            if (!queueToSend.isEmpty() && canSend()) {
                sendLastToPhy();
                wasACK.set(false);
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
                sendingActive = false;
            }
        }
    }

    @Override
    public void connect(PhysicalLayerSettings settings) throws ConnectionException {
        getLowerLayer().connect(settings);
        sendingActive = true;
        sendingThread.start();
    }

    @Override
    public void disconnect() {
        sendingActive = false;
        queueToSend.clear();
        wasACK.set(true);
        getLowerLayer().disconnect();
    }

    @Override
    public void send(byte[] data) {
        Frame frame = new Frame(Frame.Type.I, data);
        queueToSend.add(frame.serialize());
    }

    @Override
    public void receive(byte[] data) {
        Frame frame = Frame.deserialize(data);

        if (frame.isACK()) {
            if (queueToSend.isEmpty()) {
                notifyOnError(new UnexpectedChatException("Frame queue is empty"));
                return;
            }
            queueToSend.poll();
            wasACK.set(true);
            System.out.println("Received ACK");
        }
        else if (frame.isRET()) {
            sendLastToPhy();
        }
        else {
            if (frame.isCorrect()) {
                Frame ack = new Frame(Frame.Type.S, new byte[0]);
                ack.setACK(true);
                getLowerLayer().send(ack.serialize());

                apl.receive(frame.getMsg());
            }
            else {
                Frame ret = new Frame(Frame.Type.S, new byte[0]);
                ret.setRET(true);
                getLowerLayer().send(ret.serialize());
            }
        }
    }

    @Override
    public IApplicationLayer getUpperLayer() {
        return apl;
    }

    @Override
    public IPhysicalLayer getLowerLayer() {
        return phy;
    }

    @Override
    public void setUpperLayer(ILayer layer) {
        apl = (IApplicationLayer) layer;
    }

    @Override
    public void setLowerLayer(ILayer layer) {
        phy = (IPhysicalLayer) layer;
    }

    @Override
    public void subscribeOnError(Consumer<Exception> listener) {
        onErrorListeners.add(listener);
    }

    private void notifyOnError(Exception e) {
        onErrorListeners.forEach(listener -> listener.accept(e));
    }

    private void sendLastToPhy() {
        phy.send(queueToSend.peek());
        wasACK.set(false);
    }

    private boolean canSend() {
        return wasACK.get() && getLowerLayer().readyToSend();
    }
}
