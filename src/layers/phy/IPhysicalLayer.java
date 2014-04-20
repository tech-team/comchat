package layers.phy;

import layers.ILayer;

import java.io.IOException;
import java.util.Map;

public interface IPhysicalLayer extends ILayer {
    boolean isConnected();
    void connect(Map<String, String> settings) throws Exception;
    void disconnect();
    void write(byte[] data) throws IOException;
}
