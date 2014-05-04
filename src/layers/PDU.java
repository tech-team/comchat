package layers;

public interface PDU  {
    byte[] serialize();
    static PDU deserialize(byte[] data) {
        return null;
    }
}
