package cern.rade.services.codec.lvtojava;

import cern.rade.services.common.types.TypeLookup;
import cern.rade.services.common.types.labview.*;

import java.nio.ByteBuffer;

public final class LVTypeDecoder {
    public static LVTypeDescriptor decode(ByteBuffer buffer){
        int length = buffer.getShort();
        buffer.get();//As per the NI documentation, the first 2 bytes are the length of the type descriptor and the next 2 bytes are the type code but the first of these bytes is reserved for use by LabVIEW
        byte typeCode = buffer.get();
        LVType type = TypeLookup.getLVType(typeCode);
        LVTypeDescriptor lvTypeDescriptor = null;

        switch(type){
            case CLUSTER -> {lvTypeDescriptor = decodeCluster(buffer);}
            case ARRAY -> {lvTypeDescriptor = decodeArray(buffer);}
            case STRING, PATH, PICTURE -> {lvTypeDescriptor = decodeString(buffer);}
            case WAVE_FORM -> {}
            case REFNUM -> {}
            case VARIANT -> {}
            case ENUM_8, ENUM_16, ENUM_32 -> {}
            case SGL_PHS, DBL_PHS, EXT_PHS, SGL_PHS_CPX, EXT_PHS_CPX -> {}
            case INT_8, INT_16, INT_32, INT_64, UINT_8, UINT_16, UINT_32, UINT_64, SGL, DBL, EXT, SGL_CPX, DBL_CPX, EXT_CPX, BOOL -> {lvTypeDescriptor = decodeBasic(buffer, type);}
        }
        return lvTypeDescriptor;
    }

    private static LVClusterDescriptor decodeCluster(ByteBuffer buffer){
        int nTDs = buffer.getShort();
        LVTypeDescriptor nestedType;
        LVTypeDescriptor[] nestedTypes = new LVTypeDescriptor[nTDs];
        int nestedlen;
        for (int i = 0; i<nTDs; i++){
            buffer.mark();
            nestedlen = buffer.getShort();
            buffer.reset();

            nestedType = decode(buffer.slice(buffer.position(), nestedlen));
            nestedTypes[i] = nestedType;

            buffer.position(buffer.position() + nestedlen);
        }

        String fieldName = readFieldName(buffer);

        return new LVClusterDescriptor(fieldName, nestedTypes);
    }

    private static LVTypeDescriptor decodeBasic(ByteBuffer buffer, LVType lVType){
        String fieldName = readFieldName(buffer);
        return new LVTypeDescriptor(lVType, fieldName);
    }

    private static LVTypeDescriptor decodeString(ByteBuffer buffer){
        buffer.getInt(); //FFFF FFFF - 4 bytes read
        String fieldName = readFieldName(buffer);
        return new LVTypeDescriptor(LVType.STRING, fieldName);
    }

    private static LVArrayDescriptor decodeArray(ByteBuffer buffer){

        int dimension = buffer.getShort(); // 2 bytes
        for (int i = 0; i<dimension; i++) {
            buffer.getInt(); // FFFF FFFF
        }

        buffer.mark();
        int nestedLen = buffer.getShort();
        buffer.reset();
        LVTypeDescriptor arrayType = decode(buffer.slice(buffer.position(), nestedLen));
        buffer.position(buffer.position() + nestedLen);

        String fieldName = readFieldName(buffer);
        return new LVArrayDescriptor(fieldName, dimension, arrayType);
    }

    private static String readFieldName(ByteBuffer buffer){
        if (!buffer.hasRemaining()){
            return new String ("\u0000");
        }
        int fieldNameLen = buffer.get();
        byte[] fieldNameBytes = new byte[fieldNameLen];
        buffer.get(fieldNameBytes);
        return new String(fieldNameBytes);
    }
}
