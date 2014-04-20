package layers;

import layers.apl.IApplicationLayer;
import layers.dll.IDataLinkLayer;
import layers.phy.IPhysicalLayer;

public class ProtocolStack {
    IApplicationLayer apl;
    IDataLinkLayer dll;
    IPhysicalLayer phy;

    public ProtocolStack(Class apl, Class dll, Class phy) throws ProtocolStackException {
        try {
            this.apl = (IApplicationLayer) apl.newInstance();
            this.dll = (IDataLinkLayer) dll.newInstance();
            this.phy = (IPhysicalLayer) phy.newInstance();

            this.apl.setLowerLayer(this.dll);

            this.dll.setUpperLayer(this.apl);
            this.dll.setLowerLayer(this.phy);

            this.phy.setUpperLayer(this.dll);

        }
        catch (Exception e) {
            throw new ProtocolStackException(e);
        }
    }

    public IApplicationLayer getApl() {
        return apl;
    }

    public IDataLinkLayer getDll() {
        return dll;
    }

    public IPhysicalLayer getPhy() {
        return phy;
    }
}
