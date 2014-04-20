package layers.phy;

import layers.ILayer;
import layers.phy.settings.PhysicalLayerSettings;

import java.io.IOException;

public interface IPhysicalLayer extends ILayer {
    boolean isConnected();
    void connect(PhysicalLayerSettings settings) throws Exception;
    void disconnect();
    void write(byte[] data) throws IOException;
}
