package codec.javatolv;

import cern.cmw.data.DataType;
import types.ArrayContainer;
import types.JavaContainer;
import types.PropertyContainer;
import types.TypeLookup;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class LVTypeEncoder {

    public static byte[] createTypeDescriptor(JavaContainer javaContainer) throws IOException {
        switch(javaContainer.getDataType()){
            case DT_DATA -> {return createClusterDescriptor((PropertyContainer) javaContainer);}
            case DT_BYTE, DT_BOOL, DT_SHORT, DT_INT, DT_FLOAT, DT_DOUBLE, DT_STRING -> {return createSingleDescriptor(javaContainer.getDataType(), javaContainer.getFieldName());}
            case DT_BYTE_ARRAY, DT_BOOL_ARRAY, DT_SHORT_ARRAY, DT_INT_ARRAY, DT_LONG_ARRAY, DT_FLOAT_ARRAY, DT_DOUBLE_ARRAY, DT_STRING_ARRAY,
                    DT_BYTE_ARRAY_2D, DT_BOOL_ARRAY_2D, DT_SHORT_ARRAY_2D, DT_INT_ARRAY_2D, DT_LONG_ARRAY_2D, DT_FLOAT_ARRAY_2D, DT_DOUBLE_ARRAY_2D, DT_STRING_ARRAY_2D,
                    DT_BYTE_MULTI_ARRAY, DT_BOOL_MULTI_ARRAY, DT_SHORT_MULTI_ARRAY, DT_INT_MULTI_ARRAY, DT_LONG_MULTI_ARRAY, DT_FLOAT_MULTI_ARRAY, DT_DOUBLE_MULTI_ARRAY, DT_STRING_MULTI_ARRAY -> {return createArrayDescriptor((ArrayContainer) javaContainer);}
            default -> {return null;}
        }

    }

    public static byte[] createSingleDescriptor(DataType dataType, String fieldName)  {
        byte[] typeDesc = new byte[0];
        switch(dataType){
            case DT_BYTE, DT_BOOL, DT_SHORT, DT_INT, DT_FLOAT, DT_DOUBLE -> {return createNumericDescriptor(dataType, fieldName);}
            case DT_STRING -> {return createStringDescriptor(dataType, fieldName);}
        }
        return typeDesc;
    }

    public static ByteBuffer padWithZeros(int totalLength){
        if(totalLength%2==0){
            return ByteBuffer.allocate(totalLength);
        }
        else{
            ByteBuffer byteBuffer = ByteBuffer.allocate(totalLength+1);
            byteBuffer.put(totalLength-1, (byte) 0);
            return byteBuffer;
        }
    }

    private static byte[] createNumericDescriptor(DataType dataType, String fieldName) {
        byte typeCode = TypeLookup.getByteFromCMW(dataType);
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

    private static byte[] createStringDescriptor(DataType dataType, String fieldName){
        byte typeCode = TypeLookup.getByteFromCMW(dataType);
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

    public static byte[] createClusterDescriptor(PropertyContainer propertyContainer) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        byte typeCode = TypeLookup.getByteFromCMW(propertyContainer.getDataType());
        String fieldName = propertyContainer.getFieldName();
        byte[] fieldNameBytes = fieldName.getBytes();
        short totalLength = 0;
        short nTDs = (short) propertyContainer.getData().length;
        JavaContainer[] javaContainers = propertyContainer.getData();
        for (JavaContainer javaData : javaContainers){
            byte[] nestedtypeDesc = createTypeDescriptor(javaData);
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

    public static byte[] createArrayDescriptor(ArrayContainer<Object> arrayContainer) throws IOException {
        Object[] arr = arrayContainer.getData();
        int[] dimensions = arrayContainer.getDimensions();
        int dimension = dimensions.length;
        DataType elemType = arrayContainer.getDataType();
        byte typeCode = TypeLookup.getByteFromCMW(elemType);
        String fieldName = arrayContainer.getFieldName();
        byte[] fieldNameBytes = fieldName.getBytes();
        short totalLength = 0;
        byte[] arrTypeDesc;

        if(elemType==DataType.DT_DATA){arrTypeDesc = createClusterDescriptor((PropertyContainer) arr[0]);}
        else{arrTypeDesc = createSingleDescriptor(arrayContainer.getElemType(), arrayContainer.getElemName());}

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
