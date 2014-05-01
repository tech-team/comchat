package layers.dll;

import layers.ILayer;
import layers.apl.IApplicationLayer;
import layers.exceptions.ConnectionException;
import layers.phy.IPhysicalLayer;
import layers.phy.settings.PhysicalLayerSettings;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataLinkLayer implements IDataLinkLayer {
    private IApplicationLayer apl;
    private IPhysicalLayer phy;

    private Queue<byte[]> queueToSend = new ConcurrentLinkedQueue<>();
    private AtomicBoolean wasACK = new AtomicBoolean(false);


    private Thread sendingThread = new Thread(this::sendingThreadJob);
    private boolean sendingActive = false;

    private void sendingThreadJob() {
        while (sendingActive) {
            if (!queueToSend.isEmpty() && canSend()) {
                // PROTO: sendLastToPhy();
                wasACK.set(false);
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
    public void send(byte[] msg) throws IOException {
        Frame frame = new Frame(Frame.Type.I, msg);
        queueToSend.add(frame.serialize());
    }

    @Override
    public void receive(byte[] data) {
        Frame frame = Frame.deserialize(data);

        if (frame.isACK()) {
            wasACK.set(true);
            if (queueToSend.isEmpty()) {
                //TODO: send error
                return;
            }
            queueToSend.poll();
        }
        else if (frame.isRET()) {
            // PROTO: sendLastToPhy();
        }
        else {
            if (frame.isCorrect()) {
                Frame ack = new Frame(Frame.Type.S, new byte[0]);
                ack.setACK(true);

                apl.receive(frame.getMsg());
            }
            else {
                Frame ret = new Frame(Frame.Type.S, new byte[0]);
                ret.setRET(true);

                // PROTO: getLowerLayer().send(ret.serialize());
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

    private void sendLastToPhy() throws IOException {
        phy.send(queueToSend.peek());
        wasACK.set(false);
    }

    private boolean canSend() {
        return wasACK.get() && getLowerLayer().readyToSend();
    }
}
