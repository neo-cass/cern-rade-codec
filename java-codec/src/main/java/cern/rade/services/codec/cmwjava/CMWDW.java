package cern.rade.services.codec.cmwjava;

import cern.cmw.data.Data;
import cern.cmw.data.DataFactory;
import org.apache.commons.lang3.ArrayUtils;
import cern.rade.services.common.containers.java.ArrayContainer;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.java.PropertyContainer;

/**
 * This class provides an abstracted view of {@link Data} which allows you to append the internal {@link cern.rade.services.common.containers.java}s.
 */
public class CMWDW {
    /**
     * cmwData is the actual raw CMW data which we are wrapping with this class.
     */
    private Data cmwData;
    public CMWDW(){cmwData = DataFactory.createData();}
    public Data getCmwData() {return cmwData;}
    public void appendByte(DataContainer<Byte> DataContainer){
        cmwData.append(DataContainer.getName(), DataContainer.getData());
    }
    public void appendBool(DataContainer<Boolean> DataContainer){
        cmwData.append(DataContainer.getName(), DataContainer.getData());
    }
    public void appendShort(DataContainer<Short> DataContainer){
        cmwData.append(DataContainer.getName(), DataContainer.getData());
    }
    public void appendInt(DataContainer<Integer> DataContainer){
        cmwData.append(DataContainer.getName(), DataContainer.getData());
    }
    public void appendFloat(DataContainer<Float> DataContainer){
        cmwData.append(DataContainer.getName(), DataContainer.getData());
    }
    public void appendDouble(DataContainer<Double> DataContainer){
        cmwData.append(DataContainer.getName(), DataContainer.getData());
    }

    public void appendLong(DataContainer<Long> DataContainer){
        cmwData.append(DataContainer.getName(), DataContainer.getData());
    }
    public void appendString(DataContainer<String> DataContainer){
        cmwData.append(DataContainer.getName(), DataContainer.getData());
    }
    public void appendProperty(PropertyContainer propertyContainer){
        cmwData.append(propertyContainer.getName(), encodeProperty(propertyContainer));
    }
    private Data encodeProperty(PropertyContainer propertyContainer){
        return CMWDEncoder.encode(propertyContainer);
    }
    public void appendByteArray(ArrayContainer<Byte> arrayContainer) {
        int[] dimensions = arrayContainer.getDimensions();
        byte[] arr = ArrayUtils.toPrimitive(arrayContainer.getData());
        if(dimensions.length==1){cmwData.appendArray(arrayContainer.getName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getName(), arr, dimensions);}
    }

    public void appendBoolArray(ArrayContainer<Boolean> arrayContainer) {
        int[] dimensions = arrayContainer.getDimensions();
        boolean[] arr = ArrayUtils.toPrimitive(arrayContainer.getData());
        if(dimensions.length==1){cmwData.appendArray(arrayContainer.getName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getName(), arr, dimensions);}
    }

    public void appendShortArray(ArrayContainer<Short> arrayContainer){
        int[] dimensions = arrayContainer.getDimensions();
        short[] arr = ArrayUtils.toPrimitive(arrayContainer.getData());
        if(dimensions.length==1){cmwData.appendArray(arrayContainer.getName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getName(), arr, dimensions);}
    }

    public void appendIntArray(ArrayContainer<Integer> arrayContainer)  {
        int[] dimensions = arrayContainer.getDimensions();
        int[] arr = ArrayUtils.toPrimitive(arrayContainer.getData());
        if(dimensions.length==1){cmwData.appendArray(arrayContainer.getName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getName(), arr, dimensions);}
    }

    public void appendFloatArray(ArrayContainer<Float> arrayContainer){
        int[] dimensions = arrayContainer.getDimensions();
        float[] arr = ArrayUtils.toPrimitive(arrayContainer.getData());
        if(dimensions.length==1){cmwData.appendArray(arrayContainer.getName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getName(), arr, dimensions);}
    }

    public void appendDoubleArray(ArrayContainer<Double> arrayContainer){
        int[] dimensions = arrayContainer.getDimensions();
        double[] arr = ArrayUtils.toPrimitive(arrayContainer.getData());
        if(dimensions.length==1){cmwData.appendArray(arrayContainer.getName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getName(), arr, dimensions);}
    }
    public void appendLongArray(ArrayContainer<Long> arrayContainer){
        int[] dimensions = arrayContainer.getDimensions();
        long[] arr = ArrayUtils.toPrimitive(arrayContainer.getData());
        if(dimensions.length==1){cmwData.appendArray(arrayContainer.getName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getName(), arr, dimensions);}
    }
    public void appendStringArray(ArrayContainer<String> arrayContainer){
        int[] dimensions = arrayContainer.getDimensions();
        String[] arr = arrayContainer.getData();
        if(dimensions.length==1){cmwData.appendArray(arrayContainer.getName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getName(), arr, dimensions);}
    }
    public void appendDataArray(ArrayContainer<PropertyContainer> arrayContainer) {
        int[] dimensions = arrayContainer.getDimensions();
        PropertyContainer[] arr = arrayContainer.getData();
        Data[] datum = new Data[arr.length];
        for (int i =0; i< arr.length; i++){
            //this is the reason this class is in the codec and not in common
            //otherwise common would pull from codec and codec would pull from common
            datum[i] = encodeProperty(arr[i]);
        }
        if(dimensions.length==1){cmwData.appendArray(arrayContainer.getName(), datum);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getName(), datum, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getName(), datum, dimensions);}
    }
}
