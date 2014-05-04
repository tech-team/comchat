package layers.phy.settings.comport_settings;

import layers.phy.settings.PhySettings;

public enum ComSettings implements PhySettings {
    PORT_NAME,
    BAUD_RATE,
    DATA_BITS,
    STOP_BITS,
    PARITY,
    END_BYTE,
    ESC_BYTE,
    ESC_ESC_BYTE
}
