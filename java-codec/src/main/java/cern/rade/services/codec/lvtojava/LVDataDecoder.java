package cern.rade.services.codec.lvtojava;

import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.java.*;
import cern.rade.services.common.containers.labview.LVType;
import cern.rade.services.common.containers.labview.LVArrayDescriptor;
import cern.rade.services.common.containers.labview.LVClusterDescriptor;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;
//import org.checkerframework.checker.units.qual.C;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Objects;

public final class LVDataDecoder {

    public static DataContainer decode(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor) throws CodecException {
        try{
            return decodeInternal(buffer, lvTypeDescriptor);
        } catch(BufferUnderflowException e ){
            throw new CodecException("Exception while decoding LabVIEW data, buffer underflow occurred while reading data. Check your type descriptor is correct and that you are providing the correct data.", e);
        } catch(ContainerException e){
            throw new CodecException("Exception while trying to set data in DataContainer, the type being set did not match the type specified in the DataContainer.", e);
        }
    }

    private static DataContainer decodeInternal(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor) throws BufferUnderflowException, ContainerException, CodecException {
        if (Objects.requireNonNull(lvTypeDescriptor.getType()) == LVType.CLUSTER) {
            return decodeCluster(buffer, (LVClusterDescriptor) lvTypeDescriptor);
        } else {
            return decodeSingle(buffer, lvTypeDescriptor);
        }
    }

    private static FieldContainer decodeSingle(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor) throws BufferUnderflowException, ContainerException, CodecException {
        FieldContainer field = new FieldContainer(lvTypeDescriptor.getName());
        switch(lvTypeDescriptor.getType()){
            case INT_8, BOOL, INT_16, INT_32, INT_64, SGL, DBL, STRING -> {decodeScalar(buffer, field, lvTypeDescriptor);}
            case ARRAY -> {
                decodeArray(buffer, field, (LVArrayDescriptor) lvTypeDescriptor);}
            default -> throw new CodecException("Decoding simple which is not of single type.");
        }
        return field;
    }

    private static void decodeScalar(ByteBuffer buffer, FieldContainer field, LVTypeDescriptor lvTypeDescriptor){
        switch(lvTypeDescriptor.getType()) {
            case INT_8 -> {
                field.setByte(buffer.get());
            }
            case BOOL -> {
                field.setBoolean(getBool(buffer));
            }
            case INT_16 -> {
                field.setShort(buffer.getShort());
            }
            case INT_32 -> {
                field.setInt(buffer.getInt());
            }
            case INT_64 -> {
                field.setLong(buffer.getLong());
            }
            case SGL -> {
                field.setFloat(buffer.getFloat());
            }
            case DBL -> {
                field.setDouble(buffer.getDouble());
            }
            case STRING -> {
                field.setString(getString(buffer));
            }
        }
    }

