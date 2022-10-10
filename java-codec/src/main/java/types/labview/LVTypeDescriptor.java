package types.labview;

public class LVTypeDescriptor {
    private LVType lvType;
    private LVTypeSpecificDescription LVTypeSpecificDescription;
    private String fieldName;
    public LVTypeDescriptor(LVType lvType, LVTypeSpecificDescription LVTypeSpecificDescription, String fieldName){
        this.lvType = lvType;
        this.LVTypeSpecificDescription = LVTypeSpecificDescription;
        this.fieldName = fieldName;
    }

    public LVTypeDescriptor(LVType lvType, String fieldName){
        this.lvType = lvType;
        this.fieldName = fieldName;
    }

    public LVType getLvType() {
        return lvType;
    }

    public String getFieldName() {return fieldName;}

    public LVTypeSpecificDescription getTypeSpecificInfo() {return LVTypeSpecificDescription;}

    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Field Name: ");
        stringBuilder.append(fieldName);
        stringBuilder.append(", Type: ");
        stringBuilder.append(lvType);
        stringBuilder.append(" | ");
        if(LVTypeSpecificDescription != null) {
            stringBuilder.append(LVTypeSpecificDescription.toString());
        }
        return stringBuilder.toString();
    }
}
