package layers.apl;

import layers.ILayer;
import layers.phy.settings.PhysicalLayerSettings;

import java.io.IOException;

public interface IApplicationLayer extends ILayer {
    void connect(PhysicalLayerSettings settings) throws Exception;
    void send(Message.Type type, String msg) throws IOException;
    void receive(byte[] data);
}
