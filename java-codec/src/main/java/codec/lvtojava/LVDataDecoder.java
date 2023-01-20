package codec.lvtojava;

import cern.cmw.data.DataType;
import types.*;
import types.labview.*;

import java.nio.ByteBuffer;

public final class LVDataDecoder {
    public static JavaContainer deserialise(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor){
        JavaContainer javaContainer = null;
        switch(lvTypeDescriptor.getType()){
            case CLUSTER -> {
                javaContainer = deserialiseCluster(buffer,lvTypeDescriptor);}
            case INT_8, INT_16, INT_32, INT_64, UINT_8, UINT_16, UINT_32, BOOL, SGL, DBL, STRING -> {
                javaContainer = deserialiseSingle(buffer,lvTypeDescriptor);}
            case ARRAY -> {
                javaContainer = deserialiseArray(buffer,lvTypeDescriptor);}
        }
        return javaContainer;
    }

    private static JavaContainer deserialiseSingle(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor) throws ClassNotFoundException, NoSuchMethodException {
        LVType type = lvTypeDescriptor.getType();
        String fieldName = lvTypeDescriptor.getFieldName();
        DataType dataType = null;
        JavaContainer data = null;
        switch(type){
            case INT_8 -> {data =  new JavaContainer<Byte>(fieldName, (Byte) getSingle(type, buffer),DataType.DT_BYTE, StorageType.SINGLE);}
            case BOOL -> {data = new JavaContainer<Boolean>(fieldName, (Boolean) getSingle(type, buffer), DataType.DT_BOOL, StorageType.SINGLE);}
            case INT_16, UINT_8 -> {data =  new JavaContainer<Short>(fieldName, (Short) getSingle(type, buffer),DataType.DT_SHORT, StorageType.SINGLE);}
            case INT_32, UINT_16 -> {data = new JavaContainer<Integer>(fieldName, (Integer) getSingle(type,buffer),DataType.DT_INT, StorageType.SINGLE);}
            case INT_64, UINT_32 -> {data = new JavaContainer<Long>(fieldName, (Long) getSingle(type,buffer),DataType.DT_LONG, StorageType.SINGLE);}
            case SGL -> {data = new JavaContainer<Float>(fieldName, (Float) getSingle(type,buffer),DataType.DT_FLOAT, StorageType.SINGLE);}
            case DBL -> {data = new JavaContainer<Double>(fieldName, (Double) getSingle(type,buffer), DataType.DT_DOUBLE, StorageType.SINGLE);}
            case STRING -> {data =new JavaContainer<String>(lvTypeDescriptor.getFieldName(), getString(lvTypeDescriptor.getType(), buffer), DataType.DT_STRING, StorageType.SINGLE);}
        }
        return data;
    }

    private static JavaContainer deserialiseCluster(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor) throws NoSuchMethodException {
        LVClusterInfo clusterInfo = (LVClusterInfo) lvTypeDescriptor.getTypeSpecificInfo();
        int nTDs = clusterInfo.getnTypeDescriptors();
        LVTypeDescriptor[] lvTypeDescriptors = clusterInfo.getTypeDescriptors();
        JavaContainer[] nestedContainers = new JavaContainer[nTDs];
        for (int i = 0; i< nTDs; i++){
            nestedContainers[i] = deserialise(buffer, lvTypeDescriptors[i]);
        }
        PropertyContainer propertyContainer = new PropertyContainer(lvTypeDescriptor.getFieldName(), nestedContainers, DataType.DT_DATA);
        return propertyContainer;
    }

    private static JavaContainer deserialiseArray(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor) throws NoSuchMethodException {
        LVArrayInfo arrayInfo = (LVArrayInfo) lvTypeDescriptor.getTypeSpecificInfo();
        LVTypeDescriptor arrTypeDesc = arrayInfo.getArrayType();
        LVType arrType = arrTypeDesc.getType();
        String elemName = arrTypeDesc.getFieldName();
        int dim = arrayInfo.getArrayDim();
        int[] dimensions = new int[dim];
        int arrLen = 1;

        for (int i=0; i<dim; i++) {
            dimensions[i] = buffer.getInt();
            arrLen *= dimensions[i];
        }

        DataType DT_elemType = TypeLookup.getType(arrType);
        DataType DT_arrType = TypeLookup.getArrType(DT_elemType, dim);
        JavaContainer[] arr = new JavaContainer[arrLen];
        for(int i = 0; i<arrLen; i++){
            arr[i] = deserialise(buffer, lvTypeDescriptor);
        }
        return new ArrayContainer<>(arr.getClass(), lvTypeDescriptor.getFieldName(), arr, DT_arrType, dimensions, elemName, DT_elemType);
    }

    private static Object getSingle(LVType type, ByteBuffer buffer){
        Object single=null;
        switch(type){
            case INT_8, INT_16, INT_32, INT_64, UINT_8, UINT_16, UINT_32, BOOL, SGL, DBL -> single=getNumeric(type, buffer);
            case STRING -> single=getString(type, buffer);
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

    private static String getString(LVType type, ByteBuffer buffer){
        int stringLen = buffer.getInt();
        byte[] rawString = new byte[stringLen];
        buffer.get(rawString);
        String string = new String(rawString);
        return string;
    }



}
