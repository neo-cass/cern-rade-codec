package codec.lvtojava;

import cern.accsoft.commons.util.value.Pair;
import types.labview.*;

import java.nio.ByteBuffer;

public class LVTypeDecoderImp {
    private ByteBuffer buffer;

    public LVTypeDecoderImp(byte[] rawData){
        buffer = ByteBuffer.wrap(rawData);
    }



    private Pair<String, Integer> decodeFieldName(){
        int fieldNameLen = buffer.get();
        byte[] fieldName = new byte[fieldNameLen];
        buffer.get(fieldName);
        return new Pair<String, Integer>(new String(fieldName), fieldNameLen+1);
    }

    private Pair<String, Integer> getFieldName(int typeLen, int bytesRead){
        Pair<String, Integer> namePair = decodeFieldName();
        String name = namePair.getFirst();
        bytesRead+=namePair.getSecond();
        if (bytesRead<typeLen){
            for(int i = 0; i < typeLen-bytesRead; i++){
                buffer.get();
            }
            bytesRead = typeLen;
        }
        return new Pair<>(name, bytesRead);
    }

    private Pair<LVTypeDescriptor, Integer> decodeCluster(int typeLen){
        int nTDs = buffer.getShort(); // 2 bytes
        int bytesRead = 6;
        LVTypeDescriptor nestedLVTypeDesc = null;
        Pair<LVTypeDescriptor, Integer> typePair;
        ClusterDescriptor clusterDescriptor = new ClusterDescriptor(nTDs);
        for (int i = 0; i<nTDs; i++){
            typePair = decodeTypeDescriptor();
            nestedLVTypeDesc = typePair.getFirst();
            clusterDescriptor.addNewTypeDescriptor(nestedLVTypeDesc);
            bytesRead+=typePair.getSecond();
        }
        Pair<String, Integer> namePair = getFieldName(typeLen, bytesRead);
        String name = namePair.getFirst();
        bytesRead=namePair.getSecond();
        LVTypeDescriptor clusterLVTypeDesc = new LVTypeDescriptor(LVType.CLUSTER, clusterDescriptor, name);

        return new Pair<>(clusterLVTypeDesc, bytesRead);
    }

    private Pair<LVTypeDescriptor, Integer> decodeArray(int typeLen){

        int dimension = buffer.getShort(); // 2 bytes
        buffer.getShort(); //FFFF - 2 bytes
        buffer.getShort(); //FFFF - 2 bytes
        int bytesRead = 10;
        Pair<LVTypeDescriptor, Integer> typePair = decodeTypeDescriptor();
        bytesRead += typePair.getSecond();


        LVTypeDescriptor arrayType = typePair.getFirst();
        ArrayDescriptor arrayDescriptor = new ArrayDescriptor(dimension, arrayType);

        Pair<String, Integer> namePair = getFieldName(typeLen, bytesRead);
        String name = namePair.getFirst();
        bytesRead=namePair.getSecond();
        LVTypeDescriptor lvTypeDescriptor =  new LVTypeDescriptor(LVType.ARRAY, arrayDescriptor, name);

        return new Pair<>(lvTypeDescriptor, bytesRead);
    }

    private Pair<LVTypeDescriptor, Integer> decodeString(int typeLen){
        buffer.getInt(); //FFFF FFFF - 4 bytes read
        int bytesRead = 8;


        Pair<String, Integer> namePair = getFieldName(typeLen, bytesRead);
        String name = namePair.getFirst();
        bytesRead=namePair.getSecond();
        LVTypeDescriptor lvTypeDescriptor = new LVTypeDescriptor(LVType.STRING, name);

        return new Pair<>(lvTypeDescriptor, bytesRead);
    }

    private LVTypeDescriptor decodeStringPathPic(){
        return null;
    }

    private Pair<LVTypeDescriptor, Integer> decodeBasic(LVType type, int typeLen){
        int bytesRead = 4;


        Pair<String, Integer> namePair = getFieldName(typeLen, bytesRead);
        String name = namePair.getFirst();
        bytesRead=namePair.getSecond();
        LVTypeDescriptor lvTypeDescriptor = new LVTypeDescriptor(LVType.STRING, name);

        return new Pair<>(lvTypeDescriptor, bytesRead);
    }

    //TODO: remove recursive calls.
    //Here we call the type specific decoder
    public Pair<LVTypeDescriptor, Integer> decodeTypeDescriptor(){
        int typeLen = buffer.getShort(); //2 bytes read
        buffer.get();//As per the NI documentation, the first 2 bytes are the length of the type descriptor and the next 2 bytes are the type code but the first of these bytes is reserved for use by LabVIEW
        byte typeCode = buffer.get();
        LVType type = LVTypeLookup.getType(typeCode);
        Pair<LVTypeDescriptor, Integer> typePair = null;
        switch(type){
            case CLUSTER -> {
                typePair =decodeCluster(typeLen);
            }
            case ARRAY -> {
                typePair = decodeArray(typeLen);
            }

            case STRING, PATH, PICTURE -> {
                typePair = decodeString(typeLen);

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
                typePair = decodeBasic(type, typeLen);
            }


        }
        return typePair;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }
}
