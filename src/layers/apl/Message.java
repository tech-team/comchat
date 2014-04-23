package layers.apl;

import layers.PDU;
import util.ArrayUtils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class Message extends PDU {
    public enum Type {
        Auth, Msg, Ack, Term, TermAck;

        public static Type fromInteger(int x) throws Exception {
            int max = Type.values().length;
            if (x < 0 || x >= max)
                throw new Exception("bad range!");

            return Type.values()[x];
        }
    }

    private Type type;
    private String msg;

    public Message(Type type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    public Type getType() {
        return type;
    }

    public String getMsg() {
        return msg;
    }


    public byte[] serialize() {
        byte typeByte = (byte) type.ordinal();
        byte[] msgBytes = new byte[0];
        try {
            msgBytes = msg.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ignored) {
            System.err.println(ignored.getMessage());
        }
        return ArrayUtils.concatenate(typeByte, msgBytes);
    }

    public static Message deserialize(byte[] data)  {
        byte typeByte = data[0];
        byte[] msgBytes = Arrays.copyOfRange(data, 1, data.length);

        Type type = null;
        try {
            type = Type.fromInteger((int) typeByte);
        } catch (Exception e) {
            e.printStackTrace(); // TODO: review
        }
        String msg = null;
        try {
            msg = new String(msgBytes, "UTF-8");
        } catch (UnsupportedEncodingException ignored) { }

        return new Message(type, msg);
    }
}
