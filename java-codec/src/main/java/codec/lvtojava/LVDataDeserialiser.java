package codec.lvtojava;

import cern.cmw.data.DataType;
import types.JavaContainer;
import types.labview.LVArrayInfo;
import types.labview.LVClusterInfo;
import types.labview.LVType;
import types.labview.LVTypeDescriptor;

import java.nio.ByteBuffer;

public final class LVDataDeserialiser {

    private static JavaContainer deserialiseCluster(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor){
        LVClusterInfo LVClusterInfo = (LVClusterInfo) lvTypeDescriptor.getTypeSpecificInfo();
        int nTDs = LVClusterInfo.getnTypeDescriptors();
        LVTypeDescriptor[] lvTypeDescriptors = LVClusterInfo.getTypeDescriptors();
        JavaContainer[] javaData = new JavaContainer[nTDs];
        for (int i = 0; i< nTDs; i++){
            javaData[i] = deserialise(buffer, lvTypeDescriptors[i]);
        }
        JavaContainer javaContainer = new JavaContainer(lvTypeDescriptor.getFieldName(), javaData, DataType.DT_DATA);

        return javaContainer;
    }

    private static JavaContainer deserialiseNumeric(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor){
        LVType type = lvTypeDescriptor.getType();
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

        return new JavaContainer(lvTypeDescriptor.getFieldName(), javaData, dataType);

    }

    private static JavaContainer deserialiseString(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor){
        int stringLen = buffer.getInt();
        byte[] rawString = new byte[stringLen];
        buffer.get(rawString);
        String string = new String(rawString);
        return new JavaContainer(lvTypeDescriptor.getFieldName(), string, DataType.DT_STRING);
    }

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
    private static JavaContainer deserialiseArray(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor) {
        LVArrayInfo LVArrayInfo = (LVArrayInfo) lvTypeDescriptor.getTypeSpecificInfo();
        int dimension = LVArrayInfo.getArrayDim();
        int arrayLen = buffer.getInt();

        LVTypeDescriptor arrayType = LVArrayInfo.getArrayType();
        JavaContainer[] javaData = new JavaContainer[arrayLen];

        for (int i = 0; i< arrayLen; i++){
            javaData[i] = deserialise(buffer, arrayType);
        }
        DataType DT_arrayType = getArrayType(javaData[0].getDataType(), dimension);
        JavaContainer javaContainer = new JavaContainer(lvTypeDescriptor.getFieldName(), javaData, DT_arrayType);

        return javaContainer;
    }
}