    private static void decodeArray(ByteBuffer buffer, FieldContainer field, LVArrayDescriptor lvArrayDescriptor) throws CodecException {
        LVTypeDescriptor lvElemDescriptor = lvArrayDescriptor.getElemDescriptor();
        int[] dimensions = getDims(buffer, lvArrayDescriptor);
        int len = getArrLen(dimensions);
        LVType lvType = lvArrayDescriptor.getElemDescriptor().getType();
        if (dimensions.length == 1){
            switch(lvType){
                case INT_8   -> { field.setBytes(getByteArr(buffer, len)); }
                case BOOL    -> { field.setBooleans(getBoolArr(buffer, len)); }
                case INT_16  -> { field.setShorts(getShortArr(buffer, len)); }
                case INT_32  -> { field.setInts(getIntArr(buffer, len)); }
                case INT_64  -> { field.setLongs(getLongArr(buffer, len)); }
                case SGL     -> { field.setFloats(getFloatArr(buffer, len)); }
                case DBL     -> { field.setDoubles(getDoubleArr(buffer, len)); }
                case STRING  -> { field.setStrings(getStringArr(buffer, len)); }
                default -> throw new CodecException("Array element type: " + lvType + " is not supported");
            }
        }
        else if (dimensions.length == 2){
            switch(lvType) {
                case INT_8   -> { field.setBytes2D(getByteArr(buffer, len), dimensions); }
                case BOOL    -> { field.setBooleans2D(getBoolArr(buffer, len), dimensions); }
                case INT_16  -> { field.setShorts2D(getShortArr(buffer, len), dimensions); }
                case INT_32  -> { field.setInts2D(getIntArr(buffer, len), dimensions); }
                case INT_64  -> { field.setLongs2D(getLongArr(buffer, len), dimensions); }
                case SGL     -> { field.setFloats2D(getFloatArr(buffer, len), dimensions); }
                case DBL     -> { field.setDoubles2D(getDoubleArr(buffer, len), dimensions); }
                case STRING  -> { field.setStrings2D(getStringArr(buffer, len), dimensions); }
                default -> throw new CodecException("Array element type: " + lvType + " is not supported");
            }
        }
        else{
            throw new CodecException("Unsupported array dimension (>2)");
        }
    }
//
//    private static FieldContainer decodeArray(ByteBuffer buffer, FieldContainer field, LVArrayDescriptor lvArrayDescriptor) throws BufferUnderflowException, ContainerException, CodecException {
//        LVTypeDescriptor lvElemDescriptor = lvArrayDescriptor.getElemDescriptor();
//        int[] dimensions = getDims(buffer, lvArrayDescriptor);
//        int len = getArrLen(dimensions);
//
//        switch(lvArrayDescriptor.getElemDescriptor()){
//            case INT_8 -> {arrayContainer.setData(getByteArr(buffer, len), dimensions);}
//            case DT_BOOL -> {arrayContainer.setData(getBoolArr(buffer, len), dimensions);}
//            case DT_SHORT -> {arrayContainer.setData(getShortArr(buffer, len), dimensions);}
//            case DT_INT -> {arrayContainer.setData(getIntArr(buffer, len), dimensions);}
//            case DT_LONG-> {arrayContainer.setData(getLongArr(buffer, len), dimensions);}
//            case DT_FLOAT -> {arrayContainer.setData(getFloatArr(buffer, len), dimensions);}
//            case DT_DOUBLE -> {arrayContainer.setData(getDoubleArr(buffer, len), dimensions);}
//            case DT_STRING -> {arrayContainer.setData(getStringArr(buffer, len), dimensions);}
//            case DT_DATA -> {arrayContainer.setData(getClusterArr(buffer, len, (LVClusterDescriptor) lvElemDescriptor), dimensions);}
//            case DT_DISCRETE_FUNCTION -> {throw new CodecException("Unimplemented type.");}//TODO: implement DF arrays
//            default -> throw new CodecException("Array element type: " + arrayContainer.getElemType() + " is not supported");
//        }
//    }

    private static PropertyContainer decodeCluster(ByteBuffer buffer, LVClusterDescriptor lvClusterDescriptor) throws BufferUnderflowException, ContainerException, CodecException {
        PropertyContainer propertyContainer = new PropertyContainer(lvClusterDescriptor.getName());
        LVTypeDescriptor[] lvTypeDescriptors = lvClusterDescriptor.getFieldDescriptors();
        for (LVTypeDescriptor lvTypeDescriptor : lvTypeDescriptors) {
            DataContainer field = decodeInternal(buffer, lvTypeDescriptor);
            propertyContainer.put(field);
        }
        return propertyContainer;
    }

//    private static PropertyContainer[] getClusterArr(ByteBuffer buffer, int len, LVClusterDescriptor lvClusterDescriptor) throws BufferUnderflowException, ContainerException, CodecException {
//        PropertyContainer[] propertyContainers = LVDataContainerFactory.createPropertyContainerArray(lvClusterDescriptor, len);
//        for(int i = 0; i<len; i++){
//            decodeCluster(buffer, propertyContainers[i], lvClusterDescriptor);
//        }
//        return propertyContainers;
//    }

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
