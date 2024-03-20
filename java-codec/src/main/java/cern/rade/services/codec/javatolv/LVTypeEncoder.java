package cern.rade.services.codec.javatolv;

import cern.cmw.data.DataType;
import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.java.ArrayContainer;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.java.PropertyContainer;
import cern.rade.services.common.containers.TypeLookup;
import static cern.rade.services.common.BytesUtil.byteArrToShortArr;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

public final class LVTypeEncoder {
    public static short[] encodeTypeDescriptor(DataContainer DataContainer) throws CodecException {
        try{
            return byteArrToShortArr(encodeTypeDescriptor_bytes(DataContainer));
        } catch (IOException e) {
            throw new CodecException("Failed to encode type descriptor  due to IOException.", e);
        }
    }

    private static byte[] encodeTypeDescriptor_bytes(DataContainer dataContainer) throws IOException, CodecException {
        if(dataContainer.getDataType()==null){throw new CodecException("Data type of container not set, cannot encode.");}
        switch(dataContainer.getDataType()){
            case DT_DATA -> {return encodeClusterDescriptor((PropertyContainer) dataContainer);}
            case DT_BYTE, DT_BOOL, DT_SHORT, DT_LONG, DT_INT, DT_FLOAT, DT_DOUBLE, DT_STRING -> {return encodeSingleDescriptor(dataContainer.getName(), dataContainer.getDataType());}
            case DT_BYTE_ARRAY, DT_BOOL_ARRAY, DT_SHORT_ARRAY, DT_INT_ARRAY, DT_LONG_ARRAY, DT_FLOAT_ARRAY, DT_DOUBLE_ARRAY, DT_STRING_ARRAY, DT_DATA_ARRAY,
                    DT_BYTE_ARRAY_2D, DT_BOOL_ARRAY_2D, DT_SHORT_ARRAY_2D, DT_INT_ARRAY_2D, DT_LONG_ARRAY_2D, DT_FLOAT_ARRAY_2D, DT_DOUBLE_ARRAY_2D, DT_STRING_ARRAY_2D, DT_DATA_ARRAY_2D,
                    DT_BYTE_MULTI_ARRAY, DT_BOOL_MULTI_ARRAY, DT_SHORT_MULTI_ARRAY, DT_INT_MULTI_ARRAY, DT_LONG_MULTI_ARRAY, DT_FLOAT_MULTI_ARRAY, DT_DOUBLE_MULTI_ARRAY, DT_STRING_MULTI_ARRAY, DT_DATA_MULTI_ARRAY, DT_DISCRETE_FUNCTION_ARRAY -> {return encodeArrayDescriptor((ArrayContainer) dataContainer);}
            case DT_DISCRETE_FUNCTION, DT_DISCRETE_FUNCTION_LIST -> throw new CodecException("Unimplemented type");
            default -> throw new CodecException("Unknown type.");
        }
    }

    private static byte[] encodeSingleDescriptor(String fieldName, DataType dataType) throws CodecException {
        if(dataType==null){throw new CodecException("Data type given is null, cannot encode.");}
        switch(dataType){
            case DT_BYTE, DT_BOOL, DT_SHORT, DT_INT, DT_FLOAT, DT_DOUBLE -> {return encodeNumericDescriptor(fieldName, dataType);}
            case DT_STRING -> {return encodeStringDescriptor(fieldName, dataType);}
            default -> throw new CodecException("Trying to encode a single which is not of type single.");
        }
    }

    private static ByteBuffer padWithZeros(int totalLength){
        if(totalLength%2==0){
            return ByteBuffer.allocate(totalLength);
        }
        else{
            ByteBuffer byteBuffer = ByteBuffer.allocate(totalLength+1);
            byteBuffer.put(totalLength-1, (byte) 0);
            return byteBuffer;
        }
    }

