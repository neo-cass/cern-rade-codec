package codec.lvtojava;

import types.labview.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class LVTypeDecoderImp {
    private ByteBuffer buffer;
    private ArrayList<LVTypeDescriptor> lvTypeDescriptors;
    private void decodeCluster(){
        int nTDs = buffer.getShort();


        for (int i = 0; i<nTDs; i++){
            lvTypeDescriptors.add(decodeTypeDescriptor());
        }
        int bufferLeft = buffer.remaining();
        byte[] fieldName = new byte[bufferLeft];
        buffer.get(fieldName);
        String name = new String(fieldName);
        LVTypeDescriptor lvTypeDescriptor = new LVTypeDescriptor(LVType.CLUSTER, name);
    }

    private LVTypeDescriptor decodeArray(){

    }

    private LVTypeDescriptor decodeStringPathPic(){
        return null;
    }

    private LVTypeDescriptor decodeBasic(){

    }

    //TODO: remove recursive calls.
    //Here we call the type specific decoder
    private LVTypeDescriptor decodeTypeDescriptor(){
        //As per the NI documentation, the first 2 bytes are the length of the type descriptor and the next 2 bytes are the type code but the first of these bytes is reserved for use by LabVIEW
        int typeLen = buffer.getShort();
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
                LVTypeDescriptor = decodeBasic(typeDescriptorArr);
            }
        }
        return LVTypeDescriptor;
    }
}
