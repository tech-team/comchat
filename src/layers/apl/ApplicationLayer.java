package layers.apl;

import layers.ILayer;
import layers.SerializationException;
import layers.dll.IDataLinkLayer;
import layers.phy.settings.PhysicalLayerSettings;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class ApplicationLayer implements IApplicationLayer {
    private IDataLinkLayer dll;
    private List<Consumer<Message>> recievers = new LinkedList<>();

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
        getLowerLayer().connect(settings);
    }

    @Override
    public void send(Message.Type type, String msg) throws SerializationException, IOException {
        dll.send(new Message(type, msg).serialize());
    }

    @Override
    public void receive(byte[] data) throws SerializationException {
        Message message = (Message) Message.deserialize(data);

        recievers.forEach(receiver -> receiver.accept(message));
    }

    @Override
    public void subscribeToReceive(final Consumer<Message> receiver) {
        recievers.add(receiver);
    }
}
