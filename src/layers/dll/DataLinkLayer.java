package layers.dll;

import layers.ILayer;
import layers.apl.IApplicationLayer;
import layers.phy.settings.ComPortSettings;
import layers.phy.IPhysicalLayer;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class DataLinkLayer implements IDataLinkLayer {
    IApplicationLayer apl;
    IPhysicalLayer phy;

    private List<Consumer<Boolean>> connectionChangedListeners = new LinkedList<>();

    @Override
    public void connect(ComPortSettings settings) {

    }

    @Override
    public void send(byte[] msg) {

    }

    @Override
    public void receive(byte[] data) {

    }

    @Override
    public void subscribeConnectionStatusChanged(Consumer<Boolean> listener) {
        connectionChangedListeners.add(listener);
    }

    @Override
    public void notifyConnectionChanged(boolean status) {
        connectionChangedListeners.forEach(listener -> listener.accept(status));
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
