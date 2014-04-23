package layers.dll;

import layers.ILayer;
import layers.phy.settings.PhysicalLayerSettings;

import java.io.IOException;

public interface IDataLinkLayer extends ILayer {
    void connect(PhysicalLayerSettings settings) throws Exception;
    void send(byte[] msg) throws IOException;
    void receive(byte[] data) throws Exception;
}
