package codec.javatolv;

import types.JavaContainer;
import types.labview.LVTypeLookup;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class LVTypeDescriptorFactory {
    public static byte[] createTypeDescrptor(JavaContainer javaContainer){
        ByteBuffer byteBuffer = ByteBuffer.allocate(2048);
        switch(javaContainer.getDataType()){
            case DT_DATA -> byteBuffer.put(createClusterType(javaContainer));
            case DT_BYTE, DT_BOOL, DT_SHORT, DT_INT, DT_FLOAT, DT_DOUBLE -> byteBuffer.put(createNumericType(javaContainer));
            case DT_STRING -> byteBuffer.put(createStringType(javaContainer));
        }
        return byteBuffer.array();
    }

    private static byte[] createNumericType(JavaContainer javaContainer) {
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

    private static byte[] createStringType(JavaContainer javaContainer){
        System.out.println("decoding string");
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

    public static byte[] createClusterType(JavaContainer javaContainer){
        System.out.println("decoding cluster");
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byte typeCode = LVTypeLookup.getByteFromCMW(javaContainer.getDataType());
        String fieldName = javaContainer.getFieldName();
        short totalLength = 0;
        short nTDs = (short) ((JavaContainer[])(javaContainer.getData())).length;
        JavaContainer[] javaContainers = (JavaContainer[]) javaContainer.getData();
        for (JavaContainer javaData : javaContainers){
            byte[] nestedtypeDesc = createTypeDescrptor(javaData);
            totalLength+=nestedtypeDesc.length;

            byteBuffer.put(nestedtypeDesc);
        }
        totalLength += (short) (fieldName.length() + 1 + 6);
        byteBuffer.putShort(0, nTDs);
        byteBuffer.put(0, typeCode);
        byteBuffer.put(0, (byte) 0);
        byteBuffer.putShort(0, totalLength);
        return Arrays.copyOfRange(byteBuffer.array(), 0, totalLength);
    }

    public static byte[] createArrayType(JavaContainer javaContainer){
        byte typeCode = LVTypeLookup.getByteFromCMW(javaContainer.getDataType());

        String fieldName = javaContainer.getFieldName();
        //byte[] arrayType = createTypeDescrptor();
        return null;

    }
}
