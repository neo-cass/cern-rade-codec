package types;

import cern.cmw.data.DataType;
public class ArrayContainer<T> extends JavaContainer<T[]>{
    private int[] dimensions;
    private String elemName;
    private DataType elemType;
    public ArrayContainer(String fieldName, String elemName, T[] data, int[] dimensions, DataType dataType, DataType elemType)   {
        super(fieldName, data, dataType);
        this.dimensions = dimensions;
        this.elemName = elemName;
        this.elemType = elemType;
    }
    public int[] getDimensions() {return dimensions;}
    public String getElemName() {return elemName;}
    public DataType getElemType() {return elemType;}
    public String toString(){
        StringBuilder strB = new StringBuilder();
        strB.append("Name: ").append(fieldName).append("\n");
        strB.append("Array Type: ").append(dataType).append("\n");
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
