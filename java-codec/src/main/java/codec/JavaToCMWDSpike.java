package codec;

import cern.cmw.data.Data;
import cern.cmw.data.DataFactory;
import types.CMWDataWrapper;
import types.JavaContainer;
import types.PropertyContainer;

import java.util.function.Function;

public class JavaToCMWDSpike<T> implements Function<JavaContainer<T>, Data> {
    @Override
    public Data apply(JavaContainer<T> javaContainer) {
        return null;
    }

    public static Data encode(JavaContainer javaData){
        CMWDataWrapper cmwDataW = null;
        switch(javaData.getDataType()){
            case DT_DATA -> cmwDataW = encodeCluster((PropertyContainer) javaData);
            default -> {cmwDataW = new CMWDataWrapper(); encode(javaData, cmwDataW);}
        }
        return data;
    }

    private static CMWDataWrapper encode(JavaContainer javaData, CMWDataWrapper cmwDataW){
        switch(javaData.getDataType()){
            case DT_DATA -> {data.append(javaData.getFieldName(), encodeCluster((PropertyContainer) javaData));}
            case DT_BOOL_ARRAY, DT_BYTE_ARRAY, DT_DOUBLE_ARRAY, DT_FLOAT_ARRAY, DT_STRING_ARRAY, DT_INT_ARRAY, DT_SHORT_ARRAY, DT_LONG_ARRAY, DT_DATA_ARRAY,
                    DT_BOOL_ARRAY_2D, DT_BYTE_ARRAY_2D, DT_DOUBLE_ARRAY_2D, DT_FLOAT_ARRAY_2D, DT_STRING_ARRAY_2D, DT_INT_ARRAY_2D, DT_SHORT_ARRAY_2D, DT_LONG_ARRAY_2D, DT_DATA_ARRAY_2D,
                    DT_BOOL_MULTI_ARRAY, DT_BYTE_MULTI_ARRAY, DT_DOUBLE_MULTI_ARRAY, DT_FLOAT_MULTI_ARRAY, DT_STRING_MULTI_ARRAY, DT_INT_MULTI_ARRAY, DT_SHORT_MULTI_ARRAY, DT_LONG_MULTI_ARRAY, DT_DATA_MULTI_ARRAY -> {appendEncodedArray(javaData, data);}
            case DT_BYTE, DT_SHORT, DT_INT, DT_LONG, DT_FLOAT, DT_DOUBLE, DT_BOOL, DT_STRING -> {appendEncodedSingle(javaData, data);}
        }
        return data;
    }


    private void encodeSingle(JavaContainer javaContainer, CMWDataWrapper cmwDataW){
        switch(javaContainer.getDataType()){
            case DT_BYTE -> cmwDataW.appendByte((JavaContainer<Byte>) javaContainer);
            case DT_SHORT -> cmwDataW.appendShort(cmwDataW.getFieldName(), (short)javaData.getData());
            case DT_INT -> cmwDataW.appendInt(cmwDataW.getFieldName(), (int)javaData.getData());
            case DT_LONG -> cmwDataW.appendLong(cmwDataW.getFieldName(), (long)javaData.getData());
            case DT_FLOAT -> cmwDataW.appendFloat(javaData.getFieldName(), (float)javaData.getData());
            case DT_DOUBLE -> cmwDataW.appendDouble(javaData.getFieldName(), (double)javaData.getData());
            case DT_BOOL -> cmwDataW.appendBool(javaData.getFieldName(), (boolean)javaData.getData());
            case DT_STRING -> cmwDataW.appendString(javaData.getFieldName(), (String)javaData.getData());
        }

    }

}
