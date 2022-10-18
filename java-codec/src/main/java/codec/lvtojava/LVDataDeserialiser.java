package codec.lvtojava;

import cern.cmw.data.DataType;
import types.JavaDataContainer;
import types.labview.ArrayDescriptor;
import types.labview.ClusterDescriptor;
import types.labview.LVType;
import types.labview.LVTypeDescriptor;

import java.nio.ByteBuffer;

public final class LVDataDeserialiser {

    private static JavaDataContainer deserialiseCluster(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor){
        ClusterDescriptor clusterDescriptor = (ClusterDescriptor) lvTypeDescriptor.getTypeSpecificInfo();
        int nTDs = clusterDescriptor.getnTypeDescriptors();
        LVTypeDescriptor[] lvTypeDescriptors = clusterDescriptor.getTypeDescriptors();
        JavaDataContainer[] javaData = new JavaDataContainer[nTDs];
        for (int i = 0; i< nTDs; i++){
            javaData[i] = deserialise(buffer, lvTypeDescriptors[i]);
        }
        JavaDataContainer javaDataContainer = new JavaDataContainer(lvTypeDescriptor.getFieldName(), javaData, DataType.DT_DATA);

        return javaDataContainer;
    }

    private static JavaDataContainer deserialiseNumeric(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor){
        LVType type = lvTypeDescriptor.getLvType();
        Object javaData = null;
        DataType dataType = null;
        switch(type){
            case INT_8 -> {javaData = buffer.get();dataType = DataType.DT_BYTE;}
            case INT_16 -> {javaData = buffer.getShort(); dataType = DataType.DT_SHORT;}
            case INT_32 -> {javaData = buffer.getInt();dataType = DataType.DT_INT;}
            case INT_64 -> {javaData = buffer.getLong();dataType = DataType.DT_LONG;}
            case UINT_8 -> {javaData = buffer.getShort();dataType = DataType.DT_SHORT;}
            case UINT_16 -> {javaData = buffer.getInt(); dataType = DataType.DT_INT;}
            case UINT_32 -> {javaData = buffer.getLong();dataType = DataType.DT_LONG;}
            case SGL -> {javaData = buffer.getFloat();dataType = DataType.DT_FLOAT;}
            case DBL -> {javaData = buffer.getDouble();dataType = DataType.DT_DOUBLE;}
            case BOOL -> {javaData = buffer.get();dataType = DataType.DT_BYTE;}
        }

        return new JavaDataContainer(lvTypeDescriptor.getFieldName(), javaData, dataType);

    }

    private static JavaDataContainer deserialiseString(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor){
        int stringLen = buffer.getInt();
        byte[] rawString = new byte[stringLen];
        buffer.get(rawString);
        String string = new String(rawString);
        return new JavaDataContainer(lvTypeDescriptor.getFieldName(), string, DataType.DT_STRING);
    }

    public static JavaDataContainer deserialise(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor){
        JavaDataContainer javaDataContainer = null;
        switch(lvTypeDescriptor.getLvType()){
            case CLUSTER -> {javaDataContainer = deserialiseCluster(buffer,lvTypeDescriptor);}
            case INT_8, INT_16, INT_32, INT_64, UINT_8, UINT_16, UINT_32, BOOL, SGL, DBL -> {javaDataContainer = deserialiseNumeric(buffer,lvTypeDescriptor);}
            case STRING -> {javaDataContainer = deserialiseString(buffer, lvTypeDescriptor);}
            case ARRAY -> {javaDataContainer = deserialiseArray(buffer,lvTypeDescriptor);}
        }
        return javaDataContainer;
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

    //TODO: multidimension support
    private static JavaDataContainer deserialiseArray(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor) {
        ArrayDescriptor arrayDescriptor = (ArrayDescriptor) lvTypeDescriptor.getTypeSpecificInfo();
        int dimension = arrayDescriptor.getArrayDim();
        int arrayLen = buffer.getInt();

        LVTypeDescriptor arrayType = arrayDescriptor.getArrayType();
        JavaDataContainer[] javaData = new JavaDataContainer[arrayLen];

        for (int i = 0; i< arrayLen; i++){
            javaData[i] = deserialise(buffer, arrayType);
        }
        DataType DT_arrayType = getArrayType(javaData[0].getDataType(), dimension);
        JavaDataContainer javaDataContainer = new JavaDataContainer(lvTypeDescriptor.getFieldName(), javaData, DT_arrayType);

        return javaDataContainer;
    }
}
