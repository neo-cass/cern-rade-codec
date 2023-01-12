package codec.lvtojava;

import cern.cmw.data.DataType;
import types.JavaContainer;
import types.labview.LVArrayInfo;
import types.labview.LVClusterInfo;
import types.labview.LVType;
import types.labview.LVTypeDescriptor;

import java.nio.ByteBuffer;

public final class LVDataDeserialiser {
    public static JavaContainer deserialise(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor){
        JavaContainer javaContainer = null;
        switch(lvTypeDescriptor.getType()){
            case CLUSTER -> {
                javaContainer = deserialiseCluster(buffer,lvTypeDescriptor);}
            case INT_8, INT_16, INT_32, INT_64, UINT_8, UINT_16, UINT_32, BOOL, SGL, DBL -> {
                javaContainer = deserialiseNumeric(buffer,lvTypeDescriptor);}
            case STRING -> {
                javaContainer = deserialiseString(buffer, lvTypeDescriptor);}
            case ARRAY -> {
                javaContainer = deserialiseArray(buffer,lvTypeDescriptor);}
        }
        return javaContainer;
    }

    private static JavaContainer deserialiseNumeric(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor){
        LVType type = lvTypeDescriptor.getType();
        DataType dataType = null;
        JavaContainer data = null;
        switch(type){
            case INT_8, BOOL -> {data =  new JavaContainer<Byte>(lvTypeDescriptor.getFieldName(), buffer.get(),DataType.DT_BYTE);}
            case INT_16, UINT_8 -> {data =  new JavaContainer<Short>(lvTypeDescriptor.getFieldName(), buffer.getShort(),DataType.DT_SHORT);}
            case INT_32, UINT_16 -> {data = new JavaContainer<Integer>(lvTypeDescriptor.getFieldName(), buffer.getInt(),DataType.DT_INT);}
            case INT_64, UINT_32 -> {data = new JavaContainer<Long>(lvTypeDescriptor.getFieldName(), buffer.getLong(),DataType.DT_LONG);}
            case SGL -> {data = new JavaContainer<Float>(lvTypeDescriptor.getFieldName(), buffer.getFloat(),DataType.DT_FLOAT);}
            case DBL -> {data = new JavaContainer<Double>(lvTypeDescriptor.getFieldName(), buffer.getDouble(),DataType.DT_DOUBLE);}
        }
        return data;
    }

    private static JavaContainer deserialiseCluster(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor){
        LVClusterInfo clusterInfo = (LVClusterInfo) lvTypeDescriptor.getTypeSpecificInfo();
        int nTDs = clusterInfo.getnTypeDescriptors();
        LVTypeDescriptor[] lvTypeDescriptors = clusterInfo.getTypeDescriptors();
        JavaContainer[] nestedContainers = new JavaContainer[nTDs];
        for (int i = 0; i< nTDs; i++){
            nestedContainers[i] = deserialise(buffer, lvTypeDescriptors[i]);
        }
        JavaContainer javaContainer = new JavaContainer<JavaContainer[]>(lvTypeDescriptor.getFieldName(), nestedContainers, DataType.DT_DATA);
        return javaContainer;
    }

    private static JavaContainer deserialiseString(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor){
        int stringLen = buffer.getInt();
        byte[] rawString = new byte[stringLen];
        buffer.get(rawString);
        String string = new String(rawString);
        return new JavaContainer<String>(lvTypeDescriptor.getFieldName(), string, DataType.DT_STRING);
    }

