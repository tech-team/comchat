package layers.dll;

import layers.ILayer;
import layers.phy.settings.ComPortSettings;

import java.util.function.Consumer;

public interface IDataLinkLayer extends ILayer {
    public void connect(ComPortSettings settings);
    public void send(byte[] msg);
    public void receive(byte[] data);
    public void subscribeConnectionStatusChanged(Consumer<Boolean> listener);
    public void notifyConnectionChanged(boolean status);
}
