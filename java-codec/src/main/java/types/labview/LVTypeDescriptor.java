package types.labview;

public class LVTypeDescriptor {
    private LVType lvType;
    private String fieldName;
    public LVTypeDescriptor(LVType lvType, String fieldName){
        this.lvType = lvType;
        this.fieldName = fieldName;
    }
    public LVType getType() {
        return lvType;
    }

    public String getFieldName() {return fieldName;}

    //TODO: correct
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Field Name: ");
        stringBuilder.append(fieldName);
        stringBuilder.append(", Type: ");
        stringBuilder.append(lvType);
        stringBuilder.append(" | ");
        return stringBuilder.toString();
    }
}
