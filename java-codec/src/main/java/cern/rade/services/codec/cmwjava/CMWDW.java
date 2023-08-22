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
    public void appendByte(DataContainer DataContainer){
        cmwData.append(DataContainer.getName(), (byte) DataContainer.getData());
    }
    public void appendBool(DataContainer DataContainer){
        cmwData.append(DataContainer.getName(), (boolean) DataContainer.getData());
    }
    public void appendShort(DataContainer DataContainer){
        cmwData.append(DataContainer.getName(), (short) DataContainer.getData());
    }
    public void appendInt(DataContainer DataContainer){
        cmwData.append(DataContainer.getName(), (int) DataContainer.getData());
    }
    public void appendFloat(DataContainer DataContainer){
        cmwData.append(DataContainer.getName(), (float) DataContainer.getData());
    }
    public void appendDouble(DataContainer DataContainer){
        cmwData.append(DataContainer.getName(), (boolean) DataContainer.getData());
    }

    public void appendLong(DataContainer DataContainer){
        cmwData.append(DataContainer.getName(), (long) DataContainer.getData());
    }
    public void appendString(DataContainer DataContainer){
        cmwData.append(DataContainer.getName(), (boolean) DataContainer.getData());
    }
    public void appendProperty(PropertyContainer propertyContainer){
        cmwData.append(propertyContainer.getName(), encodeProperty(propertyContainer));
    }
    private Data encodeProperty(PropertyContainer propertyContainer){
        return CMWDEncoder.encode(propertyContainer);
    }
    public void appendByteArray(ArrayContainer arrayContainer) {
        int[] dimensions = arrayContainer.getDimensions();
        byte[] arr = ArrayUtils.toPrimitive((Byte[]) arrayContainer.getData());
        if(dimensions.length==1){cmwData.appendArray(arrayContainer.getName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getName(), arr, dimensions);}
    }

    public void appendBoolArray(ArrayContainer arrayContainer) {
        int[] dimensions = arrayContainer.getDimensions();
        boolean[] arr = ArrayUtils.toPrimitive((Boolean[]) arrayContainer.getData());
        if(dimensions.length==1){cmwData.appendArray(arrayContainer.getName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getName(), arr, dimensions);}
    }

    public void appendShortArray(ArrayContainer arrayContainer){
        int[] dimensions = arrayContainer.getDimensions();
        short[] arr = ArrayUtils.toPrimitive((Short[]) arrayContainer.getData());
        if(dimensions.length==1){cmwData.appendArray(arrayContainer.getName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getName(), arr, dimensions);}
    }

    public void appendIntArray(ArrayContainer arrayContainer)  {
        int[] dimensions = arrayContainer.getDimensions();
        int[] arr = ArrayUtils.toPrimitive((Integer[]) arrayContainer.getData());
        if(dimensions.length==1){cmwData.appendArray(arrayContainer.getName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getName(), arr, dimensions);}
    }

    public void appendFloatArray(ArrayContainer arrayContainer){
        int[] dimensions = arrayContainer.getDimensions();
        float[] arr = ArrayUtils.toPrimitive((Float[])arrayContainer.getData());
        if(dimensions.length==1){cmwData.appendArray(arrayContainer.getName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getName(), arr, dimensions);}
    }

    public void appendDoubleArray(ArrayContainer arrayContainer){
        int[] dimensions = arrayContainer.getDimensions();
        double[] arr = ArrayUtils.toPrimitive((Double[])arrayContainer.getData());
        if(dimensions.length==1){cmwData.appendArray(arrayContainer.getName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getName(), arr, dimensions);}
    }
    public void appendLongArray(ArrayContainer arrayContainer){
        int[] dimensions = arrayContainer.getDimensions();
        long[] arr = ArrayUtils.toPrimitive((Long[])arrayContainer.getData());
        if(dimensions.length==1){cmwData.appendArray(arrayContainer.getName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getName(), arr, dimensions);}
    }
    public void appendStringArray(ArrayContainer arrayContainer){
        int[] dimensions = arrayContainer.getDimensions();
        String[] arr = (String[]) arrayContainer.getData();
        if(dimensions.length==1){cmwData.appendArray(arrayContainer.getName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getName(), arr, dimensions);}
    }
    public void appendDataArray(ArrayContainer arrayContainer) {
        int[] dimensions = arrayContainer.getDimensions();
        PropertyContainer[] arr = (PropertyContainer[]) arrayContainer.getData();
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
