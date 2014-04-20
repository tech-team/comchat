package layers.phy.settings.comport_settings;

import gnu.io.SerialPort;

/**
* Created by Igor on 4/21/2014.
*/
public enum ParityEnum {
    Even(SerialPort.PARITY_EVEN),
    Mark(SerialPort.PARITY_MARK),
    Odd(SerialPort.PARITY_ODD),
    Space(SerialPort.PARITY_SPACE),
    None(SerialPort.PARITY_NONE);

    private int parity;
    private ParityEnum(int parity) {
        this.parity = parity;
    }

    public int toParity() {
        return this.parity;
    }

    public static ParityEnum getDefault() {
        return ParityEnum.None;
    }

    public static ParityEnum fromString(String str) {
        return valueOf(str);
    }
}
