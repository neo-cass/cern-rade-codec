package cern.rade.services.codec.labview;

import cern.rade.services.codec.CodecException;
import cern.japc.core.factory.MapParameterValueFactory;
import cern.japc.core.factory.SimpleParameterValueFactory;
import cern.japc.value.MapParameterValue;
import cern.japc.value.ParameterValue;
import cern.japc.value.SimpleParameterValue;
import cern.rade.services.common.containers.labview.LVType;
import cern.rade.services.common.containers.labview.LVArrayDescriptor;
import cern.rade.services.common.containers.labview.LVClusterDescriptor;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Objects;

public final class LVDataDecoder {

    public static ParameterValue decode(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor) throws CodecException {
        try{
            return decodeInternal(buffer, lvTypeDescriptor);
        } catch(BufferUnderflowException e ){
            throw new CodecException("Exception while decoding LabVIEW data, buffer underflow occurred while reading data. Check your type descriptor is correct and that you are providing the correct data.", e);
        }
    }

    private static ParameterValue decodeInternal(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor) throws BufferUnderflowException, CodecException {
        if (Objects.requireNonNull(lvTypeDescriptor.getType()) == LVType.CLUSTER) {
            return decodeCluster(buffer, (LVClusterDescriptor) lvTypeDescriptor);
        } else {
            return decodeSingle(buffer, lvTypeDescriptor);
        }
    }

    private static SimpleParameterValue decodeSingle(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor) throws BufferUnderflowException, CodecException {
        return switch(lvTypeDescriptor.getType()){
            case INT_8, BOOL, INT_16, INT_32, INT_64, SGL, DBL, STRING -> SimpleParameterValueFactory.newSimpleParameterValue(decodeScalar(buffer, lvTypeDescriptor.getType()));
            case ARRAY -> decodeArray(buffer, (LVArrayDescriptor) lvTypeDescriptor);
            default -> throw new CodecException("Decoding simple which is not of single type.");
        };
    }

    private static Object decodeScalar(ByteBuffer buffer, LVType lvType) throws CodecException {
        return switch(lvType) {
            case INT_8 -> buffer.get();
            case BOOL -> getBool(buffer);
            case INT_16 -> buffer.getShort();
            case INT_32 -> buffer.getInt();
            case INT_64 -> buffer.getLong();
            case SGL -> buffer.getFloat();
            case DBL -> buffer.getDouble();
            case STRING -> getString(buffer);
            default -> throw new CodecException("Scalar type: " + lvType + " is not supported");
        };
    }

    private static SimpleParameterValue decodeArray(ByteBuffer buffer, LVArrayDescriptor lvArrayDescriptor) throws CodecException {
        int[] dimensions = getDims(buffer, lvArrayDescriptor);
        int len = getArrLen(dimensions);
        LVType lvType = lvArrayDescriptor.getElemDescriptor().getType();
        Object arr = switch(lvType){
            case INT_8   -> getByteArr(buffer, len);
            case BOOL    -> getBoolArr(buffer, len);
            case INT_16  -> getShortArr(buffer, len);
            case INT_32  -> getIntArr(buffer, len);
            case INT_64  -> getLongArr(buffer, len);
            case SGL     -> getFloatArr(buffer, len);
            case DBL     -> getDoubleArr(buffer, len);
            case STRING  -> getStringArr(buffer, len);
            default -> throw new CodecException("Array element type: " + lvType + " is not supported");
        };
        return switch(dimensions.length){
            case 1 -> SimpleParameterValueFactory.newSimpleParameterValue(arr);
            case 2 -> SimpleParameterValueFactory.newSimpleParameterValue(arr, dimensions);
            default -> throw new CodecException("Unsupported array dimension (>2)");
        };
    }

    private static MapParameterValue decodeCluster(ByteBuffer buffer, LVClusterDescriptor lvClusterDescriptor) throws BufferUnderflowException, CodecException {
        MapParameterValue map = MapParameterValueFactory.newMapParameterValue();
        for (LVTypeDescriptor lvTypeDescriptor : lvClusterDescriptor.getFieldDescriptors()) {
            map.put(lvTypeDescriptor.getName(), decodeSingle(buffer, lvTypeDescriptor));
        }
        return map;
    }

    private static byte[] getByteArr(ByteBuffer buffer, int len) throws BufferUnderflowException{
        byte[] arr = new byte[len];
        buffer.get(arr);
        return arr;
    }

    private static boolean[] getBoolArr(ByteBuffer buffer, int len)throws BufferUnderflowException{
        boolean[] arr = new boolean[len];
        for(int i = 0; i<len; i++) {arr[i] = getBool(buffer);}
        return arr;
    }

    private static short[] getShortArr(ByteBuffer buffer, int len) throws BufferUnderflowException{
        short[] arr = new short[len];
        for(int i = 0; i<len; i++) {arr[i] = buffer.getShort();}
        return arr;
    }

    private static int[] getIntArr(ByteBuffer buffer, int len)throws BufferUnderflowException{
        int[] arr = new int[len];
        for(int i = 0; i<len; i++) {arr[i] = buffer.getInt();}
        return arr;
    }

    private static float[] getFloatArr(ByteBuffer buffer, int len)throws BufferUnderflowException{
        float[] arr = new float[len];
        for(int i = 0; i<len; i++) {arr[i] = buffer.getFloat();}
        return arr;
    }

    private static double[] getDoubleArr(ByteBuffer buffer, int len)throws BufferUnderflowException{
        double[] arr = new double[len];
        for(int i = 0; i<len; i++) {arr[i] = buffer.getDouble();}
        return arr;
    }
    private static long[] getLongArr(ByteBuffer buffer, int len)throws BufferUnderflowException{
        long[] arr = new long[len];
        for(int i = 0; i<len; i++) {arr[i] = buffer.getLong();}
        return arr;
    }
    private static String[] getStringArr(ByteBuffer buffer, int len)throws BufferUnderflowException{
        String[] arr = new String[len];
        for(int i = 0; i<len; i++) {arr[i] = getString(buffer);}
        return arr;
    }
    private static String getString(ByteBuffer buffer)throws BufferUnderflowException{
        int stringLen = buffer.getInt();
        byte[] rawString = new byte[stringLen];
        buffer.get(rawString);
        return new String(rawString);
    }

    private static boolean getBool(ByteBuffer buffer)throws BufferUnderflowException{
        byte dataByte = buffer.get();
        return (((int)dataByte) == 1);
    }

    private static int[] getDims(ByteBuffer buffer, LVArrayDescriptor lvArrayDescriptor)throws BufferUnderflowException{
        int dim = lvArrayDescriptor.getDimension();
        int[] dimensions = new int[dim];
        for (int i=0; i<dim; i++) {dimensions[i] = buffer.getInt();}
        return dimensions;
    }

    private static int getArrLen(int[] dimensions) {
        int len = 1;
        for (int dimension : dimensions) {len *= dimension;}
        return len;
    }
}
