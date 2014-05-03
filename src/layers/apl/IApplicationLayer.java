package layers.apl;

import layers.ILayer;

import java.util.function.Consumer;

public interface IApplicationLayer extends ILayer {
    int send(Message.Type type, String msg);
    void receive(byte[] data);
    void subscribeToReceive(final Consumer<Message> receiver);

    void handshakeFinished();
}
