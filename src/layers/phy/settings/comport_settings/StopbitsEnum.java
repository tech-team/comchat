package layers.phy.settings.comport_settings;

import gnu.io.SerialPort;

/**
 * Created by Igor on 4/21/2014.
 */
public enum StopBitsEnum {
    StopBits1("1", SerialPort.STOPBITS_1),
    StopBits1_5("1.5", SerialPort.STOPBITS_1_5),
    StopBits2("2", SerialPort.STOPBITS_2);

    private String name;
    private int value;

    private StopBitsEnum(String name, int stop_bits) {
        this.name = name;
        this.value = stop_bits;
    }

    @Override
    public String toString() {
        return name;
    }

    public int toStopBits() {
        return value;
    }

    public static StopBitsEnum getDefault() {
        return StopBits1;
    }

    public static StopBitsEnum fromString(String str) {
        for (StopBitsEnum e : StopBitsEnum.values()) {
            if (e.name.equals(str))
                return e;
        }
        throw new IllegalArgumentException("Stop bits not found");
    }
}
