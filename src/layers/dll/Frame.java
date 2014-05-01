package layers.dll;

import layers.PDU;
import util.ArrayUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;

public class Frame extends PDU {
    public enum Type {
        I, S;

        public static Type fromInteger(int x) throws Exception {
            int max = Type.values().length;
            if (x < 0 || x >= max)
                throw new Exception("bad range! x was: " + x);

            return Type.values()[x];
        }
    }

    public static final int MAX_SIZE = 128;
    public static final int MAX_MSG_SIZE = MAX_SIZE - 2;


    private boolean ACK = false;
    private boolean RET = false;
    private boolean CHUNKS = false;
    private boolean END_CHUNKS = false;



    private Type type;
    private byte[] msg;

    private Frame(Type type, byte[] msg) {
        this.type = type;
        this.msg = msg;
    }

    public Frame(byte[] msg) {
        this.type = Type.I;
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
        byte[] size = ByteBuffer.allocate(2).putShort((short) frame.length).array();
        return ArrayUtils.concatenate(size, frame);
    }

    public static Frame deserialize(byte[] data) {
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



    public boolean isCorrect() {
        return true; // TODO
    }

    private static BitSet fromByteArray(byte[] bytes) {
        BitSet bits = new BitSet();
        for (int i=0; i<bytes.length*8; i++) {
            if ((bytes[bytes.length-i/8-1]&(1<<(i%8))) > 0) {
                bits.set(i);
            }
        }
        return bits;
    }

    private BitSet division(BitSet x)
    {
        BitSet res = new BitSet(7);
        for (int i=0; i < 4; ++i)
        {
            res.set(i, x.get(i));
        }

        BitSet divisor = new BitSet(4);
        for(int i=0;i < 4; ++i)
        {
            divisor.set(i,true);
        }
        divisor.set(1,false);

        for (int i = 0; i < x.length(); ++i) {
            if (x.get(i)) {
                for (int j = 0; j <4; ++j) {
                    res.set(i+j, x.get(i+j) ^ divisor.get(j));
                }
            }
        }

        for (int i=0; i < 3; ++i)
        {
            res.set(4+i,x.get(1+i));
        }
        return res;
    }

    private byte[] cyclicEncode(byte[] frame)
    {

        BitSet input = fromByteArray(frame);
        BitSet output = new BitSet(14 * frame.length);
        BitSet inputToEncode = new BitSet(4);
        for (int i = 0; i < input.length(); ++i) {
            for (int k = 0; k < 4; ++k) {
                inputToEncode.set(k, input.get(i));
                ++i;
            }
            BitSet outputBits = division(inputToEncode);
            for (int m = 0; m < 7; ++m) {
                output.set(i + m, outputBits.get(m));
            }

        }
        return output.toByteArray();
    }

    private boolean check(BitSet x)
    {
        BitSet res = division(x);
        for (int i = 4; i < 7; ++i)
        {
            if((res.get(i)) == true) return false;
        }
        return true;
    }

    private byte[] cyclicDecode(byte[] frame)
    {
        BitSet output = fromByteArray(frame);
        BitSet input = new BitSet(8 * frame.length);
        BitSet outputToDecode = new BitSet(7);
        for ( int i = 0; i < output.length(); ++i) {
            for ( int j = 0; j < 7; ++j)
            {
                outputToDecode.set(j,output.get(i));
                ++i;
            }
            if(check(outputToDecode)){
                for (int m = 0; m < 4; ++i)
                {
                    input.set(i+m,outputToDecode.get(m));
                }
            }

        }
        return input.toByteArray();

    }




    public static void main(String[] args) {
        byte[] bytes = {1};
        Frame frame = new Frame(bytes);
        Frame temp = frame;
        byte[] encoded = frame.cyclicEncode(bytes);
        System.out.println("Encoded:");
        for (byte anEncoded : encoded) System.out.print(anEncoded + " ");

        byte[] decoded = frame.cyclicDecode(encoded);

        System.out.println("Decoded:");
        for (byte anDecoded : decoded) System.out.print(anDecoded + " ");

    }





}
