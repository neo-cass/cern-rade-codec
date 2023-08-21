package cern.rade.services.codec.cmwjava;

import cern.cmw.data.Data;
import cern.rade.services.common.containers.java.ArrayContainer;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.java.PropertyContainer;

/**
 * Encodes containers from {@link cern.rade.services.common.containers.java} into {@link Data}.
 */
public final class CMWDEncoder {
    /**
     * Due to nesting there are 2 encode functions. If the top level data is not DT_DATA, then we can call {@link CMWDEncoder#encodeData} straight away.
     * If not, we create the new CMW data object ourselves and then pass it to the other encode function.
     * We need the distinction because the encodeData function always creates its own CMW data object, whereas when encoding, for example, just a string, we do not want this behaviour.
     * The CMW data object has weird behaviour. For example, if we have DT_DATA_ARRAY, it must still be appended to a CMW data object.
     * When I reference CMW data here I do mean the raw CMW data object INSIDE the CMW data wrapper. See {@link CMWDW} for further clarification on what it adds to {@link Data}.
     * @param DataContainer the DataContainer to be encoded into CMW data.
     * @return the encoded data.
     */
    public static Data encode(DataContainer DataContainer){
        CMWDW cmwDataW;
        switch(DataContainer.getDataType()){
            case DT_DATA -> cmwDataW = encodeData((PropertyContainer) DataContainer);
            default -> {cmwDataW = new CMWDW(); encode(DataContainer, cmwDataW);}
        }
        return cmwDataW.getCmwData();
    }

    /**
     * Appends encoded CMW data to the provided CMW data wrapper provided.
     * @param DataContainer the DataCOntainer to be encoded and appended.
     * @param cmwDataW the CMW data wrapper which will be appended to.
     */
    private static void encode(DataContainer DataContainer, CMWDW cmwDataW){
        switch(DataContainer.getDataType()){
            case DT_DATA -> cmwDataW.appendProperty((PropertyContainer) DataContainer);
            case DT_BOOL_ARRAY, DT_BYTE_ARRAY, DT_DOUBLE_ARRAY, DT_FLOAT_ARRAY, DT_STRING_ARRAY, DT_INT_ARRAY, DT_SHORT_ARRAY, DT_LONG_ARRAY, DT_DATA_ARRAY,
                    DT_BOOL_ARRAY_2D, DT_BYTE_ARRAY_2D, DT_DOUBLE_ARRAY_2D, DT_FLOAT_ARRAY_2D, DT_STRING_ARRAY_2D, DT_INT_ARRAY_2D, DT_SHORT_ARRAY_2D, DT_LONG_ARRAY_2D, DT_DATA_ARRAY_2D,
                    DT_BOOL_MULTI_ARRAY, DT_BYTE_MULTI_ARRAY, DT_DOUBLE_MULTI_ARRAY, DT_FLOAT_MULTI_ARRAY, DT_STRING_MULTI_ARRAY, DT_INT_MULTI_ARRAY, DT_SHORT_MULTI_ARRAY, DT_LONG_MULTI_ARRAY, DT_DATA_MULTI_ARRAY -> encodeArray((ArrayContainer) DataContainer, cmwDataW);
            case DT_BYTE, DT_SHORT, DT_INT, DT_LONG, DT_FLOAT, DT_DOUBLE, DT_BOOL, DT_STRING -> encodeSingle(DataContainer, cmwDataW);
        }
    }

    /**
     * Appends an encoded ArrayContainer to the provided CMW data wrapper provided.
     * @param arrayContainer the ArrayContainer to be encoded and appended.
     * @param cmwDataW the CMW data wrapper which will be appended to.
     */
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

    /**
     * Appends an encoded DataContainer to the provided CMW data wrapper provided. The DataContainer will contain a "single" type, by this I mean there is no nesting.
     * @param  dataContainer DataContainer to be encoded and appended.
     * @param cmwDataW the CMW data wrapper which will be appended to.
     */
    @SuppressWarnings("unchecked")
    private static void encodeSingle(DataContainer<?> dataContainer, CMWDW cmwDataW){
        switch(dataContainer.getDataType()){
            case DT_BYTE -> cmwDataW.appendByte((DataContainer<Byte>) dataContainer);
            case DT_SHORT -> cmwDataW.appendShort((DataContainer<Short>) dataContainer);
            case DT_INT -> cmwDataW.appendInt((DataContainer<Integer>) dataContainer);
            case DT_LONG -> cmwDataW.appendLong((DataContainer<Long>) dataContainer);
            case DT_FLOAT -> cmwDataW.appendFloat((DataContainer<Float>) dataContainer);
            case DT_DOUBLE -> cmwDataW.appendDouble((DataContainer<Double>) dataContainer);
            case DT_BOOL -> cmwDataW.appendBool((DataContainer<Boolean>) dataContainer);
            case DT_STRING -> cmwDataW.appendString((DataContainer<String>) dataContainer);
        }
    }

    /**
     * Encodes a PropertyContainer into a CMW data wrapper. As mentioned above, this function actually creates a CMW data object, unlike the other functions in this class which only append data.
     * @param propertyContainer the PropertyContainer to be enocded.
     * @return the encoded CMW data wrapper.
     */
    private static CMWDW encodeData(PropertyContainer propertyContainer){
        CMWDW cmwDataW = new CMWDW();
        DataContainer<?>[] DataContainers = propertyContainer.getData();
        for (DataContainer<?> DataContainer : DataContainers) {encode(DataContainer, cmwDataW);}
        return cmwDataW;
    }
}
