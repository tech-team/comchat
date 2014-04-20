package layers.apl;

import layers.ILayer;
import layers.dll.IDataLinkLayer;
import layers.phy.settings.PhysicalLayerSettings;

import java.io.IOException;

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

    @Override
    public void connect(PhysicalLayerSettings settings) throws Exception {

    }

    @Override
    public void send(Message.Type type, String msg) throws IOException {
        dll.send(new Message(type, msg).serialize());
    }

    @Override
    public void receive(byte[] data) {

    }
}
