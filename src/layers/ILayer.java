package layers;

import java.util.function.Consumer;

public interface ILayer {
    ILayer getUpperLayer();
    ILayer getLowerLayer();
    void setUpperLayer(ILayer layer);
    void setLowerLayer(ILayer layer);

    void subscribeOnError(Consumer<Exception> listener);
}
