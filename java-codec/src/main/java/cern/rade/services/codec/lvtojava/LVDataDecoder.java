package cern.rade.services.codec.lvtojava;

import cern.cmw.data.DataType;
import org.apache.commons.lang3.ArrayUtils;
import cern.rade.services.common.containers.java.ArrayContainer;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.java.PropertyContainer;
import cern.rade.services.common.containers.TypeLookup;
import cern.rade.services.common.containers.labview.LVArrayDescriptor;
import cern.rade.services.common.containers.labview.LVClusterDescriptor;
import cern.rade.services.common.containers.labview.LVType;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;
import java.nio.ByteBuffer;

public final class LVDataDecoder {

    public static DataContainer decode(ByteBuffer buffer, DataContainer dataContainer, LVTypeDescriptor lvTypeDescriptor)   {
        switch(dataContainer.getDataType()){
            case DT_DATA -> {
                return decodeCluster(buffer, (PropertyContainer) dataContainer, (LVClusterDescriptor) lvTypeDescriptor);
            }
            case DT_BYTE, DT_BOOL, DT_SHORT, DT_INT, DT_LONG, DT_FLOAT, DT_DOUBLE, DT_STRING -> {
                return decodeSingle(buffer, dataContainer);
            }
            case DT_BOOL_ARRAY, DT_BYTE_ARRAY, DT_DOUBLE_ARRAY, DT_FLOAT_ARRAY, DT_STRING_ARRAY, DT_INT_ARRAY, DT_SHORT_ARRAY, DT_LONG_ARRAY, DT_DATA_ARRAY,
                    DT_BOOL_ARRAY_2D, DT_BYTE_ARRAY_2D, DT_DOUBLE_ARRAY_2D, DT_FLOAT_ARRAY_2D, DT_STRING_ARRAY_2D, DT_INT_ARRAY_2D, DT_SHORT_ARRAY_2D, DT_LONG_ARRAY_2D, DT_DATA_ARRAY_2D,
                    DT_BOOL_MULTI_ARRAY, DT_BYTE_MULTI_ARRAY, DT_DOUBLE_MULTI_ARRAY, DT_FLOAT_MULTI_ARRAY, DT_STRING_MULTI_ARRAY, DT_INT_MULTI_ARRAY, DT_SHORT_MULTI_ARRAY, DT_LONG_MULTI_ARRAY, DT_DATA_MULTI_ARRAY -> {
                return decodeArray(buffer, (ArrayContainer) dataContainer, (LVArrayDescriptor) lvTypeDescriptor);
            }
            default -> {throw new RuntimeException();}
        }
    }

    private static DataContainer decodeSingle(ByteBuffer buffer, DataContainer dataContainer){
        switch(dataContainer.getDataType()){
            case DT_BYTE -> {dataContainer.setData(buffer.get());}
            case DT_BOOL -> {dataContainer.setData(getBool(buffer));}
            case DT_SHORT -> {dataContainer.setData(buffer.getShort());}
            case DT_INT -> {dataContainer.setData(buffer.getInt());}
            case DT_LONG -> {dataContainer.setData(buffer.getLong());}
            case DT_FLOAT -> {dataContainer.setData(buffer.getFloat());}
            case DT_DOUBLE -> {dataContainer.setData(buffer.getDouble());}
            case DT_STRING -> {dataContainer.setData(getString(buffer));}
        }
        return dataContainer;
    }

    private static ArrayContainer decodeArray(ByteBuffer buffer, ArrayContainer arrayContainer, LVArrayDescriptor lvArrayDescriptor){
        LVTypeDescriptor lvElemDescriptor = lvArrayDescriptor.getElemDescriptor();
        int[] dimensions = getDims(buffer, lvArrayDescriptor);
        int len = getArrLen(dimensions);
        arrayContainer.setElemName(lvElemDescriptor.getName());
        arrayContainer.setDimensions(dimensions);
        switch(arrayContainer.getElemType()){
            case DT_BYTE -> {arrayContainer.setData(getByteArr(buffer, len), dimensions);}
            case DT_BOOL -> {arrayContainer.setData(getBoolArr(buffer, len), dimensions);}
            case DT_SHORT -> {arrayContainer.setData(getShortArr(buffer, len), dimensions);}
            case DT_INT -> {arrayContainer.setData(getIntArr(buffer, len));}
            case DT_LONG-> {arrayContainer.setData(getLongArr(buffer, len), dimensions);}
            case DT_FLOAT -> {arrayContainer.setData(getFloatArr(buffer, len), dimensions);}
            case DT_DOUBLE -> {arrayContainer.setData(getDoubleArr(buffer, len), dimensions);}
            case DT_STRING -> {arrayContainer.setData(getStringArr(buffer, len), dimensions);}
            case DT_DATA -> {arrayContainer.setData(getClusterArr(buffer, len, (LVClusterDescriptor) lvElemDescriptor), dimensions);}
        }
        return arrayContainer;
    }

