package layers.phy.settings.comport_settings;

import gnu.io.SerialPort;

public enum DataBitsEnum {
    DataBits5("5 bits", SerialPort.DATABITS_5),
    DataBits6("6 bits", SerialPort.DATABITS_6),
    DataBits7("7 bits", SerialPort.DATABITS_7),
    DataBits8("8 bits", SerialPort.DATABITS_8);

    private String name;
    private int value;

    private DataBitsEnum(String name, int stop_bits) {
        this.name = name;
        this.value = stop_bits;
    }

    @Override
    public String toString() {
        return name;
    }

    public int toDataBits() {
        return value;
    }

    public static DataBitsEnum getDefault() {
        return DataBits8;
    }

    public static DataBitsEnum fromString(String str) {
        for (DataBitsEnum e : DataBitsEnum.values()) {
            if (e.name.equals(str))
                return e;
        }
        throw new IllegalArgumentException("Data bits not found");
    }
}
