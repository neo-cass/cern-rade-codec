package cern.rade.services.codec.cmwjava;

import cern.cmw.data.Data;
import cern.rade.services.common.containers.java.ArrayContainer;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.java.PropertyContainer;
import java.util.function.Function;


public final class CMWDEncoder {
    public static Data encode(DataContainer<?> DataContainer){
        CMWDW cmwDataW;
        switch(DataContainer.getDataType()){
            case DT_DATA -> cmwDataW = encodeCluster((PropertyContainer) DataContainer);
            default -> {cmwDataW = new CMWDW(); encode(DataContainer, cmwDataW);}
        }
        return cmwDataW.getCmwData();
    }

    private static void encode(DataContainer<?> DataContainer, CMWDW cmwDataW){
        switch(DataContainer.getDataType()){
            case DT_DATA -> cmwDataW.appendProperty((PropertyContainer) DataContainer);
            case DT_BOOL_ARRAY, DT_BYTE_ARRAY, DT_DOUBLE_ARRAY, DT_FLOAT_ARRAY, DT_STRING_ARRAY, DT_INT_ARRAY, DT_SHORT_ARRAY, DT_LONG_ARRAY, DT_DATA_ARRAY,
                    DT_BOOL_ARRAY_2D, DT_BYTE_ARRAY_2D, DT_DOUBLE_ARRAY_2D, DT_FLOAT_ARRAY_2D, DT_STRING_ARRAY_2D, DT_INT_ARRAY_2D, DT_SHORT_ARRAY_2D, DT_LONG_ARRAY_2D, DT_DATA_ARRAY_2D,
                    DT_BOOL_MULTI_ARRAY, DT_BYTE_MULTI_ARRAY, DT_DOUBLE_MULTI_ARRAY, DT_FLOAT_MULTI_ARRAY, DT_STRING_MULTI_ARRAY, DT_INT_MULTI_ARRAY, DT_SHORT_MULTI_ARRAY, DT_LONG_MULTI_ARRAY, DT_DATA_MULTI_ARRAY -> encodeArray((ArrayContainer) DataContainer, cmwDataW);
            case DT_BYTE, DT_SHORT, DT_INT, DT_LONG, DT_FLOAT, DT_DOUBLE, DT_BOOL, DT_STRING -> encodeSingle(DataContainer, cmwDataW);
        }
    }

    @SuppressWarnings("unchecked")
    private static void encodeArray(ArrayContainer arrayContainer, CMWDW cmwDataW) {
        switch(arrayContainer.getDataType()){
            case DT_BYTE_ARRAY, DT_BYTE_ARRAY_2D, DT_BYTE_MULTI_ARRAY -> cmwDataW.appendByteArray(arrayContainer);
            case DT_SHORT_ARRAY, DT_SHORT_ARRAY_2D, DT_SHORT_MULTI_ARRAY -> cmwDataW.appendShortArray(arrayContainer);
            case DT_INT_ARRAY, DT_INT_ARRAY_2D, DT_INT_MULTI_ARRAY -> cmwDataW.appendIntArray(arrayContainer);
            case DT_LONG_ARRAY, DT_LONG_ARRAY_2D, DT_LONG_MULTI_ARRAY -> cmwDataW.appendLongArray(arrayContainer);
            case DT_FLOAT_ARRAY, DT_FLOAT_ARRAY_2D, DT_FLOAT_MULTI_ARRAY -> cmwDataW.appendFloatArray(arrayContainer);
            case DT_DOUBLE_ARRAY, DT_DOUBLE_ARRAY_2D, DT_DOUBLE_MULTI_ARRAY -> cmwDataW.appendDoubleArray(arrayContainer);
            case DT_BOOL_ARRAY, DT_BOOL_ARRAY_2D, DT_BOOL_MULTI_ARRAY -> cmwDataW.appendBoolArray( arrayContainer);
            case DT_STRING_ARRAY, DT_STRING_ARRAY_2D, DT_STRING_MULTI_ARRAY -> cmwDataW.appendStringArray(arrayContainer);
            case DT_DATA_ARRAY, DT_DATA_ARRAY_2D, DT_DATA_MULTI_ARRAY -> cmwDataW.appendDataArray(arrayContainer);
        }


    }

    @SuppressWarnings("unchecked")
    private static void encodeSingle(DataContainer<?> DataContainer, CMWDW cmwDataW){
        switch(DataContainer.getDataType()){
            case DT_BYTE -> cmwDataW.appendByte((DataContainer<Byte>) DataContainer);
            case DT_SHORT -> cmwDataW.appendShort((DataContainer<Short>) DataContainer);
            case DT_INT -> cmwDataW.appendInt((DataContainer<Integer>) DataContainer);
            case DT_LONG -> cmwDataW.appendLong((DataContainer<Long>) DataContainer);
            case DT_FLOAT -> cmwDataW.appendFloat((DataContainer<Float>) DataContainer);
            case DT_DOUBLE -> cmwDataW.appendDouble((DataContainer<Double>) DataContainer);
            case DT_BOOL -> cmwDataW.appendBool((DataContainer<Boolean>) DataContainer);
            case DT_STRING -> cmwDataW.appendString((DataContainer<String>) DataContainer);
        }
    }

    private static CMWDW encodeCluster(PropertyContainer propertyContainer){
        CMWDW cmwDataW = new CMWDW();
        DataContainer<?>[] DataContainers = propertyContainer.getData();
        for (DataContainer<?> DataContainer : DataContainers) {encode(DataContainer, cmwDataW);}
        return cmwDataW;
    }
}
