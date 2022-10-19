package codec.javatocmw;
import cern.cmw.data.Data;
import cern.cmw.data.DataFactory;
import types.JavaDataContainer;

public class JavaToCMWData {
    public JavaToCMWData(){

    }

    public static Data encode(JavaDataContainer javaData){
        Data data = null;
        switch(javaData.getDataType()){
            case DT_DATA -> data = encodeCluster(javaData);
            default -> {data = DataFactory.createData(); encode(javaData, data);}
        }
        return data;
    }

    public static Data encode(JavaDataContainer javaData, Data data){
        switch(javaData.getDataType()){
            //TODO: possible issue with data inside data when not required (e.g. no nesting present)
            case DT_DATA -> {data.append(javaData.getFieldName(), encodeCluster(javaData));}
            case DT_BOOL_ARRAY, DT_BYTE_ARRAY, DT_DOUBLE_ARRAY, DT_FLOAT_ARRAY,
                    DT_STRING_ARRAY, DT_INT_ARRAY, DT_SHORT_ARRAY, DT_LONG_ARRAY -> {appendEncodedArray(javaData, data);}
            case DT_BOOL_ARRAY_2D, DT_BYTE_ARRAY_2D, DT_DOUBLE_ARRAY_2D, DT_FLOAT_ARRAY_2D,
                    DT_STRING_ARRAY_2D, DT_INT_ARRAY_2D, DT_SHORT_ARRAY_2D, DT_LONG_ARRAY_2D -> {appendEncodedArray2D(javaData, data);}
            case DT_BOOL_MULTI_ARRAY, DT_BYTE_MULTI_ARRAY, DT_DOUBLE_MULTI_ARRAY, DT_FLOAT_MULTI_ARRAY,
                    DT_STRING_MULTI_ARRAY, DT_INT_MULTI_ARRAY, DT_SHORT_MULTI_ARRAY, DT_LONG_MULTI_ARRAY -> {appendEncodedArrayMulti(javaData, data);}
            case DT_BYTE, DT_SHORT, DT_INT, DT_LONG, DT_FLOAT, DT_DOUBLE, DT_BOOL, DT_STRING -> {appendEncodedSingle(javaData, data);}
        }
        return data;
    }

    private static Data appendEncodedSingle(JavaDataContainer javaData, Data data) {
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

    private static Data encodeCluster(JavaDataContainer javaData) {
        JavaDataContainer[] javaDataContainers = (JavaDataContainer[])javaData.getData();
        Data clusterData = DataFactory.createData();

        for (JavaDataContainer javaDataContainer : javaDataContainers) {
            encode(javaDataContainer, clusterData);
        }
        return clusterData;
    }

    private static void appendEncodedArray(JavaDataContainer javaData, Data data) {
        JavaDataContainer[] javaDataContainers = (JavaDataContainer[])javaData.getData();
        int arrayLen = javaDataContainers.length;
        switch(javaData.getDataType()){
            case DT_BYTE_ARRAY -> {
                byte[] newArray = new byte[arrayLen];
                for (int i = 0; i<arrayLen; i++){
                    newArray[i] = (byte)javaDataContainers[i].getData();
                }
                data.appendArray(javaData.getFieldName(), newArray);
            }
            case DT_SHORT_ARRAY -> {
                short[] newArray = new short[arrayLen];
                for (int i = 0; i<arrayLen; i++){
                    newArray[i] = (short)javaDataContainers[i].getData();
                }
                data.appendArray(javaData.getFieldName(), newArray);
            }
            case DT_INT_ARRAY -> {
                int[] newArray = new int[arrayLen];
                for (int i = 0; i<arrayLen; i++){
                    newArray[i] = (int)javaDataContainers[i].getData();
                }
                data.appendArray(javaData.getFieldName(), newArray);
            }
            case DT_LONG_ARRAY -> {
                long[] newArray = new long[arrayLen];
                for (int i = 0; i<arrayLen; i++){
                    newArray[i] = (long)javaDataContainers[i].getData();
                }
                data.appendArray(javaData.getFieldName(), newArray);
            }
            case DT_FLOAT_ARRAY -> {
                float[] newArray = new float[arrayLen];
                for (int i = 0; i<arrayLen; i++){
                    newArray[i] = (float)javaDataContainers[i].getData();
                }
                data.appendArray(javaData.getFieldName(), newArray);
            }
            case DT_DOUBLE_ARRAY -> {
                double[] newArray = new double[arrayLen];
                for (int i = 0; i<arrayLen; i++){
                    newArray[i] = (double)javaDataContainers[i].getData();
                }
                data.appendArray(javaData.getFieldName(), newArray);
            }
            case DT_BOOL_ARRAY -> {
                boolean[] newArray = new boolean[arrayLen];
                for (int i = 0; i<arrayLen; i++){
                    newArray[i] = (boolean)javaDataContainers[i].getData();
                }
                data.appendArray(javaData.getFieldName(), newArray);
            }
            case DT_STRING_ARRAY -> {
                String[] newArray = new String[arrayLen];
                for (int i = 0; i<arrayLen; i++){
                    newArray[i] = (String)javaDataContainers[i].getData();
                }
                data.appendArray(javaData.getFieldName(), newArray);
            }
        }
    }

    private static void appendEncodedArray2D(JavaDataContainer javaData, Data data) {
    }

    private static void appendEncodedArrayMulti(JavaDataContainer javaData, Data data) {
    }

}
