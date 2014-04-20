package layers.apl;

import layers.ILayer;
import layers.dll.IDataLinkLayer;

public class ApplicationLayer implements IApplicationLayer {
    IDataLinkLayer dll;

    @Override
    public ILayer getUpperLayer() {
        return null;
    }

    @Override
    public IDataLinkLayer getLowerLayer() {
        return dll;
    }

    @Override
    public void setUpperLayer(ILayer layer) {

    }

    @Override
    public void setLowerLayer(ILayer layer) {
        dll = (IDataLinkLayer) layer;
    }
}
