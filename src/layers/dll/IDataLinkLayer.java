package layers.dll;

import layers.ILayer;
import layers.phy.settings.ComPortSettings;

public interface IDataLinkLayer extends ILayer {
    public void connect(ComPortSettings settings);
    public void send(byte[] msg);
    public void receive(byte[] data);
}
