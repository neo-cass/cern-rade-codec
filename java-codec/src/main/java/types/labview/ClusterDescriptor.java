package types.labview;

public class ClusterDescriptor extends LVTypeSpecificDescription {
    private int nTypeDescriptors;
    private LVTypeDescriptor[] LVTypeDescriptors;
    private int currentIndex = 0;
    public ClusterDescriptor(int nTypeDescriptors){
        this.nTypeDescriptors = nTypeDescriptors;
        this.LVTypeDescriptors = new LVTypeDescriptor[nTypeDescriptors];
    }

    public boolean addNewTypeDescriptor(LVTypeDescriptor LVTypeDescriptor){
        if (currentIndex < nTypeDescriptors) {
            LVTypeDescriptors[currentIndex] = LVTypeDescriptor;
            currentIndex++;
            return true;
        }
        else{
            return false;
        }
    }

    public LVTypeDescriptor[] getTypeDescriptors() {
        return LVTypeDescriptors;
    }

    public int getnTypeDescriptors(){
        return nTypeDescriptors;
    }

    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n-----CLUSTER INFO-----\n");
        for (int i = 0; i<nTypeDescriptors; i++){
            stringBuilder.append(LVTypeDescriptors[i].toString());
        }
        stringBuilder.append("\n----------------------\n");
        return stringBuilder.toString();
    }
}
