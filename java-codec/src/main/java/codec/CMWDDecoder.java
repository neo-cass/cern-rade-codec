package codec;

import cern.cmw.data.*;
import org.apache.commons.lang3.ArrayUtils;
import types.ArrayContainer;
import types.JavaContainer;
import types.PropertyContainer;

import java.util.ArrayList;

public final class CMWDDecoder {

    public static JavaContainer<?> decode(Data cmwData){
        ArrayList<Entry> entries = (ArrayList<Entry>) cmwData.getEntries();
        if (entries.size()==1){
            return decodeEntry(entries.get(0));
        }
        else{
            return decodeData(cmwData, "CMW Data");
        }
    }

    public static JavaContainer<?> decode(Data cmwData, PropertyContainer propertyContainer){
        ArrayList<Entry> entries = (ArrayList<Entry>) cmwData.getEntries();
        if (entries.size()==1){
            return decodeEntry(entries.get(0));
        }
        else{
            return decodeData(cmwData, "CMW Data");
        }
    }

   private static PropertyContainer decodeData(Data cmwData, String fieldName){
        ArrayList<Entry> entries = (ArrayList<Entry>) cmwData.getEntries();
        int len = entries.size();
        JavaContainer<?>[] javaContainers  = new JavaContainer[len];

        for (int i = 0; i<len; i++){
            javaContainers[i] = decodeEntry(entries.get(i));
        }
        return new PropertyContainer(fieldName, javaContainers, DataType.DT_DATA);
    }



    private static JavaContainer<?> decodeEntry(Entry entry){
        DataType type = entry.getType();
        switch(type){
            case DT_BYTE -> {return new JavaContainer<>(entry.getName(), entry.getByte(), type);}
            case DT_BOOL ->{return new JavaContainer<>(entry.getName(), entry.getBool(), type);}
            case DT_SHORT -> {return new JavaContainer<>(entry.getName(), entry.getShort(), type);}
            case DT_INT -> {return new JavaContainer<>(entry.getName(), entry.getInt(), type);}
            case DT_LONG -> {return new JavaContainer<>(entry.getName(), entry.getLong(), type);}
            case DT_FLOAT -> {return new JavaContainer<>(entry.getName(), entry.getFloat(), type);}
            case DT_DOUBLE -> {return new JavaContainer<>(entry.getName(), entry.getDouble(), type);}
            case DT_STRING -> {return new JavaContainer<>(entry.getName(), entry.getString(), type);}
            case DT_DATA -> {return decodeData(entry.getData(), entry.getName());}
            case DT_BOOL_ARRAY, DT_BYTE_ARRAY, DT_DOUBLE_ARRAY, DT_FLOAT_ARRAY, DT_STRING_ARRAY, DT_INT_ARRAY, DT_SHORT_ARRAY, DT_LONG_ARRAY,
                    DT_BOOL_ARRAY_2D, DT_BYTE_ARRAY_2D, DT_DOUBLE_ARRAY_2D, DT_FLOAT_ARRAY_2D, DT_STRING_ARRAY_2D, DT_INT_ARRAY_2D, DT_SHORT_ARRAY_2D, DT_LONG_ARRAY_2D,
                    DT_BOOL_MULTI_ARRAY, DT_BYTE_MULTI_ARRAY, DT_DOUBLE_MULTI_ARRAY, DT_FLOAT_MULTI_ARRAY, DT_STRING_MULTI_ARRAY, DT_INT_MULTI_ARRAY, DT_SHORT_MULTI_ARRAY, DT_LONG_MULTI_ARRAY -> {return decodeNDArray(entry);}
        }
        return null;
    }

    private static PropertyContainer[] getPropertyArray(Data[] data){
        int len = data.length;
        PropertyContainer[] propertyContainers = new PropertyContainer[len];
        for(int i = 0; i<len; i++){
            propertyContainers[i] = decodeData(data[i], "CMW Data");
        }
        return propertyContainers;
    }

