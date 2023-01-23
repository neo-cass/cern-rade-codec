package codec.lvtojava;

import cern.cmw.data.DataType;
import types.*;
import types.labview.*;

import java.nio.ByteBuffer;

public final class LVDataDecoder {
    public static JavaContainer deserialise(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor) throws NoSuchMethodException, ClassNotFoundException {
        switch(lvTypeDescriptor.getType()){
            case CLUSTER -> {
                return deserialiseCluster(buffer, (LVClusterDescriptor) lvTypeDescriptor);}
            case INT_8, INT_16, INT_32, INT_64, UINT_8, UINT_16, UINT_32, BOOL, SGL, DBL, STRING -> {
                return deserialiseSingle(buffer,lvTypeDescriptor);}
            case ARRAY -> {
                return deserialiseArray(buffer, (LVArrayDescriptor) lvTypeDescriptor);}
            default -> {return null;}
        }
    }

    private static JavaContainer deserialiseSingle(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor) throws ClassNotFoundException, NoSuchMethodException {
        LVType type = lvTypeDescriptor.getType();
        String fieldName = lvTypeDescriptor.getFieldName();
        switch(type){
            case INT_8 -> {return new JavaContainer<Byte>(fieldName, (Byte) getSingle(type, buffer),DataType.DT_BYTE);}
            case BOOL -> {return new JavaContainer<Boolean>(fieldName, (Boolean) getSingle(type, buffer), DataType.DT_BOOL);}
            case INT_16, UINT_8 -> {return new JavaContainer<Short>(fieldName, (Short) getSingle(type, buffer),DataType.DT_SHORT);}
            case INT_32, UINT_16 -> {return new JavaContainer<Integer>(fieldName, (Integer) getSingle(type,buffer),DataType.DT_INT);}
            case INT_64, UINT_32 -> {return new JavaContainer<Long>(fieldName, (Long) getSingle(type,buffer),DataType.DT_LONG);}
            case SGL -> {return new JavaContainer<Float>(fieldName, (Float) getSingle(type,buffer),DataType.DT_FLOAT);}
            case DBL -> {return new JavaContainer<Double>(fieldName, (Double) getSingle(type,buffer), DataType.DT_DOUBLE);}
            case STRING -> {return new JavaContainer<String>(lvTypeDescriptor.getFieldName(), getString(buffer), DataType.DT_STRING);}
            default -> {return null;}
        }
    }

    private static PropertyContainer deserialiseCluster(ByteBuffer buffer, LVClusterDescriptor lvClusterDescriptor) throws NoSuchMethodException, ClassNotFoundException {
        LVTypeDescriptor[] lvTypeDescriptors = lvClusterDescriptor.getTypeDescs();
        JavaContainer<Object>[] nestedContainers = new JavaContainer[lvTypeDescriptors.length];
        for (int i = 0; i< lvTypeDescriptors.length; i++){
            nestedContainers[i] = deserialise(buffer, lvTypeDescriptors[i]);
        }
        return new PropertyContainer(lvClusterDescriptor.getFieldName(), nestedContainers, DataType.DT_DATA);
    }

    private static ArrayContainer deserialiseArray(ByteBuffer buffer, LVArrayDescriptor lvArrayDescriptor) throws NoSuchMethodException, ClassNotFoundException {
        LVTypeDescriptor arrTypeDesc = lvArrayDescriptor.getArrayTypeDesc();
        LVType arrType = arrTypeDesc.getType();
        String elemName = arrTypeDesc.getFieldName();
        int dim = lvArrayDescriptor.getArrayDim();
        int[] dimensions = new int[dim];
        int arrLen = 1;

        for (int i=0; i<dim; i++) {
            dimensions[i] = buffer.getInt();
            arrLen *= dimensions[i];
        }

        DataType DT_elemType = TypeLookup.getType(arrType);
        DataType DT_arrType = TypeLookup.getArrType(DT_elemType, dim);

        switch(arrType){
            case CLUSTER -> {
                PropertyContainer[] arr = deserialiseClusterArray(buffer, (LVClusterDescriptor) arrTypeDesc, arrLen);
                return new ArrayContainer<>(lvArrayDescriptor.getFieldName(), arr, DT_arrType, dimensions, elemName, DT_elemType);}
            default -> {
                Object[] arr = deserialiseSingleArray(buffer, arrTypeDesc, arrLen);
                return new ArrayContainer<>(lvArrayDescriptor.getFieldName(), arr, DT_arrType, dimensions, elemName, DT_elemType);}
        }
    }

    private static Object[] deserialiseSingleArray(ByteBuffer buffer, LVTypeDescriptor arrTypeDesc, int arrLen) {
        Object[] arr = new Object[arrLen];
        for(int i = 0; i<arrLen; i++){
            arr[i] = getSingle(arrTypeDesc.getType(), buffer);
        }
        return arr;
    }

    private static PropertyContainer[] deserialiseClusterArray(ByteBuffer buffer, LVClusterDescriptor lvClusterDescriptor, int arrLen) throws ClassNotFoundException, NoSuchMethodException {
        PropertyContainer[] arr = new PropertyContainer[arrLen];
        for(int i = 0; i<arrLen; i++){
            arr[i] = (PropertyContainer) deserialise(buffer, lvClusterDescriptor);
        }
        return arr;
    }

    private static Object getSingle(LVType type, ByteBuffer buffer){
        Object single=null;
        switch(type){
            case INT_8, INT_16, INT_32, INT_64, UINT_8, UINT_16, UINT_32, BOOL, SGL, DBL -> single=getNumeric(type, buffer);
            case STRING -> single=getString(buffer);
        }
        return single;
    }

    private static Object getNumeric(LVType type, ByteBuffer buffer){
        Object data = null;
        switch(type){
            case INT_8 -> {data = buffer.get();}
            case BOOL -> {byte dataByte = buffer.get(); data = (((int)dataByte) == 1);}
            case INT_16, UINT_8 -> data =  buffer.getShort();
            case INT_32, UINT_16 -> data = buffer.getInt();
            case INT_64, UINT_32 -> data =buffer.getLong();
            case SGL -> data = buffer.getFloat();
            case DBL -> data = buffer.getDouble();
        }
        return data;
    }

    private static String getString(ByteBuffer buffer){
        int stringLen = buffer.getInt();
        byte[] rawString = new byte[stringLen];
        buffer.get(rawString);
        return new String(rawString);
    }

}
