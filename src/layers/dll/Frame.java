package layers.dll;

import layers.PDU;
import sun.security.util.BitArray;
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

    public static BitSet fromByteArray(byte[] bytes) {
        BitSet bits = new BitSet();
        for (int i=0; i<bytes.length*8; i++) {
            if ((bytes[bytes.length-i/8-1]&(1<<(i%8))) > 0) {
                bits.set(i);
            }
        }
        return bits;
    }

    public BitSet division(BitSet x)
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

    public byte[] cyclicEncode(byte[] frame)
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

    public boolean check(BitSet x)
    {
        BitSet res = division(x);
        for (int i = 4; i < 7; ++i)
        {
            if((res.get(i)) == true) return false;
        }
        return true;
    }

    public byte[] cyclicDecode(byte[] frame)
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
