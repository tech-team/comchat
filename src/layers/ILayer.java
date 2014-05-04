package layers;

import layers.exceptions.ConnectionException;
import layers.phy.settings.PhysicalLayerSettings;

public interface ILayer {
    ILayer getUpperLayer();
    ILayer getLowerLayer();
    void setUpperLayer(ILayer layer);
    void setLowerLayer(ILayer layer);

    void connect(PhysicalLayerSettings settings) throws ConnectionException;
    void disconnect();
    boolean isConnected();

    void notifyOnError(Exception e);
}
