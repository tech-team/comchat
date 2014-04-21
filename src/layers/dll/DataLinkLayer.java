package layers.dll;

import layers.ILayer;
import layers.SerializationException;
import layers.apl.IApplicationLayer;
import layers.phy.IPhysicalLayer;
import layers.phy.settings.PhysicalLayerSettings;

import java.io.IOException;

public class DataLinkLayer implements IDataLinkLayer {
    private IApplicationLayer apl;
    private IPhysicalLayer phy;

    @Override
    public void connect(PhysicalLayerSettings settings) throws Exception {
        getLowerLayer().connect(settings);
    }

    @Override
    public void send(byte[] msg) throws IOException {
        //TODO
        phy.send(msg);
    }

    @Override
    public void receive(byte[] data) throws SerializationException {
        apl.receive(data);
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
