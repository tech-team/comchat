package layers.apl;

import layers.PDU;
import util.ArrayUtils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Message implements PDU {

    public enum Type {
        Auth, Msg, Ack;

        public static Type fromInteger(int x) throws Exception {
            int max = Type.values().length;
            if (x < 0 || x >= max)
                throw new Exception("bad range! x was: " + x);

            return Type.values()[x];
        }
    }

    private int id;
    private Type type;
    private String msg;

    public Message(int id, Type type, String msg) {
        this.id = id;
        this.type = type;
        this.msg = msg;
    }

    public int getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public byte[] serialize() {
        byte typeByte = (byte) type.ordinal();
        byte[] idBytes = ByteBuffer.allocate(4).putInt(this.id).array();
        byte[] infoBytes = ArrayUtils.concatenate(typeByte, idBytes);

        byte[] msgBytes = new byte[0];
        try {
            msgBytes = msg.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ignored) {
            System.err.println(ignored.getMessage());
        }
        return ArrayUtils.concatenate(infoBytes, msgBytes);
    }

    public static Message deserialize(byte[] data)  {
        byte typeByte = data[0];
        byte[] idBytes = Arrays.copyOfRange(data, 1, 5);
        byte[] msgBytes = Arrays.copyOfRange(data, 5, data.length);

        Type type = null;
        try {
            type = Type.fromInteger((int) typeByte);
        } catch (Exception e) {
            e.printStackTrace(); // TODO: review
        }

        int id = ByteBuffer.wrap(idBytes).getInt();

        String msg = null;
        try {
            msg = new String(msgBytes, "UTF-8");
        } catch (UnsupportedEncodingException ignored) { }

        return new Message(id, type, msg);
    }
}
