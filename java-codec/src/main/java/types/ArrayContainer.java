package types;

import cern.cmw.data.DataType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ArrayContainer<T> extends JavaContainer<JavaContainer<T>[]>{
    private int[] dimensions;
    private String elemName;
    private DataType elemType;
    private final Constructor<T[]> ctor;
    public ArrayContainer(Class<T[]> impl, String fieldName, JavaContainer<T>[] data, DataType dataType, int[] dimensions, String elemName, DataType elemType) throws NoSuchMethodException {
        super(fieldName, data, dataType);
        this.dimensions = dimensions;
        this.elemName = elemName;
        this.elemType = elemType;
        this.ctor = impl.getConstructor();
        this.storageType = StorageType.MULTI;
    }
    public int[] getDimensions() {
        return dimensions;
    }
    public String getElemName() {
        return elemName;
    }

    public DataType getElemType() {
        return elemType;
    }
    public T[] getRawData() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        T[] rawArr = ctor.newInstance();
        for(int i = 0; i<data.length; i++){
            rawArr[i] =  data[i].getData();
        }
        return rawArr;
    }
    public String toString(){
        StringBuilder strB = new StringBuilder();
        strB.append("Name: ").append(fieldName).append("\n");
        strB.append("Array Type: ").append(dataType).append("\n");
        strB.append("Storage Type: ").append(storageType).append("\n");
        strB.append("Element Name: ").append(elemName).append("\n");
        strB.append("Element Type: ").append(elemType).append("\n");
        strB.append("Data: [");
        for(int i = 0; i< data.length; i++){
            strB.append(data[i].toString());
            if(i != data.length - 1){strB.append(", ");}
        }
        strB.append("]\n");
        return strB.toString();
    }
}
