package cern.rade.services.codec.labview;

import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.TypeLookup;
import cern.rade.services.common.containers.labview.*;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

/**
 * Decodes flattened LabVIEW type descriptors, see {@link LVTypeEncoder} for the format. Besides the LabVIEW types it
 * decodes the two marked layouts: a discrete function (2D array of DBL, type code 0x60) as
 * {@link LVType#DISCRETE_FUNCTION}, and an enum set (cluster of labelled BOOL, type code 0x61) as an
 * {@link LVEnumSetDescriptor}.
 */
public final class LVTypeDecoder {
    private LVTypeDecoder() {
    }

    /**
     * @param buffer the flattened type descriptor (big endian); nested descriptors are read from slices of it
     * @throws CodecException if the descriptor is malformed or has a type which is not supported
     */
    public static LVTypeDescriptor decode(ByteBuffer buffer) throws CodecException {
        try {
            return decodeInternal(buffer);
        } catch (BufferUnderflowException e){
            throw new CodecException("Exception occurred when decoding type descriptor, expected more bytes on the buffer.", e);
        } catch (IndexOutOfBoundsException e){
            throw new CodecException("Exception occurred when decoding type descriptor, a nested type descriptor has an invalid size, check type descriptor.", e);
        }
    }

    private static LVTypeDescriptor decodeInternal(ByteBuffer buffer) throws BufferUnderflowException, CodecException {
        buffer.getShort(); // the size of the descriptor, nested descriptors are delimited by the caller
        buffer.get(); // flags, as per the NI documentation reserved for use by LabVIEW
        byte typeCode = buffer.get();
        LVType type = TypeLookup.getLVType(typeCode);
        if (type == null) {
            throw new CodecException("Could not map type code to a LabVIEW type, check type descriptor");
        }
        return switch (type) {
            case CLUSTER -> decodeCluster(buffer);
            case ENUM_SET -> decodeEnumSet(buffer);
            case DISCRETE_FUNCTION -> decodeDiscreteFunction(buffer);
            case ARRAY -> decodeArray(buffer);
            case STRING, PATH, PICTURE -> decodeString(buffer);
            case WAVE_FORM, REFNUM, VARIANT -> throw new CodecException("Unimplemented LabVIEW type: " + type);
            case ENUM_8, ENUM_16, ENUM_32 -> decodeEnum(buffer, type);
            case SGL_PHS, DBL_PHS, EXT_PHS, SGL_PHS_CPX, EXT_PHS_CPX, DBL_PHS_CPX -> throw new CodecException("Unsupported LabVIEW type: " + type);
            case INT_8, INT_16, INT_32, INT_64, UINT_8, UINT_16, UINT_32, UINT_64, SGL, DBL, EXT, SGL_CPX, DBL_CPX, EXT_CPX, BOOL -> decodeBasic(buffer, type);
            default -> throw new CodecException("Unknown type.");
        };
    }

    private static LVClusterDescriptor decodeCluster(ByteBuffer buffer) throws BufferUnderflowException, CodecException {
        int nTDs = Short.toUnsignedInt(buffer.getShort());
        // the count comes from an untrusted header: bound it before allocating, a nested descriptor takes at least 4 bytes
        if (nTDs > buffer.remaining() / 4) {
            throw new CodecException("Cluster declares " + nTDs + " elements but only " + buffer.remaining() + " bytes follow");
        }
        LVTypeDescriptor[] nestedTypes = new LVTypeDescriptor[nTDs];
        for (int i = 0; i < nTDs; i++) {
            nestedTypes[i] = decodeNested(buffer);
        }
        String fieldName = readFieldName(buffer);
        return new LVClusterDescriptor(fieldName, nestedTypes);
    }

    /**
     * Decodes the descriptor starting at the position of the buffer, whose size is given by its first 2 bytes, and
     * moves the buffer after it.
     */
    private static LVTypeDescriptor decodeNested(ByteBuffer buffer) throws BufferUnderflowException, CodecException {
        int nestedLength = buffer.getShort(buffer.position());
        LVTypeDescriptor nested = decodeInternal(buffer.slice(buffer.position(), nestedLength));
        buffer.position(buffer.position() + nestedLength);
        return nested;
    }

