package layers.dll;

import layers.ILayer;
import layers.phy.settings.PhysicalLayerSettings;

public interface IDataLinkLayer extends ILayer {
    void connect(PhysicalLayerSettings settings) throws Exception;
    void send(byte[] msg);
    void receive(byte[] data);
}
