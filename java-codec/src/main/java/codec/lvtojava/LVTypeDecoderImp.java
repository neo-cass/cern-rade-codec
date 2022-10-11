package codec.lvtojava;

import types.labview.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class LVTypeDecoderImp {
    private ByteBuffer buffer;

    public LVTypeDecoderImp(byte[] rawData){
        buffer = ByteBuffer.wrap(rawData);
    }

    private String getFieldName(){
        int fieldNameLen = buffer.get();
        byte[] fieldName = new byte[fieldNameLen];
        buffer.get(fieldName);
        return new String(fieldName);
    }

    private LVTypeDescriptor decodeCluster(){
        int nTDs = buffer.getShort();
        LVTypeDescriptor nestedLVTypeDesc;
        ClusterDescriptor clusterDescriptor = new ClusterDescriptor(nTDs);
        for (int i = 0; i<nTDs; i++){
            nestedLVTypeDesc = decodeTypeDescriptor();
            clusterDescriptor.addNewTypeDescriptor(nestedLVTypeDesc);
        }
        return new LVTypeDescriptor(LVType.CLUSTER, clusterDescriptor, getFieldName());
    }

    private LVTypeDescriptor decodeArray(){
        int dimension = buffer.getShort();
        buffer.getShort(); //FFFF
        buffer.getShort(); //FFFF
        LVTypeDescriptor lvTypeDescriptor = decodeTypeDescriptor();
        ArrayDescriptor arrayDescriptor = new ArrayDescriptor(dimension, lvTypeDescriptor);

        return new LVTypeDescriptor(LVType.ARRAY, arrayDescriptor, getFieldName());
    }

    private LVTypeDescriptor decodeStringPathPic(){
        return null;
    }

    private LVTypeDescriptor decodeBasic(LVType type){
        return new LVTypeDescriptor(type, getFieldName());
    }

    //TODO: remove recursive calls.
    //Here we call the type specific decoder
    public LVTypeDescriptor decodeTypeDescriptor(){
        int typeLen = buffer.getShort();
        buffer.get();//As per the NI documentation, the first 2 bytes are the length of the type descriptor and the next 2 bytes are the type code but the first of these bytes is reserved for use by LabVIEW
        byte typeCode = buffer.get();
        LVType type = LVTypeLookup.getType(typeCode);
        LVTypeDescriptor LVTypeDescriptor = null;
        switch(type){
            case CLUSTER -> {
                LVTypeDescriptor = decodeCluster();
            }
            case ARRAY -> {
                LVTypeDescriptor = decodeArray();
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
                LVTypeDescriptor = decodeBasic(type);
            }


        }
        return LVTypeDescriptor;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }
}
