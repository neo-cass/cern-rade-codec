package codec.lvtojava;

import cern.accsoft.commons.util.value.Pair;
import types.labview.*;

import java.nio.ByteBuffer;

public final class LVTypeDecoderImp {

    public LVTypeDecoderImp(){

    }

    private static Pair<String, Integer> decodeFieldName(ByteBuffer buffer){
        int fieldNameLen = buffer.get();
        byte[] fieldName = new byte[fieldNameLen];
        buffer.get(fieldName);
        return new Pair<>(new String(fieldName), fieldNameLen + 1);
    }

    private static Pair<String, Integer> getFieldName(ByteBuffer buffer, int typeLen, int bytesRead){
        Pair<String, Integer> namePair = decodeFieldName(buffer);
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

    private static Pair<LVTypeDescriptor, Integer> decodeCluster(ByteBuffer buffer, int typeLen){
        int nTDs = buffer.getShort(); // 2 bytes
        int bytesRead = 6;
        LVTypeDescriptor nestedLVTypeDesc = null;
        Pair<LVTypeDescriptor, Integer> typePair;
        ClusterDescriptor clusterDescriptor = new ClusterDescriptor(nTDs);
        for (int i = 0; i<nTDs; i++){
            typePair = decodeTypeDescriptorInternal(buffer);
            nestedLVTypeDesc = typePair.getFirst();
            clusterDescriptor.addNewTypeDescriptor(nestedLVTypeDesc);
            bytesRead+=typePair.getSecond();
        }
        Pair<String, Integer> namePair = getFieldName(buffer, typeLen, bytesRead);
        String name = namePair.getFirst();
        bytesRead=namePair.getSecond();
        LVTypeDescriptor clusterLVTypeDesc = new LVTypeDescriptor(LVType.CLUSTER, clusterDescriptor, name);

        return new Pair<>(clusterLVTypeDesc, bytesRead);
    }

    private static Pair<LVTypeDescriptor, Integer> decodeArray(ByteBuffer buffer, int typeLen){

        int dimension = buffer.getShort(); // 2 bytes
        buffer.getShort(); //FFFF - 2 bytes
        buffer.getShort(); //FFFF - 2 bytes
        int bytesRead = 10;
        Pair<LVTypeDescriptor, Integer> typePair = decodeTypeDescriptorInternal(buffer);
        bytesRead += typePair.getSecond();


        LVTypeDescriptor arrayType = typePair.getFirst();
        ArrayDescriptor arrayDescriptor = new ArrayDescriptor(dimension, arrayType);

        Pair<String, Integer> namePair = getFieldName(buffer, typeLen, bytesRead);
        String name = namePair.getFirst();
        bytesRead=namePair.getSecond();
        LVTypeDescriptor lvTypeDescriptor =  new LVTypeDescriptor(LVType.ARRAY, arrayDescriptor, name);

        return new Pair<>(lvTypeDescriptor, bytesRead);
    }

    private static Pair<LVTypeDescriptor, Integer> decodeString(ByteBuffer buffer, int typeLen){
        buffer.getInt(); //FFFF FFFF - 4 bytes read
        int bytesRead = 8;


        Pair<String, Integer> namePair = getFieldName(buffer, typeLen, bytesRead);
        String name = namePair.getFirst();
        bytesRead=namePair.getSecond();
        LVTypeDescriptor lvTypeDescriptor = new LVTypeDescriptor(LVType.STRING, name);

        return new Pair<>(lvTypeDescriptor, bytesRead);
    }


    private static Pair<LVTypeDescriptor, Integer> decodeBasic(ByteBuffer buffer, LVType type, int typeLen){
        int bytesRead = 4;
        Pair<String, Integer> namePair = getFieldName(buffer, typeLen, bytesRead);
        String name = namePair.getFirst();
        bytesRead=namePair.getSecond();
        LVTypeDescriptor lvTypeDescriptor = new LVTypeDescriptor(type, name);

        return new Pair<>(lvTypeDescriptor, bytesRead);
    }

    //TODO: remove recursive calls.
    //Here we call the type specific decoder
    private static Pair<LVTypeDescriptor, Integer> decodeTypeDescriptorInternal(ByteBuffer buffer){
        int typeLen = buffer.getShort(); //2 bytes read
        buffer.get();//As per the NI documentation, the first 2 bytes are the length of the type descriptor and the next 2 bytes are the type code but the first of these bytes is reserved for use by LabVIEW
        byte typeCode = buffer.get();
        LVType type = LVTypeLookup.getType(typeCode);
        Pair<LVTypeDescriptor, Integer> typePair = null;
        switch(type){
            case CLUSTER -> {
                typePair =decodeCluster(buffer, typeLen);
            }
            case ARRAY -> {
                typePair = decodeArray(buffer, typeLen);
            }

            case STRING, PATH, PICTURE -> {
                typePair = decodeString(buffer, typeLen);

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
                typePair = decodeBasic(buffer, type, typeLen);
            }
        }
        return typePair;
    }

    public static LVTypeDescriptor decodeTypeDescriptor(ByteBuffer buffer){
        Pair<LVTypeDescriptor, Integer> typePair = decodeTypeDescriptorInternal(buffer);
        LVTypeDescriptor lvTypeDescriptor = typePair.getFirst();
        return lvTypeDescriptor;
    }
}
