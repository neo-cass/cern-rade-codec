package cern.rade.services.codec.javatolv;

import cern.cmw.data.DataType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.java.ArrayContainer;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.java.PropertyContainer;
public final class LVDataEncoder {
    public static byte[] encode(DataContainer DataContainer) throws CodecException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            encode(DataContainer, dos);
        } catch (IOException e) {
            throw new CodecException("Cannot encode type ",e);
        }
        return bos.toByteArray();
    }

    private static void encode(DataContainer dataContainer, DataOutputStream dos) throws IOException, CodecException {
        if(dataContainer.getDataType()==null){throw new CodecException("Data type of the container is not set, cannot encode data.");}
        switch(dataContainer.getDataType()){
            case DT_DATA -> encodeData((PropertyContainer) dataContainer, dos);
            case DT_BYTE, DT_BOOL, DT_SHORT, DT_INT, DT_LONG, DT_FLOAT, DT_DOUBLE, DT_STRING -> serialiseSingle(dataContainer.getDataType(), dataContainer.getData(),dos);
            case DT_BYTE_ARRAY, DT_BOOL_ARRAY, DT_SHORT_ARRAY, DT_INT_ARRAY, DT_LONG_ARRAY, DT_FLOAT_ARRAY, DT_DOUBLE_ARRAY, DT_STRING_ARRAY, DT_DATA_ARRAY,
                    DT_BYTE_ARRAY_2D, DT_BOOL_ARRAY_2D, DT_SHORT_ARRAY_2D, DT_INT_ARRAY_2D, DT_LONG_ARRAY_2D, DT_FLOAT_ARRAY_2D, DT_DOUBLE_ARRAY_2D, DT_STRING_ARRAY_2D, DT_DATA_ARRAY_2D,
                    DT_BYTE_MULTI_ARRAY, DT_BOOL_MULTI_ARRAY, DT_SHORT_MULTI_ARRAY, DT_INT_MULTI_ARRAY, DT_LONG_MULTI_ARRAY, DT_FLOAT_MULTI_ARRAY, DT_DOUBLE_MULTI_ARRAY, DT_STRING_MULTI_ARRAY, DT_DATA_MULTI_ARRAY, DT_DISCRETE_FUNCTION_ARRAY ->
                    serialiseArray((ArrayContainer) dataContainer,dos);
            case DT_DISCRETE_FUNCTION, DT_DISCRETE_FUNCTION_LIST -> throw new CodecException("Unimplemented type.");
            default -> throw new CodecException("Unknown type.");
        }
    }

    private static void encodeData(PropertyContainer propertyContainer, DataOutputStream dos) throws IOException, CodecException {
        for(Map.Entry<String, DataContainer> entry : propertyContainer.getData().entrySet()){
            dos.write(encode(entry.getValue()));
        }
    }

    private static void serialiseSingle(DataType dataType, Object data, DataOutputStream dos) throws IOException, CodecException {
        if(dataType==null){throw new CodecException("Data type of the container is not set, cannot encode data.");}
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
            default -> throw new CodecException("Trying to serialise a single which is not of single type");
        }
    }

    private static void serialiseArray(ArrayContainer arrayContainer, DataOutputStream dos) throws IOException, CodecException {
        int[] dimensions = arrayContainer.getDimensions();
        for (int dimension : dimensions) {dos.writeInt(dimension);}
        DataType dataType = arrayContainer.getElemType();
        if(dataType==null){throw new CodecException("Data type of the container is not set, cannot encode data.");}
        if (dataType == DataType.DT_DATA) {
            serialiseDataArray(arrayContainer, dos);
        } else {
            serialiseSingleArray(arrayContainer, dos);
        }
    }

    private static void serialiseDataArray(ArrayContainer arrayContainer, DataOutputStream dos) throws IOException, CodecException {
        PropertyContainer[] propertyContainers = (PropertyContainer[]) arrayContainer.getData();
        for(PropertyContainer propertyContainer : propertyContainers){dos.write(encode(propertyContainer));}
    }

    private static void serialiseSingleArray(ArrayContainer arrayContainer, DataOutputStream dos) throws IOException, CodecException {
        Object[] datum = arrayContainer.getData();
        for(Object data : datum){serialiseSingle(arrayContainer.getElemType(), data, dos);}
    }
}
