package codec;

import cern.cmw.data.*;
import types.JavaContainer;

import java.util.ArrayList;

public class CMWDToJava {

    public static JavaContainer decode(Data cmwData){


        return null;
    }

   private static JavaContainer decodeData(Data cmwData){
        ArrayList<Entry> entries = (ArrayList<Entry>) cmwData.getEntries();
        int len = entries.size();
        JavaContainer[] javaContainers  = new JavaContainer[len];

        for (int i = 0; i<len; i++){
            javaContainers[i] = decode(entries.get(i).getData());
        }
        return new JavaContainer("CMW Data", javaContainers, DataType.DT_DATA);
    }

    private static JavaContainer decodeSingle(Entry entry){
        DataType type = entry.getType();
        Object data = null;
        switch(type){
            case DT_BYTE -> data = entry.getByte();
            case DT_BOOL -> data = entry.getBool();
            case DT_SHORT -> data = entry.getShort();
            case DT_INT -> data = entry.getInt();
            case DT_LONG -> data=entry.getLong();
            case DT_FLOAT -> data=entry.getFloat();
            case DT_DOUBLE -> data=entry.getDouble();
            case DT_STRING -> data=entry.getString();
            case DT_BOOL_ARRAY -> data=entry.getBoolArray();
            case DT_BYTE_ARRAY -> data=entry.getByteArray();
            case DT_SHORT_ARRAY -> data=entry.getShortArray();
            case DT_INT_ARRAY -> data=entry.getIntArray();
            case DT_LONG_ARRAY -> data=entry.getLongArray();
            case DT_FLOAT_ARRAY -> data=entry.getFloatArray();
            case DT_DOUBLE_ARRAY -> data=entry.getDoubleArray();
            case DT_STRING_ARRAY -> data=entry.getStringArray();
        }
        return new JavaContainer(entry.getName(), data, type);
    }


    private static JavaContainer decodeNDArray(Entry entry){
        DataType type = entry.getType();
        int[] dims;
        Object data;
        switch(type){
            case DT_BYTE_ARRAY_2D -> {Array2D array2D = entry.getByteArray2D();data = array2D.getElements(); dims = getDims2D(array2D);}
            case DT_BOOL_ARRAY_2D ->{Array2D array2D = entry.getBoolArray2D();data = array2D.getElements(); dims = getDims2D(array2D);}
            case DT_SHORT_ARRAY_2D -> {Array2D array2D = entry.getShortArray2D();data = array2D.getElements(); dims = getDims2D(array2D);};
            case DT_INT_ARRAY_2D -> {Array2D array2D = entry.getIntArray2D();data = array2D.getElements(); dims = getDims2D(array2D);};
            case DT_LONG_ARRAY_2D -> {Array2D array2D = entry.getLongArray2D();data = array2D.getElements(); dims = getDims2D(array2D);};
            case DT_FLOAT_ARRAY_2D -> {Array2D array2D = entry.getFloatArray2D();data = array2D.getElements(); dims = getDims2D(array2D);};
            case DT_DOUBLE_ARRAY_2D ->{Array2D array2D = entry.getDoubleArray2D();data = array2D.getElements(); dims = getDims2D(array2D);} ;
            case DT_STRING_ARRAY_2D -> {Array2D array2D = entry.getStringArray2D();data = array2D.getElements(); dims = getDims2D(array2D);};

            case DT_BYTE_MULTI_ARRAY -> {MultiArray multiArray = entry.getByteMultiArray();data = multiArray.getElements(); dims = multiArray.getDimensions();}
            case DT_BOOL_MULTI_ARRAY -> {MultiArray multiArray = entry.getBoolMultiArray();data = multiArray.getElements(); dims = multiArray.getDimensions();}
            case DT_SHORT_MULTI_ARRAY -> {MultiArray multiArray = entry.getShortMultiArray();data = multiArray.getElements(); dims = multiArray.getDimensions();}
            case DT_INT_MULTI_ARRAY -> {MultiArray multiArray = entry.getIntMultiArray();data = multiArray.getElements(); dims = multiArray.getDimensions();}
            case DT_LONG_MULTI_ARRAY -> {MultiArray multiArray = entry.getLongMultiArray();data = multiArray.getElements(); dims = multiArray.getDimensions();}
            case DT_FLOAT_MULTI_ARRAY -> {MultiArray multiArray = entry.getFloatMultiArray();data = multiArray.getElements(); dims = multiArray.getDimensions();}
            case DT_DOUBLE_MULTI_ARRAY -> {MultiArray multiArray = entry.getDoubleMultiArray();data = multiArray.getElements(); dims = multiArray.getDimensions();}
            case DT_STRING_MULTI_ARRAY -> {MultiArray multiArray = entry.getStringMultiArray();data = multiArray.getElements(); dims = multiArray.getDimensions();}
        }
        return new JavaContainer<>(entry.getName(), data, dims, type);
    }
    private static int[] getDimsMulti(MultiArray multiArray){
        return multiArray.getDimensions();
    }
    private static int[] getDims2D(Array2D array2D){
        int[] dims = new int[2];

        dims[0] = array2D.getRowCount();
        dims[1] = array2D.getColumnCount();

        return dims;
    }

}
