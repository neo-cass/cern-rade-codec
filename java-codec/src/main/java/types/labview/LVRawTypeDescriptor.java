package types.labview;

public class LVRawTypeDescriptor {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private byte[] bytes;
    private LVRawTypeDescriptor(byte[] bytes){
        this.bytes = bytes;
    }
    public static LVRawTypeDescriptor wrap(byte[] bytes){
        return new LVRawTypeDescriptor(bytes);
    }
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
    public String toString(){
        return bytesToHex(bytes);
    }
}
