package cern.rade.services.codec.japcjava;

import cern.japc.core.Parameter;
import cern.japc.core.factory.MapParameterValueFactory;
import cern.japc.core.factory.SimpleParameterValueFactory;
import cern.japc.value.MapParameterValue;
import cern.japc.value.ParameterValue;
import cern.japc.value.SimpleParameterValue;
import cern.rade.services.codec.DataMismatchException;
import cern.rade.services.common.containers.java.ArrayContainer;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.java.PropertyContainer;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

public final class JAPCEncoder {
    public static ParameterValue encode(DataContainer DataContainer){
        MapParameterValue mpv = MapParameterValueFactory.newMapParameterValue();
        switch(DataContainer.getDataType()){
            case DT_DATA -> {
                encodeData((PropertyContainer) DataContainer, mpv);
                return mpv;
            }
            default -> {
                encodeSingle(DataContainer, mpv);
                return mpv.castAsSimple();
            }
        }
    }

    private static MapParameterValue encodeData(PropertyContainer property, MapParameterValue mpv) {
        for (Map.Entry<String, DataContainer> entry : property.getData().entrySet()){
            encodeSingle(entry.getValue(), mpv);
        }
        return mpv;
    }

    private static void encodeSingle(DataContainer DataContainer, MapParameterValue mpv){
        switch(DataContainer.getDataType()){
            case DT_BYTE -> mpv.setByte(DataContainer.getName(), (byte) DataContainer.getData());
            case DT_BOOL -> mpv.setBoolean(DataContainer.getName(), (boolean) DataContainer.getData());
            case DT_SHORT -> mpv.setShort(DataContainer.getName(), (short) DataContainer.getData());
            case DT_INT -> mpv.setInt(DataContainer.getName(), (int) DataContainer.getData());
            case DT_FLOAT -> mpv.setFloat(DataContainer.getName(), (float) DataContainer.getData());
            case DT_DOUBLE -> mpv.setDouble(DataContainer.getName(), (double) DataContainer.getData());
            case DT_LONG -> mpv.setLong(DataContainer.getName(), (long) DataContainer.getData());
            case DT_STRING -> mpv.setString(DataContainer.getName(), (String) DataContainer.getData());

            case DT_BYTE_ARRAY -> mpv.setBytes(DataContainer.getName(), (byte[]) DataContainer.getData());
            case DT_BOOL_ARRAY -> mpv.setBooleans(DataContainer.getName(), (boolean[]) DataContainer.getData());
            case DT_SHORT_ARRAY -> mpv.setShorts(DataContainer.getName(), (short[]) DataContainer.getData());
            case DT_INT_ARRAY -> mpv.setInts(DataContainer.getName(), (int[]) DataContainer.getData());
            case DT_FLOAT_ARRAY -> mpv.setFloats(DataContainer.getName(), (float[]) DataContainer.getData());
            case DT_DOUBLE_ARRAY -> mpv.setDoubles(DataContainer.getName(), (double[]) DataContainer.getData());
            case DT_LONG_ARRAY -> mpv.setLongs(DataContainer.getName(), (long[]) DataContainer.getData());
            case DT_STRING_ARRAY -> mpv.setStrings(DataContainer.getName(), (String[]) DataContainer.getData());

            case DT_BYTE_ARRAY_2D -> mpv.setBytes2D(DataContainer.getName(), (byte[]) DataContainer.getData(), ((ArrayContainer)DataContainer).getDimensions());
            case DT_BOOL_ARRAY_2D -> mpv.setBooleans2D(DataContainer.getName(), (boolean[]) DataContainer.getData(), ((ArrayContainer)DataContainer).getDimensions());
            case DT_SHORT_ARRAY_2D -> mpv.setShorts2D(DataContainer.getName(), (short[]) DataContainer.getData(), ((ArrayContainer)DataContainer).getDimensions());
            case DT_INT_ARRAY_2D -> mpv.setInts2D(DataContainer.getName(), (int[]) DataContainer.getData(), ((ArrayContainer)DataContainer).getDimensions());
            case DT_FLOAT_ARRAY_2D -> mpv.setFloats2D(DataContainer.getName(), (float[]) DataContainer.getData(), ((ArrayContainer)DataContainer).getDimensions());
            case DT_DOUBLE_ARRAY_2D -> mpv.setDoubles2D(DataContainer.getName(), (double[]) DataContainer.getData(), ((ArrayContainer)DataContainer).getDimensions());
            case DT_LONG_ARRAY_2D -> mpv.setLongs2D(DataContainer.getName(), (long[]) DataContainer.getData(), ((ArrayContainer)DataContainer).getDimensions());
            case DT_STRING_ARRAY_2D -> mpv.setStrings2D(DataContainer.getName(), (String[]) DataContainer.getData(), ((ArrayContainer)DataContainer).getDimensions());
        }

    }
}
