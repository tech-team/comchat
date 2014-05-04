package layers.phy;

import layers.ILayer;

import java.util.function.Consumer;

public interface IPhysicalLayer extends ILayer {
    boolean readyToSend();
    void send(byte[] data);
    void subscribeConnectionStatusChanged(Consumer<Boolean> listener);
    void subscribeCompanionConnectedChanged(Consumer<Boolean> listener);
    void subscribeSendingAvailableChanged(Consumer<Boolean> listener);
}
