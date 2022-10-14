package types;

import cern.cmw.data.DataType;

import java.lang.reflect.Type;

public class JavaDataContainer {
    private String fieldName;
    private Object data;
    private DataType dataType;

    public JavaDataContainer(String fieldName, Object data, DataType dataType){
        this.fieldName = fieldName;
        this.data = data;
        this.dataType = dataType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getData() {
        return data;
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
        if(data instanceof JavaDataContainer[]){
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
        JavaDataContainer[] javaData = (JavaDataContainer[]) data;
        for (JavaDataContainer item : javaData){
            stringBuilder.append(item.toString());
            stringBuilder.append(", ");
        }
        return stringBuilder;

    }
}
