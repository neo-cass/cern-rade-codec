package codec;
import cern.cmw.data.Array2D;
import cern.cmw.data.Data;
import cern.cmw.data.DataFactory;
import cern.cmw.data.Entry;
import types.ArrayContainer;
import types.JavaContainer;
import types.PropertyContainer;

import java.lang.reflect.Array;
import java.util.ArrayList;

public final class JavaToCMWD<T> {
    public static Data encode(JavaContainer javaData){
        Data data = null;
        switch(javaData.getDataType()){
            case DT_DATA -> data = encodeCluster((PropertyContainer) javaData);
            default -> {data = DataFactory.createData(); encode(javaData, data);}
        }
        return data;
    }

    private static Data encode(JavaContainer javaData, Data data){
        switch(javaData.getDataType()){
            //TODO: possible issue with data inside data when not required (e.g. no nesting present)
            case DT_DATA -> {data.append(javaData.getFieldName(), encodeCluster((PropertyContainer) javaData));}
            case DT_BOOL_ARRAY, DT_BYTE_ARRAY, DT_DOUBLE_ARRAY, DT_FLOAT_ARRAY, DT_STRING_ARRAY, DT_INT_ARRAY, DT_SHORT_ARRAY, DT_LONG_ARRAY, DT_DATA_ARRAY,
                    DT_BOOL_ARRAY_2D, DT_BYTE_ARRAY_2D, DT_DOUBLE_ARRAY_2D, DT_FLOAT_ARRAY_2D, DT_STRING_ARRAY_2D, DT_INT_ARRAY_2D, DT_SHORT_ARRAY_2D, DT_LONG_ARRAY_2D, DT_DATA_ARRAY_2D,
                    DT_BOOL_MULTI_ARRAY, DT_BYTE_MULTI_ARRAY, DT_DOUBLE_MULTI_ARRAY, DT_FLOAT_MULTI_ARRAY, DT_STRING_MULTI_ARRAY, DT_INT_MULTI_ARRAY, DT_SHORT_MULTI_ARRAY, DT_LONG_MULTI_ARRAY, DT_DATA_MULTI_ARRAY -> {appendEncodedArray(javaData, data);}
            case DT_BYTE, DT_SHORT, DT_INT, DT_LONG, DT_FLOAT, DT_DOUBLE, DT_BOOL, DT_STRING -> {appendEncodedSingle(javaData, data);}
        }
        return data;
    }

    private static void appendEncodedSingle(JavaContainer javaData, Data data) {
        switch(javaData.getDataType()){
            case DT_BYTE -> data.append(javaData.getFieldName(), (byte)javaData.getData());
            case DT_SHORT -> data.append(javaData.getFieldName(), (short)javaData.getData());
            case DT_INT -> data.append(javaData.getFieldName(), (int)javaData.getData());
            case DT_LONG -> data.append(javaData.getFieldName(), (long)javaData.getData());
            case DT_FLOAT -> data.append(javaData.getFieldName(), (float)javaData.getData());
            case DT_DOUBLE -> data.append(javaData.getFieldName(), (double)javaData.getData());
            case DT_BOOL -> data.append(javaData.getFieldName(), (boolean)javaData.getData());
            case DT_STRING -> data.append(javaData.getFieldName(), (String)javaData.getData());
        }
    }

    private static Data encodeCluster(PropertyContainer propertyContainer) {
        JavaContainer[] javaContainers = propertyContainer.getData();
        Data clusterData = DataFactory.createData();

        for (JavaContainer javaContainer : javaContainers) {
            encode(javaContainer, clusterData);
        }
        return clusterData;
    }



    //TODO: find a way to sepearate the dimension code into a function
    private static void appendEncodedArray(ArrayContainer arrayContainer, Data data) {


        int[] dimensions = arrayContainer.getDimensions();
        int len = javaData.length;
        Object[] arr = new Object[0];
 
        switch(arrayContainer.getDataType()){
            case DT_BYTE_ARRAY, DT_BYTE_ARRAY_2D, DT_BYTE_MULTI_ARRAY -> {
                JavaContainer<Byte>[] javaData = arrayContainer.getData();

                for (int i =0; i< javaData.length; i++){
                    arr[i] = (byte)(javaData[i].getData());
                }
            }
            case DT_SHORT_ARRAY, DT_SHORT_ARRAY_2D, DT_SHORT_MULTI_ARRAY -> {
                for (int i =0; i< javaData.length; i++){
                    arr[i] = (short)javaData[i].getData();
                }
            }
            case DT_INT_ARRAY, DT_INT_ARRAY_2D, DT_INT_MULTI_ARRAY -> {
                for (int i =0; i< javaData.length; i++){
                    arr[i] = (int)javaData[i].getData();
                }
            }
            case DT_LONG_ARRAY, DT_LONG_ARRAY_2D, DT_LONG_MULTI_ARRAY -> {
                for (int i =0; i< javaData.length; i++){
                    arr[i] = (long)javaData[i].getData();
                }
            }
            case DT_FLOAT_ARRAY, DT_FLOAT_ARRAY_2D, DT_FLOAT_MULTI_ARRAY -> {
                for (int i =0; i< javaData.length; i++){
                    arr[i] = (float)javaData[i].getData();
                }
            }
            case DT_DOUBLE_ARRAY, DT_DOUBLE_ARRAY_2D, DT_DOUBLE_MULTI_ARRAY -> {
                for (int i =0; i< javaData.length; i++){
                    arr[i] = (double)javaData[i].getData();
                }
            }
            case DT_BOOL_ARRAY, DT_BOOL_ARRAY_2D, DT_BOOL_MULTI_ARRAY -> {
                for (int i =0; i< javaData.length; i++){
                    arr[i] = (boolean)javaData[i].getData();
                }
            }
            case DT_STRING_ARRAY, DT_STRING_ARRAY_2D, DT_STRING_MULTI_ARRAY -> {
                for (int i =0; i< javaData.length; i++){
                    arr[i] = (String)javaData[i].getData();
                }
            }
            case DT_DATA_ARRAY, DT_DATA_ARRAY_2D, DT_DATA_MULTI_ARRAY -> {
                Data[] dataArr = new Data[javaData.length];
                for (int i =0; i< javaData.length; i++){
                    dataArr[i] = encode(javaData[i]);
                }
            }
        }

        if(dimensions==null){
            data.appendArray(arrayContainer.getFieldName(), (byte[])arr);
        }
        else if (dimensions.length==2){
            data.appendArray2D(arrayContainer.getFieldName(), arr, dimensions[0], dimensions[1]);
        }
        else{
            data.appendMultiArray(arrayContainer.getFieldName(), arr, dimensions);
        }
    }

}
