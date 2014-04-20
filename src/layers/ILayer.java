package layers;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public interface ILayer {
    public default ILayer getUpperLayer() {
        return null;
    }

    public default ILayer getLowerLayer() {
        return null;
    }

    public default void setUpperLayer(ILayer layer) {
        throw new NotImplementedException();
    }

    public default void setLowerLayer(ILayer layer) {
        throw new NotImplementedException();
    }
}
