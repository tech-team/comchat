package layers.apl;

import layers.ILayer;
import layers.dll.IDataLinkLayer;

public class ApplicationLayer implements IApplicationLayer {
    IDataLinkLayer dll;

    @Override
    public IDataLinkLayer getLowerLayer() {
        return dll;
    }

    @Override
    public void setLowerLayer(ILayer layer) {
        dll = (IDataLinkLayer) layer;
    }
}
