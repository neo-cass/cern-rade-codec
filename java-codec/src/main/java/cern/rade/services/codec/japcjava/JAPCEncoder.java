package cern.rade.services.codec.japcjava;

import cern.japc.core.Parameter;
import cern.japc.core.factory.MapParameterValueFactory;
import cern.japc.core.factory.SimpleParameterValueFactory;
import cern.japc.value.MapParameterValue;
import cern.japc.value.ParameterValue;
import cern.japc.value.SimpleParameterValue;
import cern.rade.services.common.types.ArrayContainer;
import cern.rade.services.common.types.JavaContainer;
import cern.rade.services.common.types.PropertyContainer;

public final class JAPCEncoder {
    public static ParameterValue encode(JavaContainer<?> javaContainer){
        MapParameterValue mpv = MapParameterValueFactory.newMapParameterValue();
        switch(javaContainer.getDataType()){
            case DT_DATA -> {
                encodeData((PropertyContainer) javaContainer, mpv);
                return mpv;
            }
            default -> {
                encodeSingle(javaContainer, mpv);
                return mpv.castAsSimple();
            }
        }
    }

    private static MapParameterValue encodeData(PropertyContainer property, MapParameterValue mpv) {
        JavaContainer<Object>[] javaContainers = property.getData();

        for (JavaContainer<Object> javaContainer : javaContainers){
            encodeSingle(javaContainer, mpv);
        }
        return mpv;
    }

    private static void encodeSingle(JavaContainer<?> javaContainer, MapParameterValue mpv){
        switch(javaContainer.getDataType()){
            case DT_BYTE -> mpv.setByte(javaContainer.getName(), (byte) javaContainer.getData());
            case DT_BOOL -> mpv.setBoolean(javaContainer.getName(), (boolean) javaContainer.getData());
            case DT_SHORT -> mpv.setShort(javaContainer.getName(), (short) javaContainer.getData());
            case DT_INT -> mpv.setInt(javaContainer.getName(), (int) javaContainer.getData());
            case DT_FLOAT -> mpv.setFloat(javaContainer.getName(), (float) javaContainer.getData());
            case DT_DOUBLE -> mpv.setDouble(javaContainer.getName(), (double) javaContainer.getData());
            case DT_LONG -> mpv.setLong(javaContainer.getName(), (long) javaContainer.getData());
            case DT_STRING -> mpv.setString(javaContainer.getName(), (String) javaContainer.getData());

            case DT_BYTE_ARRAY -> mpv.setBytes(javaContainer.getName(), (byte[]) javaContainer.getData());
            case DT_BOOL_ARRAY -> mpv.setBooleans(javaContainer.getName(), (boolean[]) javaContainer.getData());
            case DT_SHORT_ARRAY -> mpv.setShorts(javaContainer.getName(), (short[]) javaContainer.getData());
            case DT_INT_ARRAY -> mpv.setInts(javaContainer.getName(), (int[]) javaContainer.getData());
            case DT_FLOAT_ARRAY -> mpv.setFloats(javaContainer.getName(), (float[]) javaContainer.getData());
            case DT_DOUBLE_ARRAY -> mpv.setDoubles(javaContainer.getName(), (double[]) javaContainer.getData());
            case DT_LONG_ARRAY -> mpv.setLongs(javaContainer.getName(), (long[]) javaContainer.getData());
            case DT_STRING_ARRAY -> mpv.setStrings(javaContainer.getName(), (String[]) javaContainer.getData());

            case DT_BYTE_ARRAY_2D -> mpv.setBytes2D(javaContainer.getName(), (byte[]) javaContainer.getData(), ((ArrayContainer)javaContainer).getDimensions());
            case DT_BOOL_ARRAY_2D -> mpv.setBooleans2D(javaContainer.getName(), (boolean[]) javaContainer.getData(), ((ArrayContainer)javaContainer).getDimensions());
            case DT_SHORT_ARRAY_2D -> mpv.setShorts2D(javaContainer.getName(), (short[]) javaContainer.getData(), ((ArrayContainer)javaContainer).getDimensions());
            case DT_INT_ARRAY_2D -> mpv.setInts2D(javaContainer.getName(), (int[]) javaContainer.getData(), ((ArrayContainer)javaContainer).getDimensions());
            case DT_FLOAT_ARRAY_2D -> mpv.setFloats2D(javaContainer.getName(), (float[]) javaContainer.getData(), ((ArrayContainer)javaContainer).getDimensions());
            case DT_DOUBLE_ARRAY_2D -> mpv.setDoubles2D(javaContainer.getName(), (double[]) javaContainer.getData(), ((ArrayContainer)javaContainer).getDimensions());
            case DT_LONG_ARRAY_2D -> mpv.setLongs2D(javaContainer.getName(), (long[]) javaContainer.getData(), ((ArrayContainer)javaContainer).getDimensions());
            case DT_STRING_ARRAY_2D -> mpv.setStrings2D(javaContainer.getName(), (String[]) javaContainer.getData(), ((ArrayContainer)javaContainer).getDimensions());
        }

    }
}
