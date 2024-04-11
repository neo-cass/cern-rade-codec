package cern.rade.services.codec.japcjava;

import cern.cmw.data.DataType;
import cern.cmw.data.DataType.StorageType;
import cern.japc.core.AcquiredParameterValue;
import cern.japc.value.Array2D;
import cern.japc.value.MapParameterValue;
import cern.japc.value.SimpleParameterValue;
import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.java.ArrayContainer;
import cern.rade.services.common.containers.java.ContainerException;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.java.PropertyContainer;
import cern.rade.services.common.containers.TypeLookup;

import java.util.Map;

/**
 * Decodes JAPC types into {@link cern.rade.services.common.containers.java} types.
 */
public final class JAPCDecoder {
    /**
     * Decodes an {@link AcquiredParameterValue} into the provided {@link PropertyContainer}.
     * @param property the {@link PropertyContainer} where you want your {@link AcquiredParameterValue} to be decoded into.
     * @param apv the {@link AcquiredParameterValue} you want to decode.
     * @return the reference to the {@link PropertyContainer} you decoded into.
     */
    public static void decode(PropertyContainer property, AcquiredParameterValue apv) throws CodecException, ContainerException {
        MapParameterValue mpv = apv.getValue().castAsMap();
        //TODO: check that all the fields in property are in apv, throw CodecException if not
        for(Map.Entry<String, DataContainer> entry: property.getData().entrySet()){
            DataContainer field = entry.getValue();
            String name = entry.getKey();
            SimpleParameterValue spv = mpv.get(name);
            if(field.getDataType() != TypeLookup.getCMWType(spv.getValueType(name))){
                throw new CodecException("The type of the field provided does not match the field in the AcquiredParameterValue.");
            }
           decodeSimple(spv, field);
        }
    }

    /**
     * Decodes an {@link AcquiredParameterValue} into a new {@link PropertyContainer}.
     * @param apv the {@link AcquiredParameterValue} to be deocded.
     * @return a new {@link PropertyContainer} containing the decoded {@link AcquiredParameterValue}
     */
    public static PropertyContainer decode(AcquiredParameterValue apv) throws CodecException, ContainerException {
        MapParameterValue mpv = apv.getValue().castAsMap();
        String[] names = mpv.getNames();
        PropertyContainer propertyContainer = new PropertyContainer(apv.getParameterName());
        for (String name : names){
            SimpleParameterValue spv = mpv.get(name);
            DataType dataType = TypeLookup.getCMWType(spv.getValueType(name));
            DataContainer dataContainer = decodeSimple(spv, dataType, name);
            propertyContainer.setDataContainer(name, dataContainer);
        }
        return propertyContainer;
    }

    private static DataContainer decodeSimple(SimpleParameterValue spv, DataType dataType, String name) throws ContainerException, CodecException {
        DataContainer field = null;
        StorageType storageType = dataType.getStorageType();
        switch(storageType){
            case ARRAY, ARRAY_2D, MULTI_ARRAY -> {
                ArrayContainer container = new ArrayContainer(name, dataType);
                decodeArray(spv, container);
                field = container;
            }
            case SCALAR -> {
                DataContainer container = new DataContainer(name, dataType);
                decodeSingle(spv, container);
                field = container;
            }
        }
        return field;
    }

    private static void decodeSimple(SimpleParameterValue spv, DataContainer field) throws ContainerException, CodecException {
        StorageType storageType = field.getDataType().getStorageType();
        switch(storageType){
            case ARRAY, ARRAY_2D, MULTI_ARRAY -> {
                decodeArray(spv, (ArrayContainer) field);
            }
            case SCALAR -> {
                decodeSingle(spv, field);
            }
        }
    }


        private static void decodeArray(SimpleParameterValue spv, ArrayContainer field) throws CodecException, ContainerException {
        String name = field.getName();
        switch(field.getDataType()) {
            case DT_BYTE_ARRAY -> field.setData(spv.getBytes(name));
            case DT_BOOL_ARRAY -> field.setData(spv.getBooleans(name));
            case DT_SHORT_ARRAY -> field.setData(spv.getShorts(name));
            case DT_INT_ARRAY -> field.setData(spv.getInts(name));
            case DT_LONG_ARRAY -> field.setData(spv.getLongs(name));
            case DT_DOUBLE_ARRAY ->  field.setData(spv.getDoubles(name));
            case DT_FLOAT_ARRAY -> field.setData(spv.getFloats(name));
            case DT_STRING_ARRAY -> field.setData(spv.getStrings(name));

            case DT_BYTE_ARRAY_2D -> {
                Array2D array2D = spv.getArray2D(name);
                field.setData(array2D.getByteArray2D(), array2D.getRowCount(), array2D.getColumnCount());
            }
            case DT_BOOL_ARRAY_2D -> {
                Array2D array2D = spv.getArray2D(name);
                field.setData(array2D.getBooleanArray2D(), array2D.getRowCount(), array2D.getColumnCount());
            }
            case DT_SHORT_ARRAY_2D -> {
                Array2D array2D = spv.getArray2D(name);
                field.setData(array2D.getShortArray2D(), array2D.getRowCount(), array2D.getColumnCount());
            }
            case DT_INT_ARRAY_2D -> {
                Array2D array2D = spv.getArray2D(name);
                field.setData(array2D.getIntArray2D(), array2D.getRowCount(), array2D.getColumnCount());
            }
            case DT_LONG_ARRAY_2D -> {
                Array2D array2D = spv.getArray2D(name);
                field.setData(array2D.getLongArray2D(), array2D.getRowCount(), array2D.getColumnCount());
            }
            case DT_DOUBLE_ARRAY_2D -> {
                Array2D array2D = spv.getArray2D(name);
                field.setData(array2D.getDoubleArray2D(), array2D.getRowCount(), array2D.getColumnCount());
            }
            case DT_FLOAT_ARRAY_2D -> {
                Array2D array2D = spv.getArray2D(name);
                field.setData(array2D.getFloatArray2D(), array2D.getRowCount(), array2D.getColumnCount());
            }
            case DT_STRING_ARRAY_2D -> {
                Array2D array2D = spv.getArray2D(name);
                field.setData(array2D.getStringArray2D(), array2D.getRowCount(), array2D.getColumnCount());
            }
        }
    }

        private static void decodeSingle(SimpleParameterValue spv, DataContainer field) throws CodecException, ContainerException {
        String name = field.getName();
        if(field.getDataType()==null){throw new CodecException("Data container has no type set, cannot decode.");}
        switch(field.getDataType()){
            case DT_BYTE -> field.setData(spv.getByte(name));
            case DT_BOOL -> field.setData(spv.getBoolean(name));
            case DT_SHORT -> field.setData(spv.getShort(name));
            case DT_INT -> field.setData(spv.getInt(name));
            case DT_LONG -> field.setData(spv.getLong(name));
            case DT_DOUBLE -> field.setData(spv.getDouble(name));
            case DT_FLOAT -> field.setData(spv.getFloat(name));
            case DT_STRING -> field.setData(spv.getString(name));

           
            default -> throw new CodecException("Unsupported type");
        }

    }
}
