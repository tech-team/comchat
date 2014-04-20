package layers.phy.settings.comport_settings;

import layers.phy.settings.PhysicalLayerSettings;

/**
 * Created by Igor on 4/20/2014.
 */
public class ComPortSettings extends PhysicalLayerSettings {

    public ComPortSettings(String port, int baudRate, int dataBits, int stopBits, int parity) {
        settings.put(ComSettings.PORT_NAME, port);
        settings.put(ComSettings.BAUD_RATE, baudRate);
        settings.put(ComSettings.DATA_BITS, dataBits);
        settings.put(ComSettings.STOP_BITS, stopBits);
        settings.put(ComSettings.PARITY, parity);
    }

    public String getPort() {
        return (String) settings.get(ComSettings.PORT_NAME);
    }

    public int getBaudRate() {
        return (int) settings.get(ComSettings.BAUD_RATE);
    }

    public int getDataBits() {
        return (int) settings.get(ComSettings.DATA_BITS);
    }

    public int getStopBits() {
        return (int) settings.get(ComSettings.STOP_BITS);
    }

    public int getParity() {
        return (int) settings.get(ComSettings.PARITY);
    }
}
