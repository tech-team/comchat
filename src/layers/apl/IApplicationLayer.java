package layers.apl;

import layers.ILayer;
import layers.phy.SerialzationException;
import layers.phy.settings.PhysicalLayerSettings;

import java.io.IOException;
import java.util.function.Consumer;

public interface IApplicationLayer extends ILayer {
    void connect(PhysicalLayerSettings settings) throws Exception;
    void send(Message.Type type, String msg) throws IOException, SerialzationException;
    void receive(byte[] data) throws SerialzationException;
    void subscribeToReceive(final Consumer<Message> receiver);
}
