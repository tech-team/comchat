package layers.phy;

import layers.ILayer;
import layers.exceptions.ConnectionException;
import layers.phy.settings.PhysicalLayerSettings;

import java.util.function.Consumer;

public interface IPhysicalLayer extends ILayer {
    boolean isConnected();
    void connect(PhysicalLayerSettings settings) throws ConnectionException;
    void disconnect();
    boolean readyToSend();
    void send(byte[] data);
    void subscribeConnectionStatusChanged(Consumer<Boolean> listener);
}
