package types;

import java.lang.invoke.TypeDescriptor;

public class DataContainer {
    private Object data;
    private TypeDescriptor typeDescriptor;

    public DataContainer(Object data, TypeDescriptor typeDescriptor){
        this.data = data;
        this.typeDescriptor = typeDescriptor;
    }

    public Object getData() {
        return data;
    }

    public TypeDescriptor getFieldName() {
        return typeDescriptor;
    }
}
