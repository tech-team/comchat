package layers.dll;

import layers.ILayer;
import layers.apl.IApplicationLayer;
import layers.phy.IPhysicalLayer;
import layers.phy.settings.PhysicalLayerSettings;

public class DataLinkLayer implements IDataLinkLayer {
    private IApplicationLayer apl;
    private IPhysicalLayer phy;

    @Override
    public void connect(PhysicalLayerSettings settings) throws Exception {
        getLowerLayer().connect(settings);
    }

    @Override
    public void send(byte[] msg) {

    }

    @Override
    public void receive(byte[] data) {

    }

    @Override
    public IApplicationLayer getUpperLayer() {
        return apl;
    }

    @Override
    public IPhysicalLayer getLowerLayer() {
        return phy;
    }

    @Override
    public void setUpperLayer(ILayer layer) {
        apl = (IApplicationLayer) layer;
    }

    @Override
    public void setLowerLayer(ILayer layer) {
        phy = (IPhysicalLayer) layer;
    }
}