    private static ArrayContainer<?> decodeNDArray(Entry entry){
        DataType type = entry.getType();
        switch(type){
            case DT_BYTE_ARRAY -> {Byte[] arr = ArrayUtils.toObject(entry.getByteArray()); return new ArrayContainer<>(entry.getName(), "Byte", arr, new int[]{arr.length}, DataType.DT_BYTE_ARRAY, DataType.DT_BYTE);}
            case DT_BOOL_ARRAY -> {Boolean[] arr = ArrayUtils.toObject(entry.getBoolArray());return new ArrayContainer<>(entry.getName(), "Bool", arr, new int[]{arr.length}, DataType.DT_BOOL_ARRAY, DataType.DT_BOOL);}
            case DT_SHORT_ARRAY -> {Short[] arr = ArrayUtils.toObject(entry.getShortArray()); return new ArrayContainer<>(entry.getName(), "Short", arr, new int[]{arr.length}, DataType.DT_SHORT_ARRAY, DataType.DT_SHORT);}
            case DT_INT_ARRAY -> {Integer[] arr = ArrayUtils.toObject(entry.getIntArray()); return new ArrayContainer<>(entry.getName(), "Int", arr, new int[]{arr.length}, DataType.DT_INT_ARRAY, DataType.DT_INT);}
            case DT_LONG_ARRAY -> {Long[] arr = ArrayUtils.toObject(entry.getLongArray()); return new ArrayContainer<>(entry.getName(), "Long", arr, new int[]{arr.length}, DataType.DT_LONG_ARRAY, DataType.DT_LONG);}
            case DT_FLOAT_ARRAY -> {Float[] arr = ArrayUtils.toObject(entry.getFloatArray()); return new ArrayContainer<>(entry.getName(), "Float", arr, new int[]{arr.length}, DataType.DT_FLOAT_ARRAY, DataType.DT_FLOAT);}
            case DT_DOUBLE_ARRAY -> {Double[] arr = ArrayUtils.toObject(entry.getDoubleArray()); return new ArrayContainer<>(entry.getName(), "Double", arr, new int[]{arr.length}, DataType.DT_DOUBLE_ARRAY, DataType.DT_DOUBLE);}
            case DT_STRING_ARRAY -> {String[] arr = entry.getStringArray(); return new ArrayContainer<>(entry.getName(), "String", arr, new int[]{arr.length}, DataType.DT_STRING_ARRAY, DataType.DT_STRING);}
            case DT_DATA_ARRAY -> {
                Data[] arr = entry.getDataArray(); PropertyContainer[] propertyContainers = getPropertyArray(arr);
                return new ArrayContainer<>(entry.getName(), "Property", propertyContainers, new int[]{arr.length}, DataType.DT_DATA_ARRAY, DataType.DT_DATA);
            }

            case DT_BYTE_ARRAY_2D -> {
                Array2D<byte[]> array2D = entry.getByteArray2D(); Byte[] elems = ArrayUtils.toObject(array2D.getElements()); int[] dims = getDims2D(array2D);
                return new ArrayContainer<>(entry.getName(), "Byte", elems, dims, DataType.DT_BYTE_ARRAY_2D, DataType.DT_BYTE);
            }
            case DT_BOOL_ARRAY_2D ->{
                Array2D<boolean[]> array2D = entry.getBoolArray2D(); Boolean[] elems = ArrayUtils.toObject(array2D.getElements()); int[] dims = getDims2D(array2D);
                return new ArrayContainer<>(entry.getName(), "Bool", elems, dims, DataType.DT_BOOL_ARRAY_2D, DataType.DT_BOOL);
            }
            case DT_SHORT_ARRAY_2D -> {
                Array2D<short[]> array2D = entry.getShortArray2D(); Short[] elems = ArrayUtils.toObject(array2D.getElements()); int[] dims = getDims2D(array2D);
                return new ArrayContainer<>(entry.getName(), "Short", elems, dims, DataType.DT_SHORT_ARRAY_2D, DataType.DT_SHORT);
            }
            case DT_INT_ARRAY_2D -> {
                Array2D<int[]> array2D = entry.getIntArray2D(); Integer[] elems = ArrayUtils.toObject(array2D.getElements()); int[] dims = getDims2D(array2D);
                return new ArrayContainer<>(entry.getName(), "Int", elems, dims, DataType.DT_INT_ARRAY_2D, DataType.DT_INT);
            }
            case DT_LONG_ARRAY_2D -> {
                Array2D<long[]> array2D = entry.getLongArray2D(); Long[] elems = ArrayUtils.toObject(array2D.getElements()); int[] dims = getDims2D(array2D);
                return new ArrayContainer<>(entry.getName(), "Long", elems, dims, DataType.DT_LONG_ARRAY_2D, DataType.DT_LONG);
            }
            case DT_FLOAT_ARRAY_2D -> {
                Array2D<float[]> array2D = entry.getFloatArray2D(); Float[] elems = ArrayUtils.toObject(array2D.getElements()); int[] dims = getDims2D(array2D);
                return new ArrayContainer<>(entry.getName(), "Float", elems, dims, DataType.DT_FLOAT_ARRAY_2D, DataType.DT_FLOAT);
            }
            case DT_DOUBLE_ARRAY_2D ->{
                Array2D<double[]> array2D = entry.getDoubleArray2D(); Double[] elems = ArrayUtils.toObject(array2D.getElements()); int[] dims = getDims2D(array2D);
                return new ArrayContainer<>(entry.getName(), "Double", elems, dims, DataType.DT_DOUBLE_ARRAY_2D, DataType.DT_DOUBLE);
            }
            case DT_STRING_ARRAY_2D -> {
                Array2D<String[]> array2D = entry.getStringArray2D(); String[] elems = array2D.getElements(); int[] dims = getDims2D(array2D);
                return new ArrayContainer<>(entry.getName(), "String", elems, dims, DataType.DT_STRING_ARRAY_2D, DataType.DT_STRING);
            }
            case DT_DATA_ARRAY_2D -> {
                Array2D<Data[]> array2D = entry.getDataArray2D(); Data[] elems = array2D.getElements(); PropertyContainer[] propertyContainers = getPropertyArray(elems); int[] dims = getDims2D(array2D);
                return new ArrayContainer<>(entry.getName(), "Property", propertyContainers, dims, DataType.DT_DATA_ARRAY_2D, DataType.DT_DATA);
            }

            case DT_BYTE_MULTI_ARRAY -> {
                MultiArray<byte[]> arrayMulti = entry.getByteMultiArray(); Byte[] elems = ArrayUtils.toObject(arrayMulti.getElements()); int[] dims = arrayMulti.getDimensions();
                return new ArrayContainer<>(entry.getName(), "Byte", elems, dims, DataType.DT_BYTE_MULTI_ARRAY, DataType.DT_BYTE);
            }
            case DT_BOOL_MULTI_ARRAY -> {
                MultiArray<boolean[]> arrayMulti = entry.getBoolMultiArray(); Boolean[] elems = ArrayUtils.toObject(arrayMulti.getElements()); int[] dims = arrayMulti.getDimensions();
                return new ArrayContainer<>(entry.getName(), "Bool", elems, dims, DataType.DT_BOOL_MULTI_ARRAY, DataType.DT_BOOL);
            }
            case DT_SHORT_MULTI_ARRAY ->  {
                MultiArray<short[]> arrayMulti = entry.getShortMultiArray(); Short[] elems = ArrayUtils.toObject(arrayMulti.getElements()); int[] dims = arrayMulti.getDimensions();
                return new ArrayContainer<>(entry.getName(), "Short", elems, dims, DataType.DT_SHORT_MULTI_ARRAY, DataType.DT_SHORT);
            }
            case DT_INT_MULTI_ARRAY ->  {
                MultiArray<int[]> arrayMulti = entry.getIntMultiArray(); Integer[] elems = ArrayUtils.toObject(arrayMulti.getElements()); int[] dims = arrayMulti.getDimensions();
                return new ArrayContainer<>(entry.getName(), "Int", elems, dims, DataType.DT_INT_MULTI_ARRAY, DataType.DT_INT);
            }
            case DT_LONG_MULTI_ARRAY ->  {
                MultiArray<long[]> arrayMulti = entry.getLongMultiArray(); Long[] elems = ArrayUtils.toObject(arrayMulti.getElements()); int[] dims = arrayMulti.getDimensions();
                return new ArrayContainer<>(entry.getName(), "Long", elems, dims, DataType.DT_LONG_MULTI_ARRAY, DataType.DT_LONG);
            }
            case DT_FLOAT_MULTI_ARRAY ->  {
                MultiArray<float[]> arrayMulti = entry.getFloatMultiArray(); Float[] elems = ArrayUtils.toObject(arrayMulti.getElements()); int[] dims = arrayMulti.getDimensions();
                return new ArrayContainer<>(entry.getName(), "Float", elems, dims, DataType.DT_FLOAT_MULTI_ARRAY, DataType.DT_FLOAT);
            }
            case DT_DOUBLE_MULTI_ARRAY ->  {
                MultiArray<double[]> arrayMulti = entry.getDoubleMultiArray(); Double[] elems = ArrayUtils.toObject(arrayMulti.getElements()); int[] dims = arrayMulti.getDimensions();
                return new ArrayContainer<>(entry.getName(), "Double", elems, dims, DataType.DT_DOUBLE_MULTI_ARRAY, DataType.DT_DOUBLE);
            }
            case DT_STRING_MULTI_ARRAY ->  {
                MultiArray<String[]> arrayMulti = entry.getStringMultiArray(); String[] elems = arrayMulti.getElements(); int[] dims = arrayMulti.getDimensions();
                return new ArrayContainer<>(entry.getName(), "String", elems, dims, DataType.DT_STRING_MULTI_ARRAY, DataType.DT_STRING);
            }
            case DT_DATA_MULTI_ARRAY -> {
                MultiArray<Data[]> arrayMulti = entry.getDataMultiArray(); Data[] elems = arrayMulti.getElements(); PropertyContainer[] propertyContainers = getPropertyArray(elems); int[] dims = arrayMulti.getDimensions();
                return new ArrayContainer<>(entry.getName(), "Property", propertyContainers, dims, DataType.DT_DATA_MULTI_ARRAY, DataType.DT_DATA);
            }
        }
        return null;
    }
    private static int[] getDims2D(Array2D<?> array2D){
        int[] dims = new int[2];

        dims[0] = array2D.getRowCount();
        dims[1] = array2D.getColumnCount();

        return dims;
    }

}
