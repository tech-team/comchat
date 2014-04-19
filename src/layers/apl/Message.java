package layers.apl;

public class Message {
    public enum Type { Auth, Msg, Ack, Term, TermAck }

    Type type;
    String msg;

    public Message(byte[] data) {

    }

    public byte[] serialize() {
        return new byte[] {};
    }

    public static Message deserialize(byte[] data) {
        return new Message(null);
    }
}
