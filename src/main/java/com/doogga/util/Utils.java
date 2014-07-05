package com.doogga.util;

/**
 * Created by joey on 14-7-2.
 */
public final class Utils {
    /**
     * bytes[3] = value >> 24
     * bytes[2] = value >> 16
     * bytes[1] = value >> 8
     * bytes[0] = value >> 0
     * @param value
     * @return
     */
    public static byte[] intToBytes(final int value) {
        int len = 4;
        byte[] bytes = new byte[len];
        for (int i = len -1; i >= 0; i --) {
            int offset = i * 8;
            bytes[i] = (byte) (value >> offset);
        }

        return bytes;
    }

    /**
     * bytes[7] = value >> 56
     * bytes[6] = value >> 48
     * bytes[5] = value >> 40
     * bytes[4] = value >> 32
     * bytes[3] = value >> 24
     * bytes[2] = value >> 16
     * bytes[1] = value >>  8
     * bytes[0] = value >>  0
     * @param value
     * @return
     */
    public static byte[] longToBytes(long value){
        int length = 8;
        byte[] bytes = new byte[length];
        for (int i = length - 1; i >= 0; i--) {
            int offset = i * 8; //56, 48, 40, 32, 24, 16, 8
            bytes[i] = (byte) (value >> offset);
        }
        return bytes;
    }

    /**
     * 操作符 << 的优先级比 & 高
     * intValue = (bytes[3] & 0xFF) << 24
     | (bytes[2] & 0xFF) << 16
     | (bytes[1] & 0xFF) <<  8
     | (bytes[0] & 0xFF) <<  0
     * @param bytes
     * @return
     */
    public static int bytesToInt (byte[] bytes){
        int length = 4;
        int intValue = 0;
        for (int i = length - 1; i >= 0; i--) {
            int offset = i * 8; //24, 16, 8
            intValue |= (bytes[i] & 0xFF) << offset;
        }
        return intValue;
    }
    /**
     * 操作符 << 的优先级比 & 高
     * longValue = (long)(bytes[7] & 0xFF) << 56
     | (long)(bytes[6] & 0xFF) << 48
     | (long)(bytes[5] & 0xFF) << 40
     | (long)(bytes[4] & 0xFF) << 32
     | (long)(bytes[3] & 0xFF) << 24
     | (long)(bytes[2] & 0xFF) << 16
     | (long)(bytes[1] & 0xFF) <<  8
     | (long)(bytes[0] & 0xFF) <<  0
     * @param bytes
     * @return
     */
    public static long bytesToLong (byte[] bytes){
        int length = 8;
        long longValue = 0;
        for (int i = length - 1; i >= 0; i--) {
            int offset = i * 8; //56, 48, 40, 32, 24, 16, 8
            longValue |= (long)(bytes[i] & 0xFF) << offset; //一定要先强制转换成long型再移位, 因为0xFF为int型
        }
        return longValue;
    }

    public static float bytesToFloat(byte[] bytes) {
        return Float.intBitsToFloat(bytesToInt(bytes));
    }

    public static double bytesToDouble(byte[] bytes) {
        return Double.longBitsToDouble(bytesToLong(bytes));
    }

    public static byte[] floatToBytes(float value){
        return intToBytes(Float.floatToIntBits(value));
    }

    public static byte[] doubleToBytes(double value){
        return longToBytes(Double.doubleToLongBits(value));
    }

    public static String bytes2HexString(byte[] b) {
        String ret = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }

    public static void main(String[] args) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) 65;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 97;
        bytes[3] = (byte) 0;

        System.out.println("String: " + new String(bytes));

        int iv = bytesToInt(bytes);
        float fv = bytesToFloat(bytes);
        byte[] bs = intToBytes(iv);
        byte[] fs = floatToBytes(fv);

        System.out.println("Int: " + iv);
        System.out.println("Float: " + fv);
        System.out.println("------------");

        for (int i = 0; i < bs.length; i++) {
            System.out.println(bs[i]);
        }
        System.out.println("============");
        for (int i = 0; i < fs.length; i++) {
            System.out.println(fs[i]);
        }
        System.out.println("============");
        System.out.println(bytesToFloat(floatToBytes(-0.45367f)));
        System.out.println("************");
//		System.out.println(0xff);
//		System.out.println(0xff00);
//		System.out.println(0xff0000);
//		System.out.println(0xff000000);

        byte[] bytesL = new byte[8];
        System.arraycopy(bytes, 0, bytesL, 0, bytes.length);
        bytesL[4] = (byte) 0;
        bytesL[5] = (byte) 0;
        bytesL[6] = (byte) 0;
        bytesL[7] = (byte) 0;

        long lv = bytesToLong(bytesL);
        double dv = bytesToDouble(bytesL);
        byte[] bls = longToBytes(lv);
        byte[] dls = doubleToBytes(dv);
        System.out.println("Long: " + lv);
        System.out.println("Double: " + dv);
        System.out.println("----");
        for (int i = 0; i < bls.length; i++) {
            System.out.println(bls[i]);
        }
        System.out.println("----");
        for (int i = 0; i < dls.length; i++) {
            System.out.println(dls[i]);
        }
        System.out.println("****");
        System.out.println(bytesToDouble(doubleToBytes(2.345)));

    }
}
