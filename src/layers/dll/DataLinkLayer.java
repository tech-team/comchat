package layers.dll;

import layers.ILayer;
import layers.apl.IApplicationLayer;
import layers.exceptions.ConnectionException;
import layers.exceptions.LayerUnavailableException;
import layers.exceptions.UnexpectedChatException;
import layers.phy.IPhysicalLayer;
import layers.phy.settings.PhysicalLayerSettings;
import util.ArrayUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataLinkLayer implements IDataLinkLayer {
    private IApplicationLayer apl;
    private IPhysicalLayer phy;

    private Queue<byte[]> framesToSend = new ConcurrentLinkedQueue<>();
    private Queue<byte[]> systemFramesToSend = new ConcurrentLinkedQueue<>();

    private List<byte[]> receivedChunkMessages = new LinkedList<>();


    private boolean remoteUserConnected = false;
    private AtomicBoolean wasACK = new AtomicBoolean(true);
    private AtomicBoolean forceSending = new AtomicBoolean(false);


    private Thread sendingThread;
    private boolean sendingActive = false;
    private static final int SENDING_DELAY = 100;
    private static final int SENDING_TIMEOUT = 3000;
    private static final int ACCESSING_PHY_TIMEOUT = 5000;


    private int getSendingCycles() {
        return SENDING_TIMEOUT / SENDING_DELAY;
    }

    private int getPhyAccessingCycles() {
        return ACCESSING_PHY_TIMEOUT / SENDING_DELAY;
    }

    private void sendingThreadJob() {
        int sendingCycles = getSendingCycles();
        int accessingCycles = getPhyAccessingCycles();

        while (sendingActive) {

            if (systemFramesToSend.isEmpty() && framesToSend.isEmpty()) {
                sendingCycles = getSendingCycles();
                accessingCycles = getPhyAccessingCycles();
            }

            if (!systemFramesToSend.isEmpty()) {

                if (getLowerLayer().readyToSend()) {
                    accessingCycles = getPhyAccessingCycles();
                    getLowerLayer().send(systemFramesToSend.poll());
                }
                else { // phy is unavailable
                    accessingCycles -= 1;
                }

            }
            else {

                if (!framesToSend.isEmpty()) {
                    if (getLowerLayer().readyToSend()) {
                        accessingCycles = getPhyAccessingCycles();
                        if (forceSending.get() || wasACK.get()) { // if we are permitted to send next frame
                            sendingCycles = getSendingCycles();

                            getLowerLayer().send(framesToSend.peek());
                            wasACK.set(false);
                            forceSending.set(false);
                        }
                        else {
                            sendingCycles -= 1;
                        }

                        if (sendingCycles <= 0) {
                            forceSend();
                            sendingCycles = getSendingCycles();
                        }
                    }
                    else { // phy is unavailable
                        accessingCycles -= 1;
                    }
                }
            }

            if (accessingCycles <= 0 && remoteUserConnected) {
                System.out.println("onError");
                notifyOnError(new LayerUnavailableException("Physical layer was unavailable for " + ACCESSING_PHY_TIMEOUT + "ms"));
                systemFramesToSend.clear();
                framesToSend.clear();
            }

            try {
                Thread.sleep(SENDING_DELAY);
            } catch (InterruptedException ignored) {
                sendingActive = false;
            }

        }
    }

    @Override
    public void connect(PhysicalLayerSettings settings) throws ConnectionException {
        getLowerLayer().connect(settings);
        sendingThread = new Thread(this::sendingThreadJob);
        sendingActive = true;
        sendingThread.start();
    }

    @Override
    public void disconnect() {
        sendingActive = false;
        framesToSend.clear();
        systemFramesToSend.clear();
        wasACK.set(true);
        getLowerLayer().disconnect();
    }

    @Override
    public boolean isConnected() {
        return getLowerLayer().isConnected();
    }

    @Override
    public void send(byte[] data) {
        int chunks = (int) Math.ceil((double) data.length / (double) Frame.MAX_MSG_SIZE);
        for (int i = 0; i < chunks; ++i) {
            int minIndex = Frame.MAX_MSG_SIZE * i;
            int maxSize = Math.min(Frame.MAX_MSG_SIZE, data.length - minIndex);
            byte[] chunk = Arrays.copyOfRange(data, minIndex, minIndex + maxSize);
            Frame frameChunk;
            if (i < chunks - 1) {
                frameChunk = Frame.newCHUNKEDFrame(chunk);
            }
            else {
                frameChunk = Frame.newChunkEndFrame(chunk);
            }
            framesToSend.add(frameChunk.serialize());
        }
    }

    @Override
    public void receive(byte[] data) {
        Frame frame = Frame.deserialize(data);
//        System.out.println("isCorrect: " + frame.isCorrect());

        if (frame.getType() != Frame.Type.I && !frame.isCorrect())
            return; // throw away this frame

        switch (frame.getType()) {
            case S:
                if (frame.isACK()) {
                    if (framesToSend.isEmpty()) {
                        notifyOnError(new UnexpectedChatException("Frame queue is empty"));
                        return;
                    }
                    framesToSend.poll();
                    wasACK.set(true);
                }
                else if (frame.isRET()) {
                    forceSend();
                }
                break;
            case I:
                if (frame.isCorrect()) {
                    Frame ack = Frame.newACKFrame();
                    systemFramesToSend.add(ack.serialize());


                    receivedChunkMessages.add(frame.getMsg());
                    if (frame.isEND_CHUNKS()) {
                        byte[] resultedMsg = new byte[0];
                        for (byte[] chunk : receivedChunkMessages) {
                            resultedMsg = ArrayUtils.concatenate(resultedMsg, chunk);
                        }
                        receivedChunkMessages.clear();
                        apl.receive(resultedMsg);
                    }
                }
                else {
                    Frame ret = Frame.newRETFrame();
                    systemFramesToSend.add(ret.serialize());
                }
                break;
        }
    }

    private void forceSend() {
        forceSending.set(true);
    }

    @Override
    public void handshakeFinished() {
        remoteUserConnected = true;
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
        phy.subscribeCompanionConnectedChanged(status -> remoteUserConnected = status);
    }

    @Override
    public synchronized void notifyOnError(Exception e) {
        getUpperLayer().notifyOnError(e);
    }
}
