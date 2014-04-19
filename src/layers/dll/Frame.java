package layers.dll;

public class Frame {
    public enum Type { I, S }

    private boolean ACK = false;
    private boolean REJ = false;

    private Type type = Type.I;
    private byte[] msg;

    public Frame(byte[] msg) {
        this.msg = msg;
    }

    public byte[] serialize() {
        //todo
        return new byte[] {};
    }

    public static Frame deserialize(byte[] data) {
        return new Frame(null);
    }
}
