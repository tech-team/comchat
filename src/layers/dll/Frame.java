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

    public boolean isACK() {
        return ACK == 1;
    }

    public boolean isRET() {
        return RET == 1;
    }


    public void setACK(byte value) {
        ACK = (byte) (value == 0 ? 0 : 1);
    }

    public void setACK(boolean value) {
        ACK = (byte) (value ? 1 : 0);
    }

    public void setRET(byte value) {
        RET = (byte) (value == 0 ? 0 : 1);
    }

    public void setRET(boolean value) {
        RET = (byte) (value ? 1 : 0);
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
        byte[] size = ByteBuffer.allocate(2).putShort((short) frame.length).array();
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


    public boolean isCorrect() {
        return true; // TODO
    }

/*    private static BitSet division(BitSet x) {
        BitSet res = new BitSet(7);
        for (int i = 0; i < 4; ++i) {
            res.set(i, x.get(i));
        }

        BitSet divisor = new BitSet(4);
        for (int i = 0; i < 4; ++i) {
            divisor.set(i, true);
        }
        divisor.set(1, false);

        for (int i = 0; i < x.length(); ++i) {
            if (x.get(i)) {
                for (int j = 0; j < 4; ++j) {
                    res.set(i + j, x.get(i + j) ^ divisor.get(j));
                }
            }
        }

        for (int i = 0; i < 3; ++i) {
            res.set(4 + i, x.get(1 + i));
        }
        return res;
    }

    private static byte[] cyclicEncode(byte[] frame) {

        BitSet input = BitSet.valueOf(frame);
        BitSet output = new BitSet(14 * frame.length);
        BitSet inputToEncode = new BitSet(4);
        for (int i = 0; i < input.length(); ++i) {
            for (int k = 0; k < 4; ++k) {
                inputToEncode.set(k, input.get(i));
                ++i;
            }
            for(byte a:inputToEncode.toByteArray()) System.out.println(a);
            BitSet outputBits = division(inputToEncode);
            for (int m = 0; m < 7; ++m) {
                output.set(i + m, outputBits.get(m));
            }

        }
        return output.toByteArray();
    }

    private static boolean check(BitSet x) {
        BitSet res = division(x);
        for (int i = 4; i < 7; ++i) {
            if ((res.get(i)) == true) return false;
        }
        return true;
    }

    private static byte[] cyclicDecode(byte[] frame) {
        BitSet output = BitSet.valueOf(frame);
        BitSet input = new BitSet(8 * frame.length);
        BitSet outputToDecode = new BitSet(7);
        for (int i = 0; i < output.length(); ++i) {
            for (int j = 0; j < 7; ++j) {
                outputToDecode.set(j, output.get(i));
                ++i;
            }
            if (check(outputToDecode)) {
                for (int m = 0; m < 4; ++i) {
                    input.set(i + m, outputToDecode.get(m));
                }
            }

        }
        return input.toByteArray();

    }*/

    private static BitSet division(BitSet x) {
        x = BitSet.valueOf(x.length()>0 ? new long[]{x.toLongArray()[0]*8} : new long[]{});
        BitSet ans = (BitSet) x.clone();
        BitSet divider = BitSet.valueOf(new long[] {11});
        int xLength = x.length(), dividerLength = divider.length();

        for(int keep = 0; keep <= xLength - dividerLength; keep++) {
            if (x.get(xLength-1-keep)) {
                for (int index = 0; index < dividerLength; index++) {
                    x.set(xLength-1-index-keep, x.get(xLength-1-index-keep) ^ divider.get(dividerLength-1-index));
                }
            }
        }

        ans.or(x);
        return ans;
    }

    private static byte[] cyclicEncode(byte[] frames) {
        BitSet output = new BitSet(frames.length*16);

        for(int keep = 0; keep < frames.length * 2; keep++) {
            BitSet value;
            if (keep % 2 == 0) {
                value = BitSet.valueOf(new byte[]{(byte)(frames[keep/2]&0x0F)});
            } else {
                value = BitSet.valueOf(new byte[]{(byte)(frames[keep/2]/16)});
            }
            value = division(value);
            for(int index = 1; index <= 7; index++) {
                output.set(keep*8+index, value.get(index-1));
            }
        }

        byte[] ans = new byte[frames.length*2];
        for (int keep = 0; keep < ans.length; keep++) {
            int value = 0;
            for(int length = 0; length < 8; length++) {
                if (output.get(keep*8+length)) {
                    value += Math.pow(2,length);
                }
            }
            ans[keep] = (byte) value;
        }
        return ans;
    }

    private static byte cyclicDecode(byte frame) {
        BitSet value = BitSet.valueOf(new byte[]{frame});
        value.and(BitSet.valueOf(new long[]{120}));

        byte[] bytes = value.toByteArray();
        return bytes.length >=0 ? bytes[0] : 0;
    }

    private static byte[] cyclicDecode(byte[] frames) {
        byte[] ans = new byte[frames.length/2];

        for(int keep = 0; keep < frames.length; keep++) {
            byte frame = frames[keep];
            int value = cyclicDecode(frame);

            if (keep % 2 == 0) {
                value = value >> 4;
            }

            ans[keep/2] |= value;
        }

        return ans;
    }

    private static boolean isCorrect(byte[] frames) {
        for(Byte frame : frames) {
            if (!frame.equals(cyclicEncode(new byte[]{cyclicDecode(frame)})[1])) {
                return false;
            }
        }
        return true;
    }


    public static void main(String[] args) {
        /*
        byte[] lol = {1,0,1,0};
        byte[] lolEncode = cyclicEncode(lol);
        for(byte a:lolEncode)System.out.println(a);
        byte[] lolDecode = cyclicDecode(lol);
        for(byte b:lolDecode)System.out.println(b);
        */
        byte[] lol = {118};
        for(byte a:lol){System.out.println(a); };
        byte[] lol1 = cyclicEncode(lol);
        for(byte a:lol1){System.out.println(a); };
        cyclicDecode(lol1);
        isCorrect(lol1);
    }
}
