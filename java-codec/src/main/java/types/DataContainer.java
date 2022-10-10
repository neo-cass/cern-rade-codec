package types;


import types.labview.LVTypeDescriptor;

public class DataContainer {
    private Object data;
    private LVTypeDescriptor lvTypeDescriptor;

    public DataContainer(Object data, LVTypeDescriptor lvTypeDescriptor){
        this.data = data;
        this.lvTypeDescriptor = lvTypeDescriptor;
    }

    public Object getData() {
        return data;
    }

    public LVTypeDescriptor getTypeDescriptor() {
        return lvTypeDescriptor;
    }
}
