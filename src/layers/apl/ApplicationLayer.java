package layers.apl;

import layers.ILayer;
import layers.dll.IDataLinkLayer;
import layers.exceptions.ConnectionException;
import layers.phy.settings.PhysicalLayerSettings;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class ApplicationLayer implements IApplicationLayer {
    private IDataLinkLayer dll;
    private List<Consumer<Message>> receivers = new LinkedList<>();
    private int messageId = 0;

    private List<Consumer<Exception>> onErrorListeners = new LinkedList<>();

    @Override
    public ILayer getUpperLayer() {
        return null;
    }

    @Override
    public IDataLinkLayer getLowerLayer() {
        return dll;
    }

    @Override
    public void setUpperLayer(ILayer layer) {
    }

    @Override
    public void setLowerLayer(ILayer layer) {
        dll = (IDataLinkLayer) layer;
    }

    @Override
    public void subscribeOnError(Consumer<Exception> listener) {
        onErrorListeners.add(listener);
    }

    @Override
    public void notifyOnError(Exception e) {
        onErrorListeners.forEach(listener -> listener.accept(e));
    }

    @Override
    public void connect(PhysicalLayerSettings settings) throws ConnectionException {
        getLowerLayer().connect(settings);
    }

    @Override
    public void disconnect() {
        getLowerLayer().disconnect();
        messageId = 1;
    }

    @Override
    public boolean isConnected() {
        return getLowerLayer().isConnected();
    }

    @Override
    public int send(Message.Type type, String msg) {
        System.out.println("Sent: " + msg);

        if (type == Message.Type.Msg)
            ++messageId;

        dll.send(new Message(messageId, type, msg).serialize());

        return messageId;
    }

    @Override
    public void receive(byte[] data) {
        Message message = Message.deserialize(data);
        System.out.println("Received: " + message.getMsg());
        receivers.forEach(receiver -> receiver.accept(message));
    }

    @Override
    public void subscribeToReceive(final Consumer<Message> receiver) {
        receivers.add(receiver);
    }

    @Override
    public void handshakeFinished() {
        getLowerLayer().handshakeFinished();
    }
}
