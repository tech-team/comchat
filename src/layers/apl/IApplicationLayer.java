package layers.apl;

import layers.ILayer;
import layers.exceptions.ConnectionException;
import layers.phy.settings.PhysicalLayerSettings;

import java.util.function.Consumer;

public interface IApplicationLayer extends ILayer {
    void connect(PhysicalLayerSettings settings) throws ConnectionException;
    void disconnect();
    void send(Message.Type type, String msg);
    void receive(byte[] data);
    void subscribeToReceive(final Consumer<Message> receiver);
}
