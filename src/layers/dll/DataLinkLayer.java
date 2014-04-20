package layers.dll;

import layers.phy.ComPortSettings;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class DataLinkLayer implements IDataLinkLayer {
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
}
