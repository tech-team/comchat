package layers.phy;

import layers.ILayer;
import layers.phy.settings.PhysicalLayerSettings;

import java.io.IOException;
import java.util.function.Consumer;

public interface IPhysicalLayer extends ILayer {
    boolean isConnected();
    void connect(PhysicalLayerSettings settings) throws Exception;
    void disconnect();
    public boolean readyToSend();
    void write(byte[] data) throws IOException;
    void subscribeConnectionStatusChanged(Consumer<Boolean> listener);
}
