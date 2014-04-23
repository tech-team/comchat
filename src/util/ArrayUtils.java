package util;

import java.lang.reflect.Array;

public class ArrayUtils {
    public static byte[] concatenate(byte[] A, byte[] B) {
        int aLen = A.length;
        int bLen = B.length;

        @SuppressWarnings("unchecked")
        byte[] C = (byte[]) Array.newInstance(A.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(A, 0, C, 0, aLen);
        System.arraycopy(B, 0, C, aLen, bLen);

        return C;
    }

    public static byte[] concatenate(byte a, byte[] B) {
        @SuppressWarnings("unchecked")
        byte[] A = new byte[1];
        A[0] = a;
        return concatenate(A, B);
    }
}
