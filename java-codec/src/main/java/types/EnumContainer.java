package types;

import cern.cmw.data.DataType;

import java.util.HashMap;

public class EnumContainer<T> extends JavaContainer<T> {
    private HashMap<String, Integer> enumLabels;
    public EnumContainer(String fieldName, T data, DataType dataType, HashMap<String, Integer> enumLabels) throws NoSuchMethodException {
        super (fieldName,  data,  dataType);
        this.enumLabels = enumLabels;
    }
    public HashMap<String, Integer> getEnumLabels() {
        return enumLabels;
    }

    //TODO: implement toString
}
