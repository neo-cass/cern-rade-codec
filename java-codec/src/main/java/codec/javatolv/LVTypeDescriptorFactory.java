package codec.javatolv;

import cern.cmw.data.DataType;
import types.JavaDataContainer;
import types.labview.ArrayDescriptor;
import types.labview.LVTypeLookup;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class LVTypeDescriptorFactory {
    public static byte[] createTypeDescrptor(JavaDataContainer javaDataContainer){
        ByteBuffer byteBuffer = ByteBuffer.allocate(2048);
        switch(javaDataContainer.getDataType()){
            case DT_DATA -> byteBuffer.put(createClusterType(javaDataContainer));
            case DT_BYTE, DT_BOOL, DT_SHORT, DT_INT, DT_FLOAT, DT_DOUBLE -> byteBuffer.put(createNumericType(javaDataContainer));
            case DT_STRING -> byteBuffer.put(createStringType(javaDataContainer));
        }

        return byteBuffer.array();
    }

    private static byte[] createNumericType(JavaDataContainer javaDataContainer) {
        byte typeCode = LVTypeLookup.getByteFromCMW(javaDataContainer.getDataType());
        String fieldName = javaDataContainer.getFieldName();
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

    private static byte[] createStringType(JavaDataContainer javaDataContainer){
        System.out.println("decoding string");
        byte typeCode = LVTypeLookup.getByteFromCMW(javaDataContainer.getDataType());
        String fieldName = javaDataContainer.getFieldName();
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

    public static byte[] createClusterType(JavaDataContainer javaDataContainer){
        System.out.println("decoding cluster");
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byte typeCode = LVTypeLookup.getByteFromCMW(javaDataContainer.getDataType());
        String fieldName = javaDataContainer.getFieldName();
        short totalLength = 0;
        short nTDs = (short) ((JavaDataContainer[])(javaDataContainer.getData())).length;
        JavaDataContainer[] javaDataContainers = (JavaDataContainer[])javaDataContainer.getData();
        for (JavaDataContainer javaData : javaDataContainers){
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

    public static byte[] createArrayType(JavaDataContainer javaDataContainer){
        byte typeCode = LVTypeLookup.getByteFromCMW(javaDataContainer.getDataType());

        String fieldName = javaDataContainer.getFieldName();
        //byte[] arrayType = createTypeDescrptor();
        return null;

    }
}