    /**
     * An enum set is laid out like a cluster of BOOL, one per item, labelled with the item name, marked with a non LabVIEW type code.
     */
    private static LVEnumSetDescriptor decodeEnumSet(ByteBuffer buffer) throws BufferUnderflowException, CodecException {
        LVClusterDescriptor cluster = decodeCluster(buffer);
        LVTypeDescriptor[] elements = cluster.getFieldDescriptors();
        if (elements.length == 0) {
            throw new CodecException("Enum set '" + cluster.getName() + "' has no item");
        }
        String[] items = new String[elements.length];
        Set<String> labels = new HashSet<>();
        for (int i = 0; i < items.length; i++) {
            items[i] = elements[i].getName();
            if (elements[i].getType() != LVType.BOOL) {
                throw new CodecException("Enum set '" + cluster.getName() + "' must be laid out as a cluster of BOOL, got an element of type " + elements[i].getType());
            }
            if (items[i] == null || items[i].isBlank()) {
                throw new CodecException("Enum set '" + cluster.getName() + "' has a BOOL without label, each BOOL must be labelled with an item name");
            }
            if (!labels.add(items[i])) {
                throw new CodecException("Enum set '" + cluster.getName() + "' has several BOOL labelled '" + items[i] + "'");
            }
        }
        return new LVEnumSetDescriptor(cluster.getName(), items);
    }

    /**
     * A discrete function is laid out like a 2D array of DBL (row 0 = X, row 1 = Y), marked with a non LabVIEW type code.
     */
    private static LVTypeDescriptor decodeDiscreteFunction(ByteBuffer buffer) throws BufferUnderflowException, CodecException {
        LVArrayDescriptor array = decodeArray(buffer);
        if (array.getDimension() != 2 || array.getElemDescriptor().getType() != LVType.DBL) {
            throw new CodecException("Discrete function " + array.getName() + " must be laid out as a 2D array of DBL (X row, Y row), got "
                    + array.getDimension() + "D array of " + array.getElemDescriptor().getType());
        }
        return new LVTypeDescriptor(LVType.DISCRETE_FUNCTION, array.getName());
    }

    /**
     * Enum: [nItems:u16][item pascal strings...][pad to even][label]
     */
    private static LVEnumDescriptor decodeEnum(ByteBuffer buffer, LVType lvType) throws BufferUnderflowException {
        int nItems = Short.toUnsignedInt(buffer.getShort());
        String[] items = new String[nItems];
        for (int i = 0; i < nItems; i++) {
            items[i] = readPascalString(buffer);
        }
        if (buffer.position() % 2 != 0 && buffer.hasRemaining()) {
            buffer.get(); // padding
        }
        String fieldName = readFieldName(buffer);
        return new LVEnumDescriptor(fieldName, lvType, items);
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
        int dimension = Short.toUnsignedInt(buffer.getShort()); // 2 bytes
        if (dimension > buffer.remaining() / 4) {
            throw new CodecException("Array declares " + dimension + " dimensions but only " + buffer.remaining() + " bytes follow");
        }
        for (int i = 0; i < dimension; i++) {
            buffer.getInt(); // FFFF FFFF
        }
        LVTypeDescriptor elementType = decodeNested(buffer);
        String fieldName = readFieldName(buffer);
        return new LVArrayDescriptor(fieldName, dimension, elementType);
    }

    private static String readFieldName(ByteBuffer buffer) throws BufferUnderflowException {
        if (!buffer.hasRemaining()){
            return "";
        }
        return readPascalString(buffer);
    }

    private static String readPascalString(ByteBuffer buffer) throws BufferUnderflowException {
        int len = Byte.toUnsignedInt(buffer.get());
        byte[] bytes = new byte[len];
        buffer.get(bytes);
        return new String(bytes);
    }
}
