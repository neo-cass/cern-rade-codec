package types;


import types.labview.LVTypeDescriptor;

public class DataContainer {
    private Object data;
    private String fieldName;

    public DataContainer(Object data, LVTypeDescriptor fieldName){
        this.data = data;
        this.fieldName = String.valueOf(fieldName);
    }

    public Object getData() {
        return data;
    }

    public String getFieldName() {
        return fieldName;
    }
}
