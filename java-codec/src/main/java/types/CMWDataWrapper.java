package types;

import cern.cmw.data.Data;
import cern.cmw.data.DataFactory;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.InvocationTargetException;

public class CMWDataWrapper {
    Data cmwData;
    public CMWDataWrapper(){
        cmwData = DataFactory.createData();
    }

    public void appendByte(JavaContainer<Byte> javaContainer){
        cmwData.append(javaContainer.getFieldName(), javaContainer.getData());
    }
    public void appendBool(JavaContainer<Boolean> javaContainer){
        cmwData.append(javaContainer.getFieldName(), javaContainer.getData());
    }
    public void appendShort(JavaContainer<Short> javaContainer){
        cmwData.append(javaContainer.getFieldName(), javaContainer.getData());
    }
    public void appendInt(JavaContainer<Integer> javaContainer){
        cmwData.append(javaContainer.getFieldName(), javaContainer.getData());
    }
    public void appendFloat(JavaContainer<Float> javaContainer){
        cmwData.append(javaContainer.getFieldName(), javaContainer.getData());
    }

    public void appendDouble(JavaContainer<Double> javaContainer){
        cmwData.append(javaContainer.getFieldName(), javaContainer.getData());
    }

    public void appendLong(JavaContainer<Long> javaContainer){
        cmwData.append(javaContainer.getFieldName(), javaContainer.getData());
    }

    public void appendString(JavaContainer<String> javaContainer){
        cmwData.append(javaContainer.getFieldName(), javaContainer.getData());
    }
    public void appendProperty(PropertyContainer propertyContainer){
        Data data = DataFactory.createData();

        cmwData.append(javaContainer.getFieldName(), javaContainer.getData());
    }
    public void appendByteArray(ArrayContainer<Byte> arrayContainer) throws InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        int[] dimensions = arrayContainer.getDimensions();

        byte[] arr = ArrayUtils.toPrimitive(arrayContainer.getData());
        if(dimensions==null){cmwData.appendArray(arrayContainer.getFieldName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getFieldName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getFieldName(), arr, dimensions);}
    }

    public void appendBoolArray(ArrayContainer<Boolean> arrayContainer) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        int[] dimensions = arrayContainer.getDimensions();
        boolean[] arr = ArrayUtils.toPrimitive(arrayContainer.getData());
        if(dimensions==null){cmwData.appendArray(arrayContainer.getFieldName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getFieldName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getFieldName(), arr, dimensions);}
    }

    public void appendShortArray(ArrayContainer<Short> arrayContainer) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        int[] dimensions = arrayContainer.getDimensions();
        short[] arr = ArrayUtils.toPrimitive(arrayContainer.getData());
        if(dimensions==null){cmwData.appendArray(arrayContainer.getFieldName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getFieldName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getFieldName(), arr, dimensions);}
    }

    public void appendIntArray(ArrayContainer<Integer> arrayContainer) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        int[] dimensions = arrayContainer.getDimensions();
        int[] arr = ArrayUtils.toPrimitive(arrayContainer.getData());
        if(dimensions==null){cmwData.appendArray(arrayContainer.getFieldName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getFieldName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getFieldName(), arr, dimensions);}
    }

    public void appendFloatArray(ArrayContainer<Float> arrayContainer) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        int[] dimensions = arrayContainer.getDimensions();
        float[] arr = ArrayUtils.toPrimitive(arrayContainer.getData());
        if(dimensions==null){cmwData.appendArray(arrayContainer.getFieldName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getFieldName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getFieldName(), arr, dimensions);}
    }

    public void appendDoubleArray(ArrayContainer<Double> arrayContainer) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        int[] dimensions = arrayContainer.getDimensions();
        double[] arr = ArrayUtils.toPrimitive(arrayContainer.getData());
        if(dimensions==null){cmwData.appendArray(arrayContainer.getFieldName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getFieldName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getFieldName(), arr, dimensions);}
    }
    public void appendLongArray(ArrayContainer<Long> arrayContainer) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        int[] dimensions = arrayContainer.getDimensions();
        long[] arr = ArrayUtils.toPrimitive(arrayContainer.getData());
        if(dimensions==null){cmwData.appendArray(arrayContainer.getFieldName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getFieldName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getFieldName(), arr, dimensions);}
    }
    public void appendStringArray(ArrayContainer<String> arrayContainer) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        int[] dimensions = arrayContainer.getDimensions();
        String[] arr = arrayContainer.getData();
        if(dimensions==null){cmwData.appendArray(arrayContainer.getFieldName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getFieldName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getFieldName(), arr, dimensions);}
    }
    public void appendDataArray(ArrayContainer<PropertyContainer> arrayContainer) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        int[] dimensions = arrayContainer.getDimensions();
        PropertyContainer[] arr = arrayContainer.getData();
        for (PropertyContainer propertyContainer : arr){

        }

        if(dimensions==null){cmwData.appendArray(arrayContainer.getFieldName(), (Data[]) arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getFieldName(), (Data[]) arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getFieldName(), (Data[]) arr, dimensions);}
    }

}
