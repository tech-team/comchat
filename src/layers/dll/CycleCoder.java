package layers.dll;

import layers.exceptions.DecodeException;

import java.util.Arrays;

public class CycleCoder {
    private byte[] map; //raw half byte -> coded byte map

    public CycleCoder() {
        map = new byte[] {0, 11, 22, 29, 39, 44, 49, 58, 69, 78, 83, 88, 98, 105, 116, 127};
    }

    public byte[] encode(byte[] data) {
        byte[] result = new byte[data.length * 2];

        for (int i = 0; i < data.length; ++i) {
            byte b = data[i];

            byte halfByte1 = (byte)(b & (byte) 0b00001111);
            result[2 * i] = map[halfByte1];

            byte halfByte2 = (byte)((b & (byte) 0b11110000) >> (byte) 4);
            result[2 * i + 1] = map[halfByte2];
        }

        return result;
    }

    public byte[] decode(byte[] data) throws DecodeException {
        byte[] result = new byte[data.length / 2];

        for (int i = 0; i < data.length - 1; i += 2) {
            byte res = 0;

            res |= mapReverse(data[i]);
            res |= mapReverse(data[i + 1]) << 4; //shift half byte left

            result[i / 2] = res;
        }

        return result;
    }

    private byte mapReverse(byte coded) throws DecodeException {
        for (int i = 0; i < map.length; ++i) {
            byte b = map[i];

            if (b == coded)
                return (byte) i;
        }

        throw new DecodeException();
    }

    public static void main(String[] args) {
        byte[] in = {10, 20, 30, 127, 0, 1};

        CycleCoder coder = new CycleCoder();

        //should output "equal"
        try {
            byte[] out = coder.decode(coder.encode(in));
            if (Arrays.equals(in, out))
                System.out.println("equal");
            else
                System.out.println("not equal");
        }
        catch (DecodeException e) {
            System.out.println("DecodeException");
        }

        //should output "DecodeException"
        try {
            byte[] coded = coder.encode(in);
            coded[0] = 101;

            byte[] out = coder.decode(coded);
            if (Arrays.equals(in, out))
                System.out.println("equal");
            else
                System.out.println("not equal");
        }
        catch (DecodeException e) {
            System.out.println("DecodeException");
        }
    }
}
