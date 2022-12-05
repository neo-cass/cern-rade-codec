package types.labview;

public class LVClusterInfo extends LVTypeInfo {
    private int nTypeDescriptors;
    private LVTypeDescriptor[] lVTypeDescriptors;
    private int currentIndex = 0;
    public LVClusterInfo(int nTypeDescriptors){
        this.nTypeDescriptors = nTypeDescriptors;
        this.lVTypeDescriptors = new LVTypeDescriptor[nTypeDescriptors];
    }

    public boolean addNewTypeDescriptor(LVTypeDescriptor LVTypeDescriptor){
        if (currentIndex < nTypeDescriptors) {
            lVTypeDescriptors[currentIndex] = LVTypeDescriptor;
            currentIndex++;
            return true;
        }
        else{
            return false;
        }
    }

    public LVTypeDescriptor[] getTypeDescriptors() {
        return lVTypeDescriptors;
    }

    public int getnTypeDescriptors(){
        return nTypeDescriptors;
    }

    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n-----CLUSTER INFO-----\n");
        for (int i = 0; i<nTypeDescriptors; i++){
            stringBuilder.append(lVTypeDescriptors[i].toString());
        }
        stringBuilder.append("\n----------------------\n");
        return stringBuilder.toString();
    }
}
