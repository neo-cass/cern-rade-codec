package codec.javatolv;

import types.JavaContainer;
import types.labview.LVTypeLookup;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class LVTypeEncoder {
    public static byte[] createTypeDescrptor(JavaContainer javaContainer) throws IOException {
        byte[] typeDesc = new byte[0];
        switch(javaContainer.getDataType()){
            case DT_DATA -> typeDesc = createClusterType(javaContainer);
            case DT_BYTE, DT_BOOL, DT_SHORT, DT_INT, DT_FLOAT, DT_DOUBLE -> typeDesc = createNumericType(javaContainer);
            case DT_STRING -> typeDesc = createStringType(javaContainer);
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
        String fieldName = javaContainer.getFieldName();
        short totalLength = (short) (fieldName.length() + 1 + 4);
        ByteBuffer byteBuffer = padWithZeros(totalLength);
        byteBuffer.putShort((short) byteBuffer.capacity());
        byteBuffer.put((byte) 0);
        byteBuffer.put(typeCode);
        byteBuffer.put((byte) fieldName.length());
        byteBuffer.put(fieldName.getBytes());
        return byteBuffer.array();
    }

    private static byte[] createStringType(JavaContainer javaContainer){
        byte typeCode = LVTypeLookup.getByteFromCMW(javaContainer.getDataType());
        String fieldName = javaContainer.getFieldName();
        short totalLength = (short) (fieldName.length() + 1 + 8);
        ByteBuffer byteBuffer = padWithZeros(totalLength);
        byteBuffer.putShort((short) byteBuffer.capacity());
        byteBuffer.put((byte) 0);
        byteBuffer.put(typeCode);
        byteBuffer.putShort((short) 0xFFFF); //FFFF FFFF
        byteBuffer.putShort((short) 0xFFFF);
        byteBuffer.put((byte) fieldName.length());
        byteBuffer.put(fieldName.getBytes());
        return byteBuffer.array();
    }

    public static byte[] createClusterType(JavaContainer javaContainer) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        byte typeCode = LVTypeLookup.getByteFromCMW(javaContainer.getDataType());
        String fieldName = javaContainer.getFieldName();
        short totalLength = 0;
        short nTDs = (short) ((JavaContainer[])(javaContainer.getData())).length;
        JavaContainer[] javaContainers = (JavaContainer[]) javaContainer.getData();
        for (JavaContainer javaData : javaContainers){
            byte[] nestedtypeDesc = createTypeDescrptor(javaData);
            totalLength+=nestedtypeDesc.length;
            dos.write(nestedtypeDesc);
        }

        totalLength += (short) (fieldName.length() + 1 + 6);
        ByteBuffer byteBuffer = padWithZeros(totalLength);

        byteBuffer.putShort(0, nTDs);
        byteBuffer.put(0, typeCode);
        byteBuffer.put(0, (byte) 0);
        byteBuffer.putShort(0, (short) byteBuffer.capacity());
        byteBuffer.put(bos.toByteArray());
        return byteBuffer.array();
    }

    public static byte[] createArrayType(JavaContainer javaContainer ){
        byte typeCode = LVTypeLookup.getByteFromCMW(javaContainer.getDataType());

        String fieldName = javaContainer.getFieldName();
        //byte[] arrayType = createTypeDescrptor();
        return null;

    }
}
