package codec.lvtojava;

import cern.cmw.data.DataType;
import org.apache.commons.lang3.ArrayUtils;
import types.ArrayContainer;
import types.JavaContainer;
import types.PropertyContainer;
import types.TypeLookup;
import types.labview.LVArrayDescriptor;
import types.labview.LVClusterDescriptor;
import types.labview.LVType;
import types.labview.LVTypeDescriptor;
import java.nio.ByteBuffer;

public final class LVDataDecoder {
    public static JavaContainer<?> deserialise(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor)   {
        switch(lvTypeDescriptor.getType()){
            case CLUSTER -> {
                return deserialiseCluster(buffer, (LVClusterDescriptor) lvTypeDescriptor);}
            case INT_8, INT_16, INT_32, INT_64, UINT_8, UINT_16, UINT_32, BOOL, SGL, DBL, STRING -> {
                return deserialiseSingle(buffer,lvTypeDescriptor);}
            case ARRAY -> {
                return deserialiseArray(buffer, (LVArrayDescriptor) lvTypeDescriptor);}
            default -> {return null;}
        }
    }

    private static JavaContainer<?> deserialiseSingle(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor){
        LVType type = lvTypeDescriptor.getType();
        String fieldName = lvTypeDescriptor.getFieldName();
        switch(type){
            case INT_8 -> {return new JavaContainer<Byte>(fieldName, (Byte) buffer.get(), DataType.DT_BYTE);}
            case BOOL -> {return new JavaContainer<Boolean>(fieldName, (Boolean) getBool(buffer), DataType.DT_BOOL);}
            case INT_16, UINT_8 -> {return new JavaContainer<Short>(fieldName, (Short) buffer.getShort(),DataType.DT_SHORT);}
            case INT_32, UINT_16 -> {return new JavaContainer<Integer>(fieldName, (Integer) buffer.getInt(),DataType.DT_INT);}
            case INT_64, UINT_32 -> {return new JavaContainer<Long>(fieldName, (Long) buffer.getLong(),DataType.DT_LONG);}
            case SGL -> {return new JavaContainer<Float>(fieldName, (Float) buffer.getFloat(),DataType.DT_FLOAT);}
            case DBL -> {return new JavaContainer<Double>(fieldName, (Double) buffer.getDouble(), DataType.DT_DOUBLE);}
            case STRING -> {return new JavaContainer<String>(lvTypeDescriptor.getFieldName(), getString(buffer), DataType.DT_STRING);}
            default -> {return null;}
        }
    }

    private static ArrayContainer<?> deserialiseArray(ByteBuffer buffer, LVArrayDescriptor lvArrayDescriptor){
        LVTypeDescriptor lvElemDescriptor = lvArrayDescriptor.getElemDescriptor();
        int[] dimensions = getDims(buffer, lvArrayDescriptor);
        int len = getArrLen(dimensions);
        DataType elemDt = TypeLookup.getType(lvElemDescriptor.getType());
        DataType dt = TypeLookup.getArrType(elemDt, dimensions.length);

        switch(lvElemDescriptor.getType()){
            case INT_8 -> {return new ArrayContainer<>(lvArrayDescriptor.getFieldName(), lvElemDescriptor.getFieldName(), getByteArr(buffer, len), dimensions, dt, elemDt);}
            case BOOL -> {return new ArrayContainer<>(lvArrayDescriptor.getFieldName(), lvElemDescriptor.getFieldName(), getBoolArr(buffer, len), dimensions, dt, elemDt);}
            case INT_16, UINT_8 -> {return new ArrayContainer<>(lvArrayDescriptor.getFieldName(), lvElemDescriptor.getFieldName(), getShortArr(buffer, len), dimensions, dt, elemDt);}
            case INT_32, UINT_16 -> {return new ArrayContainer<>(lvArrayDescriptor.getFieldName(), lvElemDescriptor.getFieldName(), getIntArr(buffer, len), dimensions, dt, elemDt);}
            case INT_64, UINT_32 -> {return new ArrayContainer<>(lvArrayDescriptor.getFieldName(), lvElemDescriptor.getFieldName(), getLongArr(buffer, len), dimensions, dt, elemDt);}
            case SGL -> {return new ArrayContainer<>(lvArrayDescriptor.getFieldName(), lvElemDescriptor.getFieldName(), getFloatArr(buffer, len), dimensions, dt, elemDt);}
            case DBL -> {return new ArrayContainer<>(lvArrayDescriptor.getFieldName(), lvElemDescriptor.getFieldName(), getDoubleArr(buffer, len), dimensions, dt, elemDt);}
            case STRING -> {return new ArrayContainer<>(lvArrayDescriptor.getFieldName(), lvElemDescriptor.getFieldName(), getStringArr(buffer, len), dimensions, dt, elemDt);}
            case CLUSTER -> {return new ArrayContainer<>(lvArrayDescriptor.getFieldName(), lvElemDescriptor.getFieldName(), getClusterArr(buffer, len, (LVClusterDescriptor) lvElemDescriptor), dimensions, dt, elemDt);}
        }
        return null;
    }

    private static PropertyContainer deserialiseCluster(ByteBuffer buffer, LVClusterDescriptor lvClusterDescriptor){
        LVTypeDescriptor[] lvTypeDescriptors = lvClusterDescriptor.getTypeDescs();
        JavaContainer<?>[] javaContainers = new JavaContainer[lvTypeDescriptors.length];
        for(int i = 0; i< lvTypeDescriptors.length; i++){
            javaContainers[i] = deserialise(buffer, lvTypeDescriptors[i]);
        }
        return new PropertyContainer(lvClusterDescriptor.getFieldName(), javaContainers, DataType.DT_DATA);
    }

    private static PropertyContainer[] getClusterArr(ByteBuffer buffer, int len, LVClusterDescriptor lvClusterDescriptor) {
        PropertyContainer[] propertyContainers = new PropertyContainer[len];
        for(int i = 0; i<len; i++){
            propertyContainers[i] = deserialiseCluster(buffer, lvClusterDescriptor);
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
        int dim = lvArrayDescriptor.getArrayDim();
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
