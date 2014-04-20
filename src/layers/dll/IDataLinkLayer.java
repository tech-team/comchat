package layers.dll;

import layers.phy.ComPortSettings;

import java.util.function.Consumer;

/**
 * Created by Igor on 4/20/2014.
 */
public interface IDataLinkLayer {
    public void connect(ComPortSettings settings);
    public void send(byte[] msg);
    public void receive(byte[] data);
    public void subscribeConnectionStatusChanged(Consumer<Boolean> listener);
    public void notifyConnectionChanged(boolean status);
}
