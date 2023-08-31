package cern.rade.services.codec.japcjava;

import cern.cmw.data.DataType;
import cern.japc.core.AcquiredParameterValue;
import cern.japc.value.MapParameterValue;
import cern.japc.value.SimpleParameterValue;
import cern.rade.services.codec.DataMismatchException;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.java.PropertyContainer;
import cern.rade.services.common.containers.TypeLookup;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public static DataContainer decode(PropertyContainer property, AcquiredParameterValue apv) throws DataMismatchException {
        MapParameterValue mpv = apv.getValue().castAsMap();
        HashSet<String> set = new HashSet<>(List.of(mpv.getNames()));
        boolean valid = property.getData().keySet().equals(set);
        if(!valid){
            throw new DataMismatchException("The fields of the provided PropertyContainer does not match the provided AcquiredParameterValue");
        }

        for(Map.Entry<String, DataContainer> entry: property.getData().entrySet()){
            DataContainer field = entry.getValue();
            String name = entry.getKey();
            SimpleParameterValue spv = mpv.get(name);
            if(field.getDataType() != TypeLookup.getCMWType(spv.getValueType(name))){
                throw new DataMismatchException("The type of the field provided does not match the field in the AcquiredParameterValue.");
            }
           decodeSimple(spv, field);
        }
        return property;
    }

    /**
     * Decodes an {@link AcquiredParameterValue} into a new {@link PropertyContainer}.
     * @param apv the {@link AcquiredParameterValue} to be deocded.
     * @return a new {@link PropertyContainer} containing the decoded {@link AcquiredParameterValue}
     */
    public static PropertyContainer decode(AcquiredParameterValue apv) {
        MapParameterValue mpv = apv.getValue().castAsMap();
        String[] names = mpv.getNames();
        PropertyContainer propertyContainer = new PropertyContainer(apv.getParameterName());
        for (String name : names){
            SimpleParameterValue spv = mpv.get(name);
            DataType dataType = TypeLookup.getCMWType(spv.getValueType(name));
            DataContainer dataContainer =  new DataContainer(name, dataType);
            decodeSimple(spv, dataContainer);
            propertyContainer.setDataContainer(name, dataContainer);
        }
        return propertyContainer;
    }

    private static void decodeSimple(SimpleParameterValue spv, DataContainer field){
        String name = field.getName();
        switch(field.getDataType()){
            case DT_BYTE -> field.setData(spv.getByte(name));
            case DT_BOOL -> field.setData(spv.getBoolean(name));
            case DT_SHORT -> field.setData(spv.getShort(name));
            case DT_INT -> field.setData(spv.getInt(name));
            case DT_LONG -> field.setData(spv.getLong(name));
            case DT_DOUBLE -> field.setData(spv.getDouble(name));
            case DT_FLOAT -> field.setData(spv.getFloat(name));
            case DT_STRING -> field.setData(spv.getString(name));

            case DT_BYTE_ARRAY -> field.setData(spv.getBytes(name));
            case DT_BOOL_ARRAY -> field.setData(spv.getBooleans(name));
            case DT_SHORT_ARRAY -> field.setData(spv.getShorts(name));
            case DT_INT_ARRAY -> field.setData(spv.getInts(name));
            case DT_LONG_ARRAY -> field.setData(spv.getLongs(name));
            case DT_DOUBLE_ARRAY -> field.setData(spv.getDoubles(name));
            case DT_FLOAT_ARRAY -> field.setData(spv.getFloats(name));
            case DT_STRING_ARRAY -> field.setData(spv.getStrings(name));

            //TODO add dimensions
            case DT_BYTE_ARRAY_2D -> field.setData(spv.getArray2D(name).getByteArray2D());
            case DT_BOOL_ARRAY_2D -> field.setData(spv.getArray2D(name).getBooleanArray2D());
            case DT_SHORT_ARRAY_2D -> field.setData(spv.getArray2D(name).getShortArray2D());
            case DT_INT_ARRAY_2D -> field.setData(spv.getArray2D(name).getIntArray2D());
            case DT_LONG_ARRAY_2D -> field.setData(spv.getArray2D(name).getLongArray2D());
            case DT_DOUBLE_ARRAY_2D -> field.setData(spv.getArray2D(name).getDoubleArray2D());
            case DT_FLOAT_ARRAY_2D -> field.setData(spv.getArray2D(name).getFloatArray2D());
            case DT_STRING_ARRAY_2D -> field.setData(spv.getArray2D(name).getStringArray2D());
        }

    }
}
