package layers.apl;

import layers.PDU;

public class Message extends PDU {
    public enum Type { Auth, Msg, Ack, Term, TermAck }

    Type type;
    String msg;

    public Message(Type type, String msg) {
        this.type = type;
        this.msg = msg;
    }
}
