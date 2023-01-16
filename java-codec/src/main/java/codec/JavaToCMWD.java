package codec;
import cern.cmw.data.Data;
import cern.cmw.data.DataFactory;
import types.JavaContainer;

public class JavaToCMWD {
    public JavaToCMWD(){

    }

    public static Data encode(JavaContainer javaData){
        Data data = null;
        switch(javaData.getDataType()){
            case DT_DATA -> data = encodeCluster(javaData);
            default -> {data = DataFactory.createData(); encode(javaData, data);}
        }
        return data;
    }

    public static Data encode(JavaContainer javaData, Data data){
        switch(javaData.getDataType()){
            //TODO: possible issue with data inside data when not required (e.g. no nesting present)
            case DT_DATA -> {data.append(javaData.getFieldName(), encodeCluster(javaData));}
            case DT_BOOL_ARRAY, DT_BYTE_ARRAY, DT_DOUBLE_ARRAY, DT_FLOAT_ARRAY, DT_STRING_ARRAY, DT_INT_ARRAY, DT_SHORT_ARRAY, DT_LONG_ARRAY,
                    DT_BOOL_ARRAY_2D, DT_BYTE_ARRAY_2D, DT_DOUBLE_ARRAY_2D, DT_FLOAT_ARRAY_2D, DT_STRING_ARRAY_2D, DT_INT_ARRAY_2D, DT_SHORT_ARRAY_2D, DT_LONG_ARRAY_2D,
                    DT_BOOL_MULTI_ARRAY, DT_BYTE_MULTI_ARRAY, DT_DOUBLE_MULTI_ARRAY, DT_FLOAT_MULTI_ARRAY, DT_STRING_MULTI_ARRAY, DT_INT_MULTI_ARRAY, DT_SHORT_MULTI_ARRAY, DT_LONG_MULTI_ARRAY -> {appendEncodedArray(javaData, data);}
            case DT_BYTE, DT_SHORT, DT_INT, DT_LONG, DT_FLOAT, DT_DOUBLE, DT_BOOL, DT_STRING -> {appendEncodedSingle(javaData, data);}
        }
        return data;
    }



    private static Data appendEncodedSingle(JavaContainer javaData, Data data) {
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
        return data;
    }

    private static Data encodeCluster(JavaContainer javaData) {
        JavaContainer[] javaContainers = (JavaContainer[])javaData.getData();
        Data clusterData = DataFactory.createData();

        for (JavaContainer javaContainer : javaContainers) {
            encode(javaContainer, clusterData);
        }
        return clusterData;
    }


    //TODO: find a way to sepearate the dimension code into a function
    private static void appendEncodedArray(JavaContainer javaContainer, Data data) {
       Object javaData = javaContainer.getRawData();
        switch(javaContainer.getDataType()){
            case DT_BYTE_ARRAY, DT_BYTE_ARRAY_2D, DT_BYTE_MULTI_ARRAY -> {
                byte[] arr = (byte[]) javaData;
                int[] dimensions = javaContainer.getDimensions();
                if(dimensions==null){
                    data.appendArray(javaContainer.getFieldName(), arr);
                }
                else if (dimensions.length==2){
                    data.appendArray2D(javaContainer.getFieldName(), arr, dimensions[0], dimensions[1]);
                }
                else{
                    data.appendMultiArray(javaContainer.getFieldName(), arr, dimensions);
                }
            }
            case DT_SHORT_ARRAY, DT_SHORT_ARRAY_2D, DT_SHORT_MULTI_ARRAY -> {
                short[] arr = (short[]) javaData;
                int[] dimensions = javaContainer.getDimensions();
                if(dimensions==null){
                    data.appendArray(javaContainer.getFieldName(), arr);
                }
                else if (dimensions.length==2){
                    data.appendArray2D(javaContainer.getFieldName(), arr, dimensions[0], dimensions[1]);
                }
                else{
                    data.appendMultiArray(javaContainer.getFieldName(), arr, dimensions);
                }
            }
            case DT_INT_ARRAY, DT_INT_ARRAY_2D, DT_INT_MULTI_ARRAY -> {
                int[] arr = (int[]) javaData;
                int[] dimensions = javaContainer.getDimensions();
                if(dimensions==null){
                    data.appendArray(javaContainer.getFieldName(), arr);
                }
                else if (dimensions.length==2){
                    data.appendArray2D(javaContainer.getFieldName(), arr, dimensions[0], dimensions[1]);
                }
                else{
                    data.appendMultiArray(javaContainer.getFieldName(), arr, dimensions);
                }
            }
            case DT_LONG_ARRAY, DT_LONG_ARRAY_2D, DT_LONG_MULTI_ARRAY -> {
                long[] arr = (long[]) javaData;
                int[] dimensions = javaContainer.getDimensions();
                if(dimensions==null){
                    data.appendArray(javaContainer.getFieldName(), arr);
                }
                else if (dimensions.length==2){
                    data.appendArray2D(javaContainer.getFieldName(), arr, dimensions[0], dimensions[1]);
                }
                else{
                    data.appendMultiArray(javaContainer.getFieldName(), arr, dimensions);
                }
            }
            case DT_FLOAT_ARRAY, DT_FLOAT_ARRAY_2D, DT_FLOAT_MULTI_ARRAY -> {
                float[] arr = (float[]) javaData;
                int[] dimensions = javaContainer.getDimensions();
                if(dimensions==null){
                    data.appendArray(javaContainer.getFieldName(), arr);
                }
                else if (dimensions.length==2){
                    data.appendArray2D(javaContainer.getFieldName(), arr, dimensions[0], dimensions[1]);
                }
                else{
                    data.appendMultiArray(javaContainer.getFieldName(), arr, dimensions);
                }
            }
            case DT_DOUBLE_ARRAY, DT_DOUBLE_ARRAY_2D, DT_DOUBLE_MULTI_ARRAY -> {
                double[] arr = (double[]) javaData;
                int[] dimensions = javaContainer.getDimensions();
                if(dimensions==null){
                    data.appendArray(javaContainer.getFieldName(), arr);
                }
                else if (dimensions.length==2){
                    data.appendArray2D(javaContainer.getFieldName(), arr, dimensions[0], dimensions[1]);
                }
                else{
                    data.appendMultiArray(javaContainer.getFieldName(), arr, dimensions);
                }
            }
            case DT_BOOL_ARRAY, DT_BOOL_ARRAY_2D, DT_BOOL_MULTI_ARRAY -> {
                boolean[] arr = (boolean[]) javaData;
                int[] dimensions = javaContainer.getDimensions();
                if(dimensions==null){
                    data.appendArray(javaContainer.getFieldName(), arr);
                }
                else if (dimensions.length==2){
                    data.appendArray2D(javaContainer.getFieldName(), arr, dimensions[0], dimensions[1]);
                }
                else{
                    data.appendMultiArray(javaContainer.getFieldName(), arr, dimensions);
                }
            }
            case DT_STRING_ARRAY, DT_STRING_ARRAY_2D, DT_STRING_MULTI_ARRAY -> {
                String[] arr = (String[]) javaData;
                int[] dimensions = javaContainer.getDimensions();

                if(dimensions==null){
                    data.appendArray(javaContainer.getFieldName(), arr);
                }
                else if (dimensions.length==2){
                    data.appendArray2D(javaContainer.getFieldName(), arr, dimensions[0], dimensions[1]);
                }
                else{
                    data.appendMultiArray(javaContainer.getFieldName(), arr, dimensions);
                }
            }
        }
    }

}
