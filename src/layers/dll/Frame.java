package layers.dll;

import layers.PDU;
import util.ArrayUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Frame extends PDU {
    public enum Type {
        I, S;

        public static Type fromInteger(int x) throws Exception {
            int max = Type.values().length;
            if (x < 0 || x >= max)
                throw new Exception("bad range!");

            return Type.values()[x];
        }
    }

    private byte ACK = 0;
    private byte RET = 0;


    private Type type;
    private byte[] msg;

    public Frame(Type type, byte[] msg) {
        this.type = type;
        this.msg = msg;
    }

    public Frame(byte[] msg) {
        this.type = Type.I;
        this.msg = msg;
    }

    public Type getType() {
        return type;
    }

    public byte[] getMsg() {
        return msg;
    }

    public void setACK(byte value) {
        ACK = (byte) (value == 0 ? 0 : 1);
    }

    public void setRET(byte value) {
        RET = (byte) (value == 0 ? 0 : 1);
    }


    public byte[] serialize() {
        byte typeByte = (byte) type.ordinal();

        byte supervisorInfoByte = ACK;
        supervisorInfoByte <<= 1;
        supervisorInfoByte += RET;

        byte[] infoBytes = new byte[2];
        infoBytes[0] = typeByte;
        infoBytes[1] = supervisorInfoByte;

        byte[] frame = ArrayUtils.concatenate(infoBytes, msg);
        byte[] size = ByteBuffer.allocate(4).putInt(frame.length).array();
        return ArrayUtils.concatenate(size, frame);
    }

    public static Frame deserialize(byte[] data) {
        byte typeByte = data[0];
        Type type = null;
        try {
            type = Type.fromInteger(typeByte);
        } catch (Exception e) {
            e.printStackTrace(); // TODO: review
        }

        byte supervisorInfoByte = data[1];
        byte mask = 0x01;
        byte RET = (byte) (supervisorInfoByte & mask);
        supervisorInfoByte >>= 1;
        byte ACK = (byte) (supervisorInfoByte & mask);

        byte[] msg = Arrays.copyOfRange(data, 2, data.length);

        Frame frame = new Frame(type, msg);
        frame.setACK(ACK);
        frame.setRET(RET);

        return frame;
    }
}
