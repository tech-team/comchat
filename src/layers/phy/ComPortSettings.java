package layers.phy;

/**
 * Created by Igor on 4/20/2014.
 */
public class ComPortSettings {
    private String port;
    private int baudRate;
    private int dataBits;
    private int stopBits;
    private int parity;

    public ComPortSettings(String port, int baudRate, int dataBits, int stopBits, int parity) {
        this.port = port;
        this.baudRate = baudRate;
        this.dataBits = dataBits;
        this.stopBits = stopBits;
        this.parity = parity;
    }

    public String getPort() {
        return port;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public int getDataBits() {
        return dataBits;
    }

    public int getStopBits() {
        return stopBits;
    }

    public int getParity() {
        return parity;
    }
}
