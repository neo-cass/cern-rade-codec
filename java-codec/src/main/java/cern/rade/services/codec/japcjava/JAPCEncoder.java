package cern.rade.services.codec.japcjava;

import cern.japc.core.factory.MapParameterValueFactory;
import cern.japc.core.factory.SimpleParameterValueFactory;
import cern.japc.value.MapParameterValue;
import cern.japc.value.ParameterValue;
import cern.japc.value.SimpleParameterValue;
import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.java.ArrayContainer;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.java.PropertyContainer;
//import org.checkerframework.checker.units.qual.C;

import java.util.Map;

public final class JAPCEncoder {
    public static ParameterValue encode(DataContainer dataContainer) throws CodecException {
        if(dataContainer.getDataType()==null){throw new CodecException("Type not set in data container so cannot encode.");}
        switch(dataContainer.getDataType()){
            case DT_DATA -> {
                MapParameterValue mpv = MapParameterValueFactory.newMapParameterValue();
                encodeData((PropertyContainer) dataContainer, mpv);
                return mpv;
            }
            case DT_BYTE, DT_BOOL, DT_INT, DT_SHORT, DT_LONG, DT_DOUBLE, DT_STRING, DT_FLOAT,
                    DT_BYTE_ARRAY, DT_BOOL_ARRAY, DT_INT_ARRAY, DT_SHORT_ARRAY, DT_LONG_ARRAY, DT_DOUBLE_ARRAY, DT_STRING_ARRAY, DT_FLOAT_ARRAY,
                    DT_BYTE_ARRAY_2D, DT_BOOL_ARRAY_2D, DT_INT_ARRAY_2D, DT_SHORT_ARRAY_2D, DT_LONG_ARRAY_2D, DT_DOUBLE_ARRAY_2D, DT_STRING_ARRAY_2D, DT_FLOAT_ARRAY_2D -> {
                return encodeSingle(dataContainer);
            }
            default -> throw new CodecException("Unsupported type: " + dataContainer.getDataType() + "to encode to JAPC ParameterValue");

        }
    }

    private static void encodeData(PropertyContainer property, MapParameterValue mpv) throws CodecException {
        for (Map.Entry<String, DataContainer> entry : property.getData().entrySet()){
            mpv.put(entry.getKey(), encodeSingle(entry.getValue()));
        }
    }

    private static SimpleParameterValue encodeSingle(DataContainer dataContainer) throws CodecException {
        if(dataContainer.getDataType()==null){throw new CodecException("Type not set to a supported type for single in data container so cannot encode.");}
        SimpleParameterValue spv = SimpleParameterValueFactory.newSimpleParameterValue();
        switch(dataContainer.getDataType()){
            case DT_BYTE -> spv.setByte((byte) dataContainer.getData());
            case DT_BOOL -> spv.setBoolean((boolean) dataContainer.getData());
            case DT_SHORT -> spv.setShort((short) dataContainer.getData());
            case DT_INT -> spv.setInt((int) dataContainer.getData());
            case DT_FLOAT -> spv.setFloat((float) dataContainer.getData());
            case DT_DOUBLE -> spv.setDouble((double) dataContainer.getData());
            case DT_LONG -> spv.setLong((long) dataContainer.getData());
            case DT_STRING -> spv.setString((String) dataContainer.getData());

            case DT_BYTE_ARRAY -> spv.setBytes((byte[]) dataContainer.getData());
            case DT_BOOL_ARRAY -> spv.setBooleans((boolean[]) dataContainer.getData());
            case DT_SHORT_ARRAY -> spv.setShorts((short[]) dataContainer.getData());
            case DT_INT_ARRAY -> spv.setInts((int[]) dataContainer.getData());
            case DT_FLOAT_ARRAY -> spv.setFloats((float[]) dataContainer.getData());
            case DT_DOUBLE_ARRAY -> spv.setDoubles((double[]) dataContainer.getData());
            case DT_LONG_ARRAY -> spv.setLongs((long[]) dataContainer.getData());
            case DT_STRING_ARRAY -> spv.setStrings((String[]) dataContainer.getData());

            case DT_BYTE_ARRAY_2D -> spv.setBytes2D((byte[]) dataContainer.getData(), ((ArrayContainer)dataContainer).getDimensions());
            case DT_BOOL_ARRAY_2D -> spv.setBooleans2D((boolean[]) dataContainer.getData(), ((ArrayContainer)dataContainer).getDimensions());
            case DT_SHORT_ARRAY_2D -> spv.setShorts2D((short[]) dataContainer.getData(), ((ArrayContainer)dataContainer).getDimensions());
            case DT_INT_ARRAY_2D -> spv.setInts2D((int[]) dataContainer.getData(), ((ArrayContainer)dataContainer).getDimensions());
            case DT_FLOAT_ARRAY_2D -> spv.setFloats2D((float[]) dataContainer.getData(), ((ArrayContainer)dataContainer).getDimensions());
            case DT_DOUBLE_ARRAY_2D -> spv.setDoubles2D((double[]) dataContainer.getData(), ((ArrayContainer)dataContainer).getDimensions());
            case DT_LONG_ARRAY_2D -> spv.setLongs2D((long[]) dataContainer.getData(), ((ArrayContainer)dataContainer).getDimensions());
            case DT_STRING_ARRAY_2D -> spv.setStrings2D((String[]) dataContainer.getData(), ((ArrayContainer)dataContainer).getDimensions());

            default -> throw new CodecException("Unsupported type.");
        }
        return spv;
    }
}
