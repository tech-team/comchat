package layers.phy;

import gnu.io.NoSuchPortException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;

/**
 * Created by Igor on 4/20/2014.
 */
public interface IComPort {
    boolean isConnected();
    void connect(ComPortSettings settings) throws NoSuchPortException, UnsupportedCommOperationException;
    void disconnect();
    void write(byte[] data) throws IOException;
}
