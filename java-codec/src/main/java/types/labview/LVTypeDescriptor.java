package types.labview;

public class LVTypeDescriptor {
    private LVType lvType;
    private LVTypeInfo lVTypeInfo;
    private String fieldName;
    public LVTypeDescriptor(LVType lvType, LVTypeInfo LVTypeInfo, String fieldName){
        this.lvType = lvType;
        this.lVTypeInfo = LVTypeInfo;
        this.fieldName = fieldName;
    }

    public LVTypeDescriptor(LVType lvType, String fieldName){
        this.lvType = lvType;
        this.fieldName = fieldName;
    }

    public LVType getType() {
        return lvType;
    }

    public String getFieldName() {return fieldName;}
    public boolean hasTypeInfo(){
        return lVTypeInfo !=null;
    }

    public LVTypeInfo getTypeSpecificInfo() {return lVTypeInfo;}

    //TODO: correct
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Field Name: ");
        stringBuilder.append(fieldName);
        stringBuilder.append(", Type: ");
        stringBuilder.append(lvType);
        stringBuilder.append(" | ");
        if(lVTypeInfo != null) {
            stringBuilder.append(lVTypeInfo.toString());
        }
        return stringBuilder.toString();
    }
}
