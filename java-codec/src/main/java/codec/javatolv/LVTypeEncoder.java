package codec.javatolv;

import types.JavaContainer;
import types.labview.LVArrayInfo;
import types.labview.LVTypeLookup;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;

public final class LVTypeEncoder {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static byte[] createTypeDescrptor(JavaContainer javaContainer) throws IOException {
        byte[] typeDesc = new byte[0];
        switch(javaContainer.getDataType()){
            case DT_DATA -> typeDesc = createClusterType(javaContainer);
            case DT_BYTE, DT_BOOL, DT_SHORT, DT_INT, DT_FLOAT, DT_DOUBLE -> typeDesc = createNumericType(javaContainer);
            case DT_STRING -> typeDesc = createStringType(javaContainer);
            case DT_BYTE_ARRAY, DT_BOOL_ARRAY, DT_SHORT_ARRAY, DT_INT_ARRAY, DT_LONG_ARRAY, DT_FLOAT_ARRAY, DT_DOUBLE_ARRAY, DT_STRING_ARRAY,
                    DT_BYTE_ARRAY_2D, DT_BOOL_ARRAY_2D, DT_SHORT_ARRAY_2D, DT_INT_ARRAY_2D, DT_LONG_ARRAY_2D, DT_FLOAT_ARRAY_2D, DT_DOUBLE_ARRAY_2D, DT_STRING_ARRAY_2D,
                    DT_BYTE_MULTI_ARRAY, DT_BOOL_MULTI_ARRAY, DT_SHORT_MULTI_ARRAY, DT_INT_MULTI_ARRAY, DT_LONG_MULTI_ARRAY, DT_FLOAT_MULTI_ARRAY, DT_DOUBLE_MULTI_ARRAY, DT_STRING_MULTI_ARRAY -> typeDesc = createArrayType(javaContainer);
        }
        return typeDesc;
    }

    public static ByteBuffer padWithZeros(int totalLength ){
        if(totalLength%2==0){
            return ByteBuffer.allocate(totalLength);
        }
        else{
            ByteBuffer byteBuffer = ByteBuffer.allocate(totalLength+1);
            byteBuffer.put(totalLength-1, (byte) 0);
            return byteBuffer;
        }
    }

    private static byte[] createNumericType(JavaContainer javaContainer ) {
        byte typeCode = LVTypeLookup.getByteFromCMW(javaContainer.getDataType());
        System.out.println(javaContainer.getDataType());
        String fieldName = javaContainer.getFieldName();
        byte [] fieldNameBytes = fieldName.getBytes();
        short totalLength = (short) (fieldNameBytes.length + 1 + 4);
        ByteBuffer byteBuffer = padWithZeros(totalLength);
        byteBuffer.putShort((short) byteBuffer.capacity());
        byteBuffer.put((byte) 0);
        byteBuffer.put(typeCode);
        byteBuffer.put((byte) fieldName.length());
        byteBuffer.put(fieldNameBytes);
        return byteBuffer.array();
    }

    private static byte[] createStringType(JavaContainer javaContainer){
        byte typeCode = LVTypeLookup.getByteFromCMW(javaContainer.getDataType());
        String fieldName = javaContainer.getFieldName();
        byte[] fieldNameBytes = fieldName.getBytes();
        short totalLength = (short) (fieldNameBytes.length + 1 + 8);
        ByteBuffer byteBuffer = padWithZeros(totalLength);
        byteBuffer.putShort((short) byteBuffer.capacity());
        byteBuffer.put((byte) 0);
        byteBuffer.put(typeCode);
        byteBuffer.putShort((short) 0xFFFF); //FFFF FFFF
        byteBuffer.putShort((short) 0xFFFF);
        byteBuffer.put((byte) fieldName.length());
        byteBuffer.put(fieldNameBytes);
        return byteBuffer.array();
    }

    public static byte[] createClusterType(JavaContainer javaContainer) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        byte typeCode = LVTypeLookup.getByteFromCMW(javaContainer.getDataType());
        String fieldName = javaContainer.getFieldName();
        byte[] fieldNameBytes = fieldName.getBytes();
        short totalLength = 0;
        short nTDs = (short) ((JavaContainer[])(javaContainer.getData())).length;
        JavaContainer[] javaContainers = (JavaContainer[]) javaContainer.getData();
        for (JavaContainer javaData : javaContainers){
            byte[] nestedtypeDesc = createTypeDescrptor(javaData);
            totalLength+=nestedtypeDesc.length;
            dos.write(nestedtypeDesc);
        }

        totalLength += (short) (fieldNameBytes.length + 1 + 6);
        ByteBuffer byteBuffer = padWithZeros(totalLength);
        byteBuffer.putShort((short) byteBuffer.capacity());
        byteBuffer.put((byte) 0);
        byteBuffer.put(typeCode);
        byteBuffer.putShort(nTDs);
        byteBuffer.put(bos.toByteArray());
        byteBuffer.put((byte) fieldName.length());
        byteBuffer.put(fieldNameBytes);
        return byteBuffer.array();
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


    public static byte[] createArrayType(JavaContainer javaContainer) throws IOException {

        JavaContainer[] arr = (JavaContainer[]) javaContainer.getData();
        int[] dimensions = javaContainer.getDimensions();
        int dimension = dimensions.length;
        byte typeCode = LVTypeLookup.getByteFromCMW(javaContainer.getDataType());
        String fieldName = javaContainer.getFieldName();
        byte[] fieldNameBytes = fieldName.getBytes();
        short totalLength = 0;
        byte[] arrTypeDesc = createTypeDescrptor(arr[0]);

        totalLength += (short) (fieldNameBytes.length + 1 + 6 + (dimension*4) + arrTypeDesc.length);
        ByteBuffer byteBuffer = padWithZeros(totalLength);

        byteBuffer.putShort( (short) byteBuffer.capacity());
        byteBuffer.put((byte) 0);
        byteBuffer.put(typeCode);
        byteBuffer.putShort((short) dimension);
        for(int i = 0; i<dimension*2; i++){
            byteBuffer.putShort((short) 0xFFFF);
        }
        byteBuffer.put(arrTypeDesc);
        byteBuffer.put((byte) fieldName.length());
        byteBuffer.put(fieldNameBytes);
        return byteBuffer.array();
    }
}
