package types;

import cern.cmw.data.Data;
import cern.cmw.data.DataFactory;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.InvocationTargetException;

public class CMWDataWrapper {
    Data cmwData;
    public CMWDataWrapper(){
        cmwData = DataFactory.createData();
    }

    private void appendByte(JavaContainer<Byte> javaContainer){
        cmwData.append(javaContainer.getFieldName(), javaContainer.getData());
    }
    private void appendBool(JavaContainer<Boolean> javaContainer){
        cmwData.append(javaContainer.getFieldName(), javaContainer.getData());
    }
    private void appendShort(JavaContainer<Short> javaContainer){
        cmwData.append(javaContainer.getFieldName(), javaContainer.getData());
    }
    private void appendInt(JavaContainer<Integer> javaContainer){
        cmwData.append(javaContainer.getFieldName(), javaContainer.getData());
    }
    private void appendFloat(JavaContainer<Float> javaContainer){
        cmwData.append(javaContainer.getFieldName(), javaContainer.getData());
    }

    private void appendDouble(JavaContainer<Double> javaContainer){
        cmwData.append(javaContainer.getFieldName(), javaContainer.getData());
    }

    private void appendLong(JavaContainer<Long> javaContainer){
        cmwData.append(javaContainer.getFieldName(), javaContainer.getData());
    }

    private void appendString(JavaContainer<String> javaContainer){
        cmwData.append(javaContainer.getFieldName(), javaContainer.getData());
    }
    private void appendByteArray(ArrayContainer<Byte> arrayContainer) throws InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        int[] dimensions = arrayContainer.getDimensions();

        byte[] arr = ArrayUtils.toPrimitive(arrayContainer.getData());
        if(dimensions==null){cmwData.appendArray(arrayContainer.getFieldName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getFieldName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getFieldName(), arr, dimensions);}
    }

    private void appendBoolArray(ArrayContainer<Boolean> arrayContainer) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        int[] dimensions = arrayContainer.getDimensions();
        boolean[] arr = ArrayUtils.toPrimitive(arrayContainer.getData());
        if(dimensions==null){cmwData.appendArray(arrayContainer.getFieldName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getFieldName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getFieldName(), arr, dimensions);}
    }

    private void appendShortArray(ArrayContainer<Short> arrayContainer) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        int[] dimensions = arrayContainer.getDimensions();
        short[] arr = ArrayUtils.toPrimitive(arrayContainer.getData());
        if(dimensions==null){cmwData.appendArray(arrayContainer.getFieldName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getFieldName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getFieldName(), arr, dimensions);}
    }

    private void appendIntArray(ArrayContainer<Integer> arrayContainer) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        int[] dimensions = arrayContainer.getDimensions();
        int[] arr = ArrayUtils.toPrimitive(arrayContainer.getData());
        if(dimensions==null){cmwData.appendArray(arrayContainer.getFieldName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getFieldName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getFieldName(), arr, dimensions);}
    }

    private void appendFloatArray(ArrayContainer<Float> arrayContainer) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        int[] dimensions = arrayContainer.getDimensions();
        float[] arr = ArrayUtils.toPrimitive(arrayContainer.getData());
        if(dimensions==null){cmwData.appendArray(arrayContainer.getFieldName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getFieldName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getFieldName(), arr, dimensions);}
    }

    private void appendDoubleArray(ArrayContainer<Double> arrayContainer) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        int[] dimensions = arrayContainer.getDimensions();
        double[] arr = ArrayUtils.toPrimitive(arrayContainer.getData());
        if(dimensions==null){cmwData.appendArray(arrayContainer.getFieldName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getFieldName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getFieldName(), arr, dimensions);}
    }
    private void appendLongArray(ArrayContainer<Long> arrayContainer) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        int[] dimensions = arrayContainer.getDimensions();
        long[] arr = ArrayUtils.toPrimitive(arrayContainer.getData());
        if(dimensions==null){cmwData.appendArray(arrayContainer.getFieldName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getFieldName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getFieldName(), arr, dimensions);}
    }
    private void appendStringArray(ArrayContainer<String> arrayContainer) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        int[] dimensions = arrayContainer.getDimensions();
        String[] arr = arrayContainer.getData();
        if(dimensions==null){cmwData.appendArray(arrayContainer.getFieldName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(arrayContainer.getFieldName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(arrayContainer.getFieldName(), arr, dimensions);}
    }
    private void appendDataArray(PropertyArrayContainer propertyArrayContainer) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        int[] dimensions = propertyArrayContainer.getDimensions();
        PropertyContainer[] arr = propertyArrayContainer.getData();

        if(dimensions==null){cmwData.appendArray(propertyArrayContainer.getFieldName(), arr);}
        else if(dimensions.length==2){cmwData.appendArray2D(propertyArrayContainer.getFieldName(), arr, dimensions[0], dimensions[1]);}
        else{cmwData.appendMultiArray(propertyArrayContainer.getFieldName(), arr, dimensions);}
    }

}
