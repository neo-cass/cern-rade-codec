package types;

import java.lang.reflect.Type;

public class JavaDataContainer {
    private String fieldName;
    private Object data;

    public JavaDataContainer(String fieldName, Object data){
        this.fieldName = fieldName;
        this.data = data;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getData() {
        return data;
    }

    public Type getType(){
        return data.getClass();
    }

    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("FIELD NAME: ");
        stringBuilder.append(fieldName);
        stringBuilder.append(" | DATA: ");
        stringBuilder.append(data);
        return stringBuilder.toString();
    }
}
