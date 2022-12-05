package types.labview;

public class LVArrayInfo extends LVTypeInfo {
    private int arrayDim;
    private LVTypeDescriptor arrayType;
    public LVArrayInfo(int arrayDim, LVTypeDescriptor arrayType){
        this.arrayDim = arrayDim;
        this.arrayType = arrayType;
    }

    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n-----ARRAY DIMENSION AND TYPE-----\n");
        stringBuilder.append("DIMENSION: ");
        stringBuilder.append(arrayDim);
        stringBuilder.append(", TYPE: ");
        stringBuilder.append(arrayType.toString());
        stringBuilder.append("\n--------------------------------------\n");
        return stringBuilder.toString();
    }

    public LVTypeDescriptor getArrayType() {
        return arrayType;
    }

    public int getArrayDim() {
        return arrayDim;
    }
}
