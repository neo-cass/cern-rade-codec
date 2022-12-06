package codec.javatolv;

import types.JavaContainer;
import types.labview.LVTypeLookup;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class LVTypeDescEncoder {
    public static byte[] createTypeDescrptor(JavaContainer javaContainer, ByteArrayOutputStream outStream){
        byte[] typeDesc = new byte[0];
        switch(javaContainer.getDataType()){
            case DT_DATA -> typeDesc = createClusterType(javaContainer, outStream);
            case DT_BYTE, DT_BOOL, DT_SHORT, DT_INT, DT_FLOAT, DT_DOUBLE -> typeDesc = createNumericType(javaContainer,outStream);
            case DT_STRING -> typeDesc = createStringType(javaContainer,outStream);
        }
        return typeDesc;
    }

    private static byte[] createNumericType(JavaContainer javaContainer, ByteArrayOutputStream outStream) {
        byte typeCode = LVTypeLookup.getByteFromCMW(javaContainer.getDataType());
        String fieldName = javaContainer.getFieldName();
        short totalLength = (short) (fieldName.length() + 1 + 4);
        byte [] typeDesc = new byte[totalLength];
        ByteBuffer byteBuffer = ByteBuffer.wrap(typeDesc);
        byteBuffer.putShort(totalLength);
        byteBuffer.put((byte) 0);
        byteBuffer.put(typeCode);
        byteBuffer.put((byte) fieldName.length());
        byteBuffer.put(fieldName.getBytes());
        return byteBuffer.array();
    }

    private static byte[] createStringType(JavaContainer javaContainer, ByteArrayOutputStream outStream){
        byte typeCode = LVTypeLookup.getByteFromCMW(javaContainer.getDataType());
        String fieldName = javaContainer.getFieldName();
        short totalLength = (short) (fieldName.length() + 1 + 8);
        byte [] typeDesc = new byte[totalLength];
        ByteBuffer byteBuffer = ByteBuffer.wrap(typeDesc);
        byteBuffer.putShort(totalLength);
        byteBuffer.put((byte) 0);
        byteBuffer.put(typeCode);
        byteBuffer.putShort((short) 0xFFFF); //FFFF FFFF
        byteBuffer.putShort((short) 0xFFFF);
        byteBuffer.put((byte) fieldName.length());
        byteBuffer.put(fieldName.getBytes());
        return byteBuffer.array();
    }

    public static byte[] createClusterType(JavaContainer javaContainer, ByteArrayOutputStream outStream){
        ArrayList<Byte> byteArray = new ArrayList<>();
        byte typeCode = LVTypeLookup.getByteFromCMW(javaContainer.getDataType());
        String fieldName = javaContainer.getFieldName();
        short totalLength = 0;
        short nTDs = (short) ((JavaContainer[])(javaContainer.getData())).length;
        JavaContainer[] javaContainers = (JavaContainer[]) javaContainer.getData();
        for (JavaContainer javaData : javaContainers){
            byte[] nestedtypeDesc = createTypeDescrptor(javaData, outStream);
            totalLength+=nestedtypeDesc.length;

            byteArray.addAll(nestedtypeDesc);
        }
        totalLength += (short) (fieldName.length() + 1 + 6);
        byteBuffer.putShort(0, nTDs);
        byteBuffer.put(0, typeCode);
        byteBuffer.put(0, (byte) 0);
        byteBuffer.putShort(0, totalLength);
        return Arrays.copyOfRange(byteBuffer.array(), 0, totalLength);
    }

    public static byte[] createArrayType(JavaContainer javaContainer, ByteArrayOutputStream outStream){
        byte typeCode = LVTypeLookup.getByteFromCMW(javaContainer.getDataType());

        String fieldName = javaContainer.getFieldName();
        //byte[] arrayType = createTypeDescrptor();
        return null;

    }
}