    private static PropertyContainer decodeCluster(ByteBuffer buffer, PropertyContainer propertyContainer, LVClusterDescriptor lvClusterDescriptor){
        LVTypeDescriptor[] lvTypeDescriptors = lvClusterDescriptor.getFieldDescriptors();
        for(int i = 0; i< lvTypeDescriptors.length; i++){
            LVTypeDescriptor lvTypeDescriptor = lvTypeDescriptors[i];
            DataContainer dataContainer = propertyContainer.getField(lvTypeDescriptor.getName());
            decode(buffer, dataContainer, lvTypeDescriptor);
        }
        return propertyContainer;
    }

    private static PropertyContainer[] getClusterArr(ByteBuffer buffer, int len, LVClusterDescriptor lvClusterDescriptor) {
        PropertyContainer[] propertyContainers = LVDataContainerFactory.createPropertyContainerArray(lvClusterDescriptor, len);
        for(int i = 0; i<len; i++){
            decodeCluster(buffer, propertyContainers[i], lvClusterDescriptor);
        }
        return propertyContainers;
    }

    private static Byte[] getByteArr(ByteBuffer buffer, int len){
        byte[] arr = new byte[len];
        buffer.get(arr);
        return ArrayUtils.toObject(arr);
    }

    private static Boolean[] getBoolArr(ByteBuffer buffer, int len){
        boolean[] arr = new boolean[len];
        for(int i = 0; i<len; i++) {arr[i] = getBool(buffer);}
        return ArrayUtils.toObject(arr);
    }

    private static Short[] getShortArr(ByteBuffer buffer, int len){
        short[] arr = new short[len];
        for(int i = 0; i<len; i++) {arr[i] = buffer.getShort();}
        return ArrayUtils.toObject(arr);
    }

    private static Integer[] getIntArr(ByteBuffer buffer, int len){
        int[] arr = new int[len];
        for(int i = 0; i<len; i++) {arr[i] = buffer.getInt();}
        return ArrayUtils.toObject(arr);
    }

    private static Float[] getFloatArr(ByteBuffer buffer, int len){
        float[] arr = new float[len];
        for(int i = 0; i<len; i++) {arr[i] = buffer.getFloat();}
        return ArrayUtils.toObject(arr);
    }

    private static Double[] getDoubleArr(ByteBuffer buffer, int len){
        double[] arr = new double[len];
        for(int i = 0; i<len; i++) {arr[i] = buffer.getDouble();}
        return ArrayUtils.toObject(arr);
    }
    private static Long[] getLongArr(ByteBuffer buffer, int len){
        long[] arr = new long[len];
        for(int i = 0; i<len; i++) {arr[i] = buffer.getLong();}
        return ArrayUtils.toObject(arr);
    }
    private static String[] getStringArr(ByteBuffer buffer, int len){
        String[] arr = new String[len];
        for(int i = 0; i<len; i++) {arr[i] = getString(buffer);}
        return arr;
    }
    private static String getString(ByteBuffer buffer){
        int stringLen = buffer.getInt();
        byte[] rawString = new byte[stringLen];
        buffer.get(rawString);
        return new String(rawString);
    }

    private static boolean getBool(ByteBuffer buffer){
        byte dataByte = buffer.get();
        return (((int)dataByte) == 1);
    }

    private static int[] getDims(ByteBuffer buffer, LVArrayDescriptor lvArrayDescriptor){
        int dim = lvArrayDescriptor.getDimension();
        int[] dimensions = new int[dim];
        for (int i=0; i<dim; i++) {dimensions[i] = buffer.getInt();}
        return dimensions;
    }

    private static int getArrLen(int[] dimensions){
        int len = 1;
        for (int dimension : dimensions) {len *= dimension;}
        return len;
    }
}
