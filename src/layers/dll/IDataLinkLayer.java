package layers.dll;

import layers.ILayer;
import layers.exceptions.ConnectionException;
import layers.phy.settings.PhysicalLayerSettings;

import java.io.IOException;

public interface IDataLinkLayer extends ILayer {
    void connect(PhysicalLayerSettings settings) throws ConnectionException;
    void disconnect();
    void send(byte[] msg) throws IOException;
    void receive(byte[] data);
}
