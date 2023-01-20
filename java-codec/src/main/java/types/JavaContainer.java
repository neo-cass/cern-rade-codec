package types;

import cern.cmw.data.DataType;

import java.lang.reflect.Constructor;
import java.util.function.Supplier;

public class JavaContainer<T> {
    protected String fieldName;
    protected T data;
    protected DataType dataType;
    protected StorageType storageType;
    public JavaContainer(String fieldName, T data, DataType dataType) throws NoSuchMethodException {
        this.fieldName = fieldName;
        this.data = data;
        this.dataType = dataType;
    }
    public JavaContainer(String fieldName, T data, DataType dataType, StorageType storageType) throws NoSuchMethodException {
        this.fieldName = fieldName;
        this.data = data;
        this.dataType = dataType;
        this.storageType = storageType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public T getData() {
        return data;
    }

    public DataType getDataType() {
        return dataType;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public String toString(){
        StringBuilder strB = new StringBuilder();
        strB.append("Name: ").append(fieldName).append("\n");
        strB.append("Data: ").append(data.toString()).append("\n");
        strB.append("Data Type: ").append(dataType).append("\n");
        strB.append("Storage Type: ").append(storageType).append("\n");
        return strB.toString();
    }
}
