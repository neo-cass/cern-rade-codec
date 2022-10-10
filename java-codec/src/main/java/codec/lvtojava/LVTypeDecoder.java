package codec.lvtojava;

import types.labview.*;

//TODO: replace with ByteBuffer wrap
public class LVTypeDecoder {

    private short getTwoBytesAsShort(byte byte1, byte byte2){
        short val = (short)(((byte1 & 0xFF) << 8) | (byte2 & 0xFF));
        return val;
    }

    private LVTypeDescriptor decodeCluster(byte[] typeDescriptorArr){
        int nTDs = getTwoBytesAsShort(typeDescriptorArr[4], typeDescriptorArr[5]);
        ClusterDescriptor clusterDescriptor = new ClusterDescriptor(nTDs);
        int index = 6;
        int typeDescLen;
        for (int i = 0; i < nTDs; i++){
            typeDescLen = getTwoBytesAsShort(typeDescriptorArr[index], typeDescriptorArr[index+1]);
            byte [] subTypeArr = copyOfRange(typeDescriptorArr, index, index+typeDescLen);
            clusterDescriptor.addNewTypeDescriptor(decodeTypeDescriptor(subTypeArr));
            index+=typeDescLen;
        }
        int fieldNameLen = typeDescriptorArr[index++];
        String fieldName = new String(copyOfRange(typeDescriptorArr, index, index+fieldNameLen));
        return new LVTypeDescriptor(LVType.CLUSTER, clusterDescriptor, fieldName);
    }



    private LVTypeDescriptor decodeArray(byte[] typeDescriptorArr){
        int dimension = getTwoBytesAsShort(typeDescriptorArr[4], typeDescriptorArr[5]);
        int typeDescLen = getTwoBytesAsShort(typeDescriptorArr[10], typeDescriptorArr[11]);
        byte[] subTypeArr = copyOfRange(typeDescriptorArr, 10, 10+typeDescLen);
        LVTypeDescriptor LVTypeDescriptor = decodeTypeDescriptor(subTypeArr);
        ArrayDescriptor arrayDescriptor = new ArrayDescriptor(dimension, LVTypeDescriptor);
        int fieldNameLen = typeDescriptorArr[10+typeDescLen];
        String fieldName = new String(copyOfRange(typeDescriptorArr, 10+typeDescLen+1, 10+typeDescLen+1+fieldNameLen));
        return new LVTypeDescriptor(LVType.ARRAY, arrayDescriptor, fieldName);
    }

    private LVTypeDescriptor decodeStringPathPic(){
        return null;
    }

    private LVTypeDescriptor decodeBasic(byte[] typeDescriptorArr){
        byte typeCode = typeDescriptorArr[3];
        LVType type = LVTypeLookup.getType(typeCode);
        int fieldNameLen = typeDescriptorArr[4];
        String fieldName = new String(copyOfRange(typeDescriptorArr, 5, 5+fieldNameLen));
        return new LVTypeDescriptor(type, fieldName);
    }

    //faster than Arrays.copyOfRange
    private byte[] copyOfRange(byte[] source, int from, int to){
        int length = to-from;
        byte[] newArray = new byte[length];
        System.arraycopy(source, from, newArray, 0, length);
        return newArray;
    }

    public LVTypeDescriptor decodeTypeFull(byte[] typeDescriptor){
        int length = getTwoBytesAsShort(typeDescriptor[0], typeDescriptor[1]);
        byte[] typeDescriptorSubset = copyOfRange(typeDescriptor, 0, length);
        return decodeTypeDescriptor(typeDescriptorSubset);
    }

    //TODO: remove recursive calls.
    //Here we call the type specific decoder
    private LVTypeDescriptor decodeTypeDescriptor(byte[] typeDescriptorArr){
        //As per the NI documentation, the first 2 bytes are the length of the type descriptor and the next 2 bytes are the type code but the first of these bytes is reserved for use by LabVIEW
        byte typeCode = typeDescriptorArr[3];
        LVType type = LVTypeLookup.getType(typeCode);
        LVTypeDescriptor LVTypeDescriptor = null;
        switch(type){
            case CLUSTER -> {
                LVTypeDescriptor = decodeCluster(typeDescriptorArr);
            }
            case ARRAY -> {
                LVTypeDescriptor = decodeArray(typeDescriptorArr);
            }

            case STRING, PATH, PICTURE -> {

            }

            case WAVE_FORM -> {

            }

            case REFNUM -> {

            }

            case VARIANT -> {

            }

            case ENUM_8, ENUM_16, ENUM_32 -> {

            }

            case SGL_PHS, DBL_PHS, EXT_PHS, SGL_PHS_CPX, EXT_PHS_CPX -> {

            }

            case INT_8, INT_16, INT_32, INT_64, UINT_8, UINT_16, UINT_32, UINT_64, SGL, DBL, EXT, SGL_CPX, DBL_CPX, EXT_CPX, BOOL -> {
                LVTypeDescriptor = decodeBasic(typeDescriptorArr);
            }
        }
        return LVTypeDescriptor;
    }
}
