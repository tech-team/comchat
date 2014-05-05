package layers.dll;

import layers.PDU;
import layers.exceptions.DecodeException;
import util.ArrayUtils;

import java.util.Arrays;

public class Frame implements PDU {
    public enum Type {
        I, S;

        public static Type fromInteger(int x) throws Exception {
            int max = Type.values().length;
            if (x < 0 || x >= max)
                throw new Exception("bad range! x was: " + x);

            return Type.values()[x];
        }
    }

    public static final int MIN_SIZE = 2;
    public static final int MAX_SIZE = 128;
    public static final int MAX_MSG_SIZE = MAX_SIZE - MIN_SIZE;


    private boolean ACK = false;
    private boolean RET = false;
    private boolean CHUNKS = false;
    private boolean END_CHUNKS = false;

    public static final byte START_BYTE = (byte) 0xFF;
    public static final byte STOP_BYTE = (byte) 0xFF;


    private Type type;
    private byte[] msg;

    private boolean correct = true;
    private static CycleCoder cc = new CycleCoder();

    private Frame(Type type, byte[] msg) {
        this.type = type;
        this.msg = msg;
    }

    public static Frame newACKFrame() {
        Frame frame = new Frame(Type.S, new byte[0]);
        frame.setACK(true);
        return frame;
    }

    public static Frame newRETFrame() {
        Frame frame = new Frame(Type.S, new byte[0]);
        frame.setRET(true);
        return frame;
    }

    public static Frame newCHUNKEDFrame(byte[] chunk) {
        Frame frame = new Frame(Type.I, chunk);
        frame.setCHUNKS(true);
        return frame;
    }

    public static Frame newChunkEndFrame(byte[] chunk) {
        Frame frame = new Frame(Type.I, chunk);
        frame.setCHUNKS(true);
        frame.setEND_CHUNKS(true);
        return frame;
    }


    public Type getType() {
        return type;
    }

    public byte[] getMsg() {
        return msg;
    }

    public boolean isACK() {
        return ACK;
    }

    public boolean isRET() {
        return RET;
    }

    public boolean isCHUNK() {
        return CHUNKS;
    }

    public boolean isEND_CHUNKS() {
        return END_CHUNKS;
    }


    private void setACK(boolean value) {
        ACK = value;
    }

    private void setRET(boolean value) {
        RET = value;
    }

    private void setCHUNKS(boolean value) {
        CHUNKS = value;
    }

    private void setEND_CHUNKS(boolean value) {
        END_CHUNKS = value;
    }


    private byte getACK() {
        return (byte) (ACK ? 1 : 0);
    }

    private byte getRET() {
        return (byte) (RET ? 1 : 0);
    }

    private byte getCHUNKS() {
        return (byte) (CHUNKS ? 1 : 0);
    }

    private byte getEND_CHUNKS() {
        return (byte) (END_CHUNKS ? 1 : 0);
    }

    public boolean isCorrect() {
        return correct;
    }

    private void setCorrect(boolean correct) {
        this.correct = correct;
    }

    @Override
    public byte[] serialize() {
        byte typeByte = (byte) type.ordinal();

        byte supervisorInfoByte = getACK();
        supervisorInfoByte <<= 1;
        supervisorInfoByte += getRET();
        supervisorInfoByte <<= 1;
        supervisorInfoByte += getCHUNKS();
        supervisorInfoByte <<= 1;
        supervisorInfoByte += getEND_CHUNKS();


        byte[] infoBytes = new byte[2];
        infoBytes[0] = typeByte;
        infoBytes[1] = supervisorInfoByte;

        byte[] frame = ArrayUtils.concatenate(infoBytes, msg);

        byte[] encodedFrame = cc.encode(frame);

        byte[] withStart = ArrayUtils.concatenate(START_BYTE, encodedFrame);
        return ArrayUtils.concatenate(withStart, STOP_BYTE);
    }

    public static Frame deserialize(byte[] data) {
        try {
            data = cc.decode(data);
        } catch (DecodeException e) {
            Type type = data.length == MIN_SIZE ? Type.S : Type.I;
            Frame frame = new Frame(type, null);
            frame.setCorrect(false);
            return frame;
        }

        byte typeByte = data[0];
        Type type = null;
        try {
            type = Type.fromInteger(typeByte);
        } catch (Exception ignored) {}

        byte supervisorInfoByte = data[1];
        byte mask = 0x01;
        byte END_CHUNKS = (byte) (supervisorInfoByte & mask);
        supervisorInfoByte >>= 1;
        byte CHUNKS = (byte) (supervisorInfoByte & mask);
        supervisorInfoByte >>= 1;
        byte RET = (byte) (supervisorInfoByte & mask);
        supervisorInfoByte >>= 1;
        byte ACK = (byte) (supervisorInfoByte & mask);

        byte[] msg = Arrays.copyOfRange(data, 2, data.length);

        Frame frame = new Frame(type, msg);
        frame.setACK(ACK != 0);
        frame.setRET(RET != 0);
        frame.setCHUNKS(CHUNKS != 0);
        frame.setEND_CHUNKS(END_CHUNKS != 0);

        return frame;
    }
}
