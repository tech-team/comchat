package layers;

public interface ILayer {
    public ILayer getUpperLayer();
    public ILayer getLowerLayer();
    public void setUpperLayer(ILayer layer);
    public void setLowerLayer(ILayer layer);
}
