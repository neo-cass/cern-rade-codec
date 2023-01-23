package types;

import cern.cmw.data.DataType;

import java.lang.reflect.Constructor;
import java.util.function.Supplier;

public class JavaContainer<T> {
    protected String fieldName;
    protected T data;
    protected DataType dataType;
    public JavaContainer(String fieldName, T data, DataType dataType)   {
        this.fieldName = fieldName;
        this.data = data;
        this.dataType = dataType;
    }
    public String getFieldName() {
        return fieldName;
    }

    public T getData() {return data;}

    public DataType getDataType() {
        return dataType;
    }

    public String toString(){
        StringBuilder strB = new StringBuilder();
        strB.append("Name: ").append(fieldName).append("\n");
        strB.append("Data: ").append(data.toString()).append("\n");
        strB.append("Data Type: ").append(dataType).append("\n");
        return strB.toString();
    }
}
