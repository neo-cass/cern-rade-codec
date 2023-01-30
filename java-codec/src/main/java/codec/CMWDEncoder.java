package codec;

import cern.cmw.data.Data;
import types.ArrayContainer;
import types.CMWDW;
import types.JavaContainer;
import types.PropertyContainer;
import java.util.function.Function;

public final class CMWDEncoder implements Function<JavaContainer<?>, Data> {
    @Override
    public Data apply(JavaContainer<?> javaContainer) {
        return encode(javaContainer);
    }
    public static Data encode(JavaContainer<?> javaContainer){
        CMWDW cmwDataW;
        switch(javaContainer.getDataType()){
            case DT_DATA -> cmwDataW = encodeCluster((PropertyContainer) javaContainer);
            default -> {cmwDataW = new CMWDW(); encode(javaContainer, cmwDataW);}
        }
        return cmwDataW.getCmwData();
    }

    private static void encode(JavaContainer<?> javaContainer, CMWDW cmwDataW){
        switch(javaContainer.getDataType()){
            case DT_DATA -> cmwDataW.appendProperty((PropertyContainer) javaContainer);
            case DT_BOOL_ARRAY, DT_BYTE_ARRAY, DT_DOUBLE_ARRAY, DT_FLOAT_ARRAY, DT_STRING_ARRAY, DT_INT_ARRAY, DT_SHORT_ARRAY, DT_LONG_ARRAY, DT_DATA_ARRAY,
                    DT_BOOL_ARRAY_2D, DT_BYTE_ARRAY_2D, DT_DOUBLE_ARRAY_2D, DT_FLOAT_ARRAY_2D, DT_STRING_ARRAY_2D, DT_INT_ARRAY_2D, DT_SHORT_ARRAY_2D, DT_LONG_ARRAY_2D, DT_DATA_ARRAY_2D,
                    DT_BOOL_MULTI_ARRAY, DT_BYTE_MULTI_ARRAY, DT_DOUBLE_MULTI_ARRAY, DT_FLOAT_MULTI_ARRAY, DT_STRING_MULTI_ARRAY, DT_INT_MULTI_ARRAY, DT_SHORT_MULTI_ARRAY, DT_LONG_MULTI_ARRAY, DT_DATA_MULTI_ARRAY -> encodeArray((ArrayContainer) javaContainer, cmwDataW);
            case DT_BYTE, DT_SHORT, DT_INT, DT_LONG, DT_FLOAT, DT_DOUBLE, DT_BOOL, DT_STRING -> encodeSingle(javaContainer, cmwDataW);
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
    private static void encodeSingle(JavaContainer<?> javaContainer, CMWDW cmwDataW){
        switch(javaContainer.getDataType()){
            case DT_BYTE -> cmwDataW.appendByte((JavaContainer<Byte>) javaContainer);
            case DT_SHORT -> cmwDataW.appendShort((JavaContainer<Short>) javaContainer);
            case DT_INT -> cmwDataW.appendInt((JavaContainer<Integer>) javaContainer);
            case DT_LONG -> cmwDataW.appendLong((JavaContainer<Long>) javaContainer);
            case DT_FLOAT -> cmwDataW.appendFloat((JavaContainer<Float>) javaContainer);
            case DT_DOUBLE -> cmwDataW.appendDouble((JavaContainer<Double>) javaContainer);
            case DT_BOOL -> cmwDataW.appendBool((JavaContainer<Boolean>) javaContainer);
            case DT_STRING -> cmwDataW.appendString((JavaContainer<String>) javaContainer);
        }
    }

    private static CMWDW encodeCluster(PropertyContainer propertyContainer){
        CMWDW cmwDataW = new CMWDW();
        JavaContainer<?>[] javaContainers = propertyContainer.getData();
        for (JavaContainer<?> javaContainer : javaContainers) {encode(javaContainer, cmwDataW);}
        return cmwDataW;
    }

}
