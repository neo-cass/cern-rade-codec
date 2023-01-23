package types.labview;

public class LVClusterDescriptor extends LVTypeDescriptor {
    private LVTypeDescriptor[] typeDescs;
    public LVClusterDescriptor(String fieldName, LVTypeDescriptor[] typeDescs){
        super(LVType.CLUSTER, fieldName);
        this.typeDescs= typeDescs;

    }
    public LVTypeDescriptor[] getTypeDescs() {return typeDescs;}

    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n-----CLUSTER INFO-----\n");
        for (int i = 0; i< typeDescs.length; i++){
            stringBuilder.append(typeDescs[i].toString());
        }
        stringBuilder.append("\n----------------------\n");
        return stringBuilder.toString();
    }
}
