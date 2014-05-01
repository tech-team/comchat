package layers.dll;

        import layers.ILayer;
import layers.exceptions.ConnectionException;
import layers.phy.settings.PhysicalLayerSettings;

public interface IDataLinkLayer extends ILayer {
    void connect(PhysicalLayerSettings settings) throws ConnectionException;
    void disconnect();
    void send(byte[] msg);
    void receive(byte[] data);

    void handshakeFinished();
}
