package cern.rade.services.codec.labview;

import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.TypeLookup;
import cern.rade.services.common.containers.labview.*;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public final class LVTypeDecoder {
    public static LVTypeDescriptor decode(ByteBuffer buffer) throws CodecException {
        try {
            return decodeInternal(buffer);
        } catch (BufferUnderflowException e){
            throw new CodecException("Exception occurred when decoding type descriptor, expected more bytes on the buffer.", e);
        }
    }

    private static LVTypeDescriptor decodeInternal(ByteBuffer buffer) throws BufferUnderflowException, CodecException {
        int length = buffer.getShort();
        buffer.get();//As per the NI documentation, the first 2 bytes are the length of the type descriptor and the next 2 bytes are the type code but the first of these bytes is reserved for use by LabVIEW
        byte typeCode = buffer.get();
        LVType type = TypeLookup.getLVType(typeCode);
        LVTypeDescriptor lvTypeDescriptor = null;
        if(type==null){throw new CodecException("Could not map type code to a LabVIEW type, check type descriptor");}
        switch(type){
            case CLUSTER -> {lvTypeDescriptor = decodeCluster(buffer);}
            case ARRAY -> {lvTypeDescriptor = decodeArray(buffer);}
            case STRING, PATH, PICTURE -> {lvTypeDescriptor = decodeString(buffer);}
            case WAVE_FORM, REFNUM, VARIANT -> {throw new CodecException("Unimplemented LabVIEW type: " + type);}
            case ENUM_8, ENUM_16, ENUM_32 -> {throw new CodecException("Unimplemented LabVIEW type: " + type);}
            case SGL_PHS, DBL_PHS, EXT_PHS, SGL_PHS_CPX, EXT_PHS_CPX, DBL_PHS_CPX, UINT_64 -> {throw new CodecException("Unsupported LabVIEW type: " + type);}
            case INT_8, INT_16, INT_32, INT_64, UINT_8, UINT_16, UINT_32, SGL, DBL, EXT, SGL_CPX, DBL_CPX, EXT_CPX, BOOL -> {lvTypeDescriptor = decodeBasic(buffer, type);}
            default -> throw new CodecException("Unknown type.");
        }
        return lvTypeDescriptor;
    }

    private static LVClusterDescriptor decodeCluster(ByteBuffer buffer) throws BufferUnderflowException, CodecException {
        int nTDs = buffer.getShort();
        LVTypeDescriptor nestedType;
        LVTypeDescriptor[] nestedTypes = new LVTypeDescriptor[nTDs];
        int nestedlen;
        for (int i = 0; i<nTDs; i++){
            buffer.mark();
            nestedlen = buffer.getShort();
            buffer.reset();

            nestedType = decodeInternal(buffer.slice(buffer.position(), nestedlen));
            nestedTypes[i] = nestedType;

            buffer.position(buffer.position() + nestedlen);
        }

        String fieldName = readFieldName(buffer);

        return new LVClusterDescriptor(fieldName, nestedTypes);
    }

    private static LVTypeDescriptor decodeBasic(ByteBuffer buffer, LVType lVType) throws BufferUnderflowException {
        String fieldName = readFieldName(buffer);
        return new LVTypeDescriptor(lVType, fieldName);
    }

    private static LVTypeDescriptor decodeString(ByteBuffer buffer)throws BufferUnderflowException {
        buffer.getInt(); //FFFF FFFF - 4 bytes read
        String fieldName = readFieldName(buffer);
        return new LVTypeDescriptor(LVType.STRING, fieldName);
    }

    private static LVArrayDescriptor decodeArray(ByteBuffer buffer) throws BufferUnderflowException, CodecException {
        int dimension = buffer.getShort(); // 2 bytes
        for (int i = 0; i<dimension; i++) {
            buffer.getInt(); // FFFF FFFF
        }
        buffer.mark();
        int nestedLen = buffer.getShort();
        buffer.reset();
        LVTypeDescriptor arrayType = decodeInternal(buffer.slice(buffer.position(), nestedLen));
        buffer.position(buffer.position() + nestedLen);

        String fieldName = readFieldName(buffer);
        return new LVArrayDescriptor(fieldName, dimension, arrayType);
    }

    private static String readFieldName(ByteBuffer buffer) throws BufferUnderflowException {
        if (!buffer.hasRemaining()){
            return "";
        }
        int fieldNameLen = buffer.get();
        byte[] fieldNameBytes = new byte[fieldNameLen];
        buffer.get(fieldNameBytes);
        return new String(fieldNameBytes);
    }
}
