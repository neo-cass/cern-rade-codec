package cern.rade.services.codec.japcjava;

import cern.cmw.data.DataType;
import cern.japc.core.AcquiredParameterValue;
import cern.japc.value.MapParameterValue;
import cern.japc.value.SimpleParameterValue;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.java.PropertyContainer;
import cern.rade.services.common.containers.TypeLookup;


public final class JAPCDecoder {
    public static DataContainer<?> decode(PropertyContainer property, AcquiredParameterValue apv){
        DataContainer<Object>[] fields = (DataContainer<Object>[]) property.getData();
        MapParameterValue mpv = apv.getValue().castAsMap();
        for(DataContainer<Object> field : fields){
           decodeSimple(mpv.get(field.getName()), field);
        }
        return property;
    }

    public static PropertyContainer decode(AcquiredParameterValue apv){
        MapParameterValue mpv = apv.getValue().castAsMap();
        String[] names = mpv.getNames();
        DataContainer<Object>[] fields = new DataContainer[names.length];
        int counter = 0;
        for (String name : names){
            fields[counter] = decodeSimple(mpv.get(name), name);
            counter++;
        }
        return new PropertyContainer(apv.getParameterName(), fields, DataType.DT_DATA);
    }

    private static DataContainer<Object> decodeSimple(SimpleParameterValue spv, String name){
        DataType dataType = TypeLookup.getCMWType(spv.getValueType(name));
        DataContainer<Object> field = new DataContainer<>(name, null, dataType);
        decodeSimple(spv, field);
        return field;
    }

    private static void decodeSimple(SimpleParameterValue spv, DataContainer<Object> field){
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
