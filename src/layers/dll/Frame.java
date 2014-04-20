package layers.dll;

import layers.PDU;

public class Frame extends PDU {
    public enum Type { I, S }

    private boolean ACK = false;
    private boolean REJ = false;

    private Type type = Type.I;
    private byte[] msg;

    public Frame(byte[] msg) {
        this.msg = msg;
    }
}
