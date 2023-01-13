package types;

import cern.cmw.data.DataType;

public class JavaContainer<T> {
    private String fieldName;
    private T data;
    private DataType dataType;
    private int[] dimensions;

    public JavaContainer(String fieldName, T data, DataType dataType){
        this.fieldName = fieldName;
        this.data = data;
        this.dataType = dataType;
        this.dimensions = new int[0];
    }
    public JavaContainer(String fieldName, T data, int[] dimensions, DataType dataType){
        this.fieldName = fieldName;
        this.data = data;
        this.dataType = dataType;
        this.dimensions = dimensions;
    }


    public String getFieldName() {
        return fieldName;
    }

    public T getData() {
        return data;
    }

    public int[] getDimensions() {
        return dimensions;
    }

    public DataType getDataType() {
        return dataType;
    }

    public String toString(){
        System.out.println("TO STRING USED");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("FIELD NAME: ");
        stringBuilder.append(fieldName);
        stringBuilder.append(" | DATA: ");
        if(data instanceof JavaContainer[]){
            stringBuilder.append("ITEMS IN ARRAY: ");
            clusterToString(stringBuilder);

        }
        else{
            stringBuilder.append(data.toString());
        }
        stringBuilder.append(" | ");
        return stringBuilder.toString();
    }

    private StringBuilder clusterToString(StringBuilder stringBuilder){
        JavaContainer[] javaData = (JavaContainer[]) data;
        for (JavaContainer item : javaData){
            stringBuilder.append(item.toString());
            stringBuilder.append(", ");
        }
        return stringBuilder;
    }
}
