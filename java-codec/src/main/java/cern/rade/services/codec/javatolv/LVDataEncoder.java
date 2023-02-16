package cern.rade.services.codec.javatolv;

import cern.cmw.data.DataType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cern.rade.services.common.types.ArrayContainer;
import cern.rade.services.common.types.JavaContainer;
import cern.rade.services.common.types.PropertyContainer;
public final class LVDataEncoder {
    public static byte[] serialise(JavaContainer<?> javaContainer) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        serialise(javaContainer, dos);
        return bos.toByteArray();
    }

    public static void serialise(JavaContainer<?> javaContainer, DataOutputStream dos) throws IOException {
        switch(javaContainer.getDataType()){
            case DT_DATA -> serialiseData((PropertyContainer) javaContainer, dos);
            case DT_BYTE, DT_BOOL, DT_SHORT, DT_INT, DT_LONG, DT_FLOAT, DT_DOUBLE, DT_STRING -> serialiseSingle(javaContainer.getDataType(), javaContainer.getData(),dos);
            case DT_BYTE_ARRAY, DT_BOOL_ARRAY, DT_SHORT_ARRAY, DT_INT_ARRAY, DT_LONG_ARRAY, DT_FLOAT_ARRAY, DT_DOUBLE_ARRAY, DT_STRING_ARRAY, DT_DATA_ARRAY,
                    DT_BYTE_ARRAY_2D, DT_BOOL_ARRAY_2D, DT_SHORT_ARRAY_2D, DT_INT_ARRAY_2D, DT_LONG_ARRAY_2D, DT_FLOAT_ARRAY_2D, DT_DOUBLE_ARRAY_2D, DT_STRING_ARRAY_2D, DT_DATA_ARRAY_2D,
                    DT_BYTE_MULTI_ARRAY, DT_BOOL_MULTI_ARRAY, DT_SHORT_MULTI_ARRAY, DT_INT_MULTI_ARRAY, DT_LONG_MULTI_ARRAY, DT_FLOAT_MULTI_ARRAY, DT_DOUBLE_MULTI_ARRAY, DT_STRING_MULTI_ARRAY, DT_DATA_MULTI_ARRAY -> serialiseArray((ArrayContainer<?>) javaContainer,dos);
        }
    }

    private static void serialiseData(PropertyContainer propertyContainer, DataOutputStream dos) throws IOException {
        JavaContainer<?>[] nestedContainers = propertyContainer.getData();
        for(JavaContainer<?> nestedJavaContainer : nestedContainers){dos.write(serialise(nestedJavaContainer));}
    }

    private static void serialiseSingle(DataType dataType, Object data, DataOutputStream dos) throws IOException {
        switch(dataType){
            case DT_BYTE -> {dos.writeByte((Byte) data); }
            case DT_BOOL -> {
                boolean bool = (Boolean) data;
                byte boolByte = 0;
                if (bool){boolByte = 1;}
                dos.writeByte(boolByte);
            }
            case DT_SHORT -> {dos.writeShort((Short) data); }
            case DT_INT -> {dos.writeInt((Integer) data); }
            case DT_LONG -> {dos.writeLong((Long) data);}
            case DT_FLOAT -> {dos.writeFloat((Float) data);}
            case DT_DOUBLE -> {dos.writeDouble((Double) data); }
            case DT_STRING -> {
                byte[] rawString = ((String) data).getBytes();
                dos.writeInt(rawString.length);
                dos.write(rawString);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void serialiseArray(ArrayContainer arrayContainer, DataOutputStream dos) throws IOException {
        int[] dimensions = arrayContainer.getDimensions();
        for (int dimension : dimensions) {dos.writeInt(dimension);}
        DataType dataType = arrayContainer.getElemType();
        switch(dataType){
            case DT_DATA -> serialiseDataArray(arrayContainer, dos);
            default -> serialiseSingleArray(arrayContainer, dos);
        }
    }

    private static void serialiseDataArray(ArrayContainer<PropertyContainer> arrayContainer, DataOutputStream dos) throws IOException {
        PropertyContainer[] propertyContainers = arrayContainer.getData();
        for(PropertyContainer propertyContainer : propertyContainers){dos.write(serialise(propertyContainer));}
    }

    private static void serialiseSingleArray(ArrayContainer<?> arrayContainer, DataOutputStream dos) throws IOException {
        Object[] datum = arrayContainer.getData();
        for(Object data : datum){serialiseSingle(arrayContainer.getElemType(), data, dos);}
    }
}