    private static DataType getArrayType(DataType type, int dimension){
        DataType dataType = null;
        if (dimension == 1){
            switch(type){
                case DT_BYTE -> dataType = DataType.DT_BYTE_ARRAY;
                case DT_SHORT -> dataType = DataType.DT_SHORT_ARRAY;
                case DT_INT -> dataType = DataType.DT_INT_ARRAY;
                case DT_LONG -> dataType = DataType.DT_LONG_ARRAY;
                case DT_FLOAT -> dataType = DataType.DT_FLOAT_ARRAY;
                case DT_DOUBLE -> dataType = DataType.DT_DOUBLE_ARRAY;
                case DT_BOOL -> dataType = DataType.DT_BOOL_ARRAY;
                case DT_STRING -> dataType = DataType.DT_STRING_ARRAY;
            }
        }
        else if (dimension == 2){
            switch(type){
                case DT_BYTE -> dataType = DataType.DT_BYTE_ARRAY_2D;
                case DT_SHORT -> dataType = DataType.DT_SHORT_ARRAY_2D;
                case DT_INT -> dataType = DataType.DT_INT_ARRAY_2D;
                case DT_LONG -> dataType = DataType.DT_LONG_ARRAY_2D;
                case DT_FLOAT -> dataType = DataType.DT_FLOAT_ARRAY_2D;
                case DT_DOUBLE -> dataType = DataType.DT_DOUBLE_ARRAY_2D;
                case DT_BOOL -> dataType = DataType.DT_BOOL_ARRAY_2D;
                case DT_STRING -> dataType = DataType.DT_STRING_ARRAY_2D;
            }
        }
        else if (dimension > 2){
            switch(type){
                case DT_BYTE -> dataType = DataType.DT_BYTE_MULTI_ARRAY;
                case DT_SHORT -> dataType = DataType.DT_SHORT_MULTI_ARRAY;
                case DT_INT -> dataType = DataType.DT_INT_MULTI_ARRAY;
                case DT_LONG -> dataType = DataType.DT_LONG_MULTI_ARRAY;
                case DT_FLOAT -> dataType = DataType.DT_FLOAT_MULTI_ARRAY;
                case DT_DOUBLE -> dataType = DataType.DT_DOUBLE_MULTI_ARRAY;
                case DT_BOOL -> dataType = DataType.DT_BOOL_MULTI_ARRAY;
                case DT_STRING -> dataType = DataType.DT_STRING_MULTI_ARRAY;
            }
        }
        else{
            //TODO: Unsupported type
        }

        return dataType;
    }

    //TODO: multidimension support and empty array will break this
    private static JavaContainer deserialiseArray(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor) {
        LVArrayInfo arrayInfo = (LVArrayInfo) lvTypeDescriptor.getTypeSpecificInfo();

        DataType arrayType = getArrayType(arrayType, dimension);
        JavaContainer javaContainer = new JavaContainer<>(lvTypeDescriptor.getFieldName(), nestedContainers, arrayType);
        return javaContainer;
    }

    private static JavaContainer spike(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor){
        LVArrayInfo arrInfo = (LVArrayInfo) lvTypeDescriptor.getTypeSpecificInfo();
        int dimension = arrInfo.getArrayDim();
        LVTypeDescriptor arrayType = arrInfo.getArrayType();
        int[] dimLens = new int[dimension];
        for (int i = 0; i<dimension; i++){
            dimLens[i] = buffer.getInt();
        }

        for (int i = 0; i<dimension; i++){

        }

    }

    private static JavaContainer[] deserialiseNDArray(ByteBuffer buffer, LVArrayInfo arrInfo) {
        Object nestedContainers = null;
        int dimension = arrInfo.getArrayDim();
        LVTypeDescriptor arrayType = arrInfo.getArrayType();
        if(dimension==1){
            nestedContainers = deserialise1DArr(buffer, arrayType);
        }

        for (int i = 0; i<dimension; i++){
            deserialiseNDArray(buffer, arrInfo);
        }
    }


    private static JavaContainer[] deserialise1DArr(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor){
        int len = buffer.getInt();
        JavaContainer[] javaContainers = new JavaContainer[len];
        for (int i = 0; i<len; i++){
            javaContainers[i] = deserialise(buffer, lvTypeDescriptor);
        }
        return javaContainers;
    }

}