    private static byte[] encodeNumericDescriptor(String fieldName, DataType dataType) {
        byte typeCode = TypeLookup.getLVTypeCode(dataType);
        byte [] fieldNameBytes = fieldName.getBytes();
        short totalLength = (short) (fieldNameBytes.length + 1 + 4);
        ByteBuffer byteBuffer = padWithZeros(totalLength);
        byteBuffer.putShort((short) byteBuffer.capacity());
        byteBuffer.put((byte) 64);
        byteBuffer.put(typeCode);
        byteBuffer.put((byte) fieldName.length());
        byteBuffer.put(fieldNameBytes);
        return byteBuffer.array();
    }

    private static byte[] encodeStringDescriptor(String fieldName, DataType dataType){
        byte typeCode = TypeLookup.getLVTypeCode(dataType);
        byte[] fieldNameBytes = fieldName.getBytes();
        short totalLength = (short) (fieldNameBytes.length + 1 + 8);
        ByteBuffer byteBuffer = padWithZeros(totalLength);
        byteBuffer.putShort((short) byteBuffer.capacity());
        byteBuffer.put((byte) 64);
        byteBuffer.put(typeCode);
        byteBuffer.putShort((short) 0xFFFF); //FFFF FFFF
        byteBuffer.putShort((short) 0xFFFF);
        byteBuffer.put((byte) fieldName.length());
        byteBuffer.put(fieldNameBytes);
        return byteBuffer.array();
    }

    private static byte[] encodeClusterDescriptor(PropertyContainer propertyContainer) throws IOException, CodecException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        byte typeCode = TypeLookup.getLVTypeCode(propertyContainer.getDataType());
        String fieldName = propertyContainer.getName();
        byte[] fieldNameBytes = fieldName.getBytes();
        short totalLength = 0;
        short nTDs = (short) propertyContainer.getData().size();
        for (Map.Entry<String, DataContainer> entry: propertyContainer.getData().entrySet()){
            byte[] nestedtypeDesc = encodeTypeDescriptor_bytes(entry.getValue());
            totalLength+=nestedtypeDesc.length;
            dos.write(nestedtypeDesc);
        }

        totalLength += (short) (fieldNameBytes.length + 1 + 6);
        ByteBuffer byteBuffer = padWithZeros(totalLength);
        byteBuffer.putShort((short) byteBuffer.capacity());
        byteBuffer.put((byte) 64);
        byteBuffer.put(typeCode);
        byteBuffer.putShort(nTDs);
        byteBuffer.put(bos.toByteArray());
        byteBuffer.put((byte) fieldName.length());
        byteBuffer.put(fieldNameBytes);
        return byteBuffer.array();
    }

    private static byte[] encodeArrayDescriptor(ArrayContainer arrayContainer) throws IOException, CodecException {
        Object[] arr = (Object[])arrayContainer.getData();
        int[] dimensions = arrayContainer.getDimensions();
        int dimension = dimensions.length;
        DataType elemType = arrayContainer.getElemType();
        byte typeCode = TypeLookup.getLVTypeCode(arrayContainer.getDataType());
        String fieldName = arrayContainer.getName();
        byte[] fieldNameBytes = fieldName.getBytes();
        short totalLength = 0;
        byte[] arrTypeDesc;

        if(elemType==DataType.DT_DATA){arrTypeDesc = encodeClusterDescriptor((PropertyContainer) arr[0]);}
        else{arrTypeDesc = encodeSingleDescriptor(arrayContainer.getElemName(), arrayContainer.getElemType());}

        totalLength += (short) (fieldNameBytes.length + 1 + 6 + (dimension*4) + arrTypeDesc.length);
        ByteBuffer byteBuffer = padWithZeros(totalLength);

        byteBuffer.putShort( (short) byteBuffer.capacity());
        //64 means LabVIEW will read the label names. 0 means LabVIEW will ignore label names
        //TODO: use a 0 when the fieldname is null (to be seen when I return to CERN)
        byteBuffer.put((byte) 64);
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
