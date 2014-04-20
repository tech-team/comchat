package layers.phy;

import gnu.io.NoSuchPortException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;

@Deprecated
public interface IComPort extends IPhysicalLayer {
    boolean isConnected();
    void connect(ComPortSettings settings) throws NoSuchPortException, UnsupportedCommOperationException;
    void disconnect();
    void write(byte[] data) throws IOException;
}
