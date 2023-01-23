package types.labview;

public class LVArrayDescriptor extends LVTypeDescriptor {
    private int arrDim;
    private LVTypeDescriptor arrTypeDesc;
    public LVArrayDescriptor(String fieldName, int arrDim, LVTypeDescriptor arrTypeDesc){
        super(LVType.ARRAY, fieldName);
        this.arrDim = arrDim;
        this.arrTypeDesc = arrTypeDesc;
    }
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n-----ARRAY DIMENSION AND TYPE-----\n");
        stringBuilder.append("DIMENSION: ");
        stringBuilder.append(arrDim);
        stringBuilder.append(", TYPE: ");
        stringBuilder.append(arrTypeDesc.toString());
        stringBuilder.append("\n--------------------------------------\n");
        return stringBuilder.toString();
    }
    public LVTypeDescriptor getArrayTypeDesc() {
        return arrTypeDesc;
    }

    public int getArrayDim() {
        return arrDim;
    }
}
