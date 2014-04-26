package layers.dll;

import layers.ILayer;
import layers.apl.IApplicationLayer;
import layers.phy.IPhysicalLayer;
import layers.phy.settings.PhysicalLayerSettings;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DataLinkLayer implements IDataLinkLayer {
    private IApplicationLayer apl;
    private IPhysicalLayer phy;

    @Override
    public void connect(PhysicalLayerSettings settings) throws Exception {
        getLowerLayer().connect(settings);
    }

    @Override
    public void send(byte[] msg) throws IOException {
        Queue<byte[]> messageHandler = new ConcurrentLinkedQueue<>();
        Frame frame = new Frame(Frame.Type.I, msg);
        messageHandler.add(frame.serialize());
        phy.send(messageHandler.element());
        messageHandler.remove();// if ACK==1
    }

    @Override
    public void receive(byte[] data) {
        Queue<Frame> dataHandler = new ConcurrentLinkedQueue<>();
        Frame frame = Frame.deserialize(data);
        dataHandler.add(frame);
        apl.receive(frame.getMsg());
        dataHandler.remove();
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
