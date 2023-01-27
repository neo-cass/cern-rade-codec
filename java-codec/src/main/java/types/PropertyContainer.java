package types;

import cern.cmw.data.DataType;

public class PropertyContainer extends JavaContainer<JavaContainer[]>{
    public PropertyContainer(String fieldName, JavaContainer<?>[] data, DataType dataType)   {
        super(fieldName, data, dataType);
    }
    public String toString(){
        StringBuilder strB = new StringBuilder();
        strB.append("Name: ").append(fieldName).append("\n");
        strB.append("Data Type: ").append(dataType).append("\n");
        strB.append("\n---Begin Data For: ");
        strB.append(fieldName);
        strB.append(" ---\n");
        for (JavaContainer jc : data){
            strB.append(jc.toString());
            strB.append("\n");
        }
        strB.append("---End Data For: ");
        strB.append(fieldName);
        strB.append(" ---\n");
        return strB.toString();
    }

    public void jumble(){
        JavaContainer<?> temp = data[0];
        data[0] = data[1];
        data[1] = temp;
    }
}
