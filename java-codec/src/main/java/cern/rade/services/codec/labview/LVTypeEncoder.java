package cern.rade.services.codec.labview;

import cern.japc.value.EnumType;
import cern.japc.value.MapParameterValue;
import cern.japc.value.ParameterValue;
import cern.japc.value.SimpleParameterValue;
import cern.japc.value.Type;
import cern.japc.value.ValueType;
import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.TypeLookup;
import cern.rade.services.common.containers.labview.LVArrayDescriptor;
import cern.rade.services.common.containers.labview.LVClusterDescriptor;
import cern.rade.services.common.containers.labview.LVEnumDescriptor;
import cern.rade.services.common.containers.labview.LVEnumSetDescriptor;
import cern.rade.services.common.containers.labview.LVType;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;
import static cern.rade.services.common.BytesUtil.byteArrToShortArr;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Builds LabVIEW type descriptors ({@link LVTypeDescriptor}) for JAPC values and flattens them as LabVIEW does (see the
 * NI documentation "Type descriptors"): {@code [size u16 incl. itself, padded to even][flags][type code][type specific
 * info][optional label as a Pascal string]}. The flags byte is 0x40 if the label is present, otherwise 0.
 * <p>
 * Besides the LabVIEW types, two JAPC types have a marked LabVIEW layout: the type code of the layout is replaced by a
 * code LabVIEW does not use, which the LabVIEW side patches in the descriptor it sends and expects. The layout itself
 * is unchanged.
 * <ul>
 *   <li>discrete function: a 2D array of DBL (row 0 = X, row 1 = Y), type code 0x60 instead of 0x40.</li>
 *   <li>enum set: a cluster of BOOL, one per item, labelled with the item name, type code 0x61 instead of 0x50.</li>
 * </ul>
 * A discrete function list is an array of clusters of one discrete function, and an enum set array an array of enum
 * sets: they are regular LV arrays (code 0x40).
 * <p>
 * Enums are described with the symbols of the JAPC enum items ordered by JAPC code, the LV value is the position in
 * that list. JAPC numbers are described with the signed LV type of the same size (unsigned types are only used by
 * descriptors supplied from LabVIEW).
 */
public final class LVTypeEncoder {
    private LVTypeEncoder() {
    }

    /**
     * Builds the LV type descriptor matching a JAPC value.
     *
     * @param name  the LV label of the descriptor
     * @param value a JAPC map (described as a cluster) or simple value
     * @throws CodecException if the value is not representable in LabVIEW (OBJECT values, UNDEFINED values), or is an
     *                        empty array of enums or enum sets (the enum type is unknown)
     */
    public static LVTypeDescriptor describe(String name, ParameterValue value) throws CodecException {
        if (Type.MAP.equals(value.getType())) {
            MapParameterValue map = value.castAsMap();
            String[] names = map.getNames();
            LVTypeDescriptor[] fields = new LVTypeDescriptor[names.length];
            for (int i = 0; i < names.length; i++) {
                fields[i] = describe(names[i], map.get(names[i]));
            }
            return new LVClusterDescriptor(name, fields);
        }
        return describeSimple(name, JapcValues.asSimple(value));
    }

    private static LVTypeDescriptor describeSimple(String name, SimpleParameterValue value) throws CodecException {
        ValueType type = JapcValues.valueTypeOf(value);
        if (ValueType.DISCRETE_FUNCTION_LIST.equals(type)) {
            return LVArrays.describeDiscreteFunctionList(name);
        }
        boolean array = type.isArray() || type.isArray2d();
        ValueType elementType = array ? type.getComponentType() : type;
        EnumType enumType = null;
        if (ValueType.ENUM.equals(elementType) || ValueType.ENUM_SET.equals(elementType)) {
            enumType = JapcValues.enumTypeOf(value);
            if (enumType == null) {
                throw new CodecException("Cannot describe the empty " + type + " '" + name + "': the enum type is unknown");
            }
        }
        if (!array) {
            return describeElement(name, elementType, enumType);
        }
        return new LVArrayDescriptor(name, type.isArray2d() ? 2 : 1, describeElement("", elementType, enumType));
    }

    /**
     * @param enumType the enum type if the element is an enum or an enum set
     */
    private static LVTypeDescriptor describeElement(String label, ValueType elementType, EnumType enumType) throws CodecException {
        if (ValueType.ENUM.equals(elementType)) {
            String[] items = EnumTypes.symbolsByCode(enumType);
            return new LVEnumDescriptor(label, items.length <= 0xFFFF ? LVType.ENUM_16 : LVType.ENUM_32, items);
        }
        if (ValueType.ENUM_SET.equals(elementType)) {
            return new LVEnumSetDescriptor(label, EnumTypes.symbolsByCode(enumType));
        }
        if (ValueType.DISCRETE_FUNCTION.equals(elementType)) {
            return new LVTypeDescriptor(LVType.DISCRETE_FUNCTION, label);
        }
        LVType lvType = TypeLookup.findLVType(elementType).orElseThrow(() -> JapcValues.notRepresentable(elementType));
        return new LVTypeDescriptor(lvType, label);
    }

    /**
     * @return the flattened type descriptor as the LabVIEW "Flatten" of a type descriptor, as 16 bit words.
     */
    public static short[] encodeTypeDescriptor(LVTypeDescriptor desc) throws CodecException {
        return byteArrToShortArr(encodeTypeDescriptorBytes(desc));
    }

    /**
     * @return the flattened type descriptor, see {@link #encodeTypeDescriptor(LVTypeDescriptor)}.
     */
    public static byte[] encodeTypeDescriptorBytes(LVTypeDescriptor desc) throws CodecException {
        try {
            return encode(desc.getName(), desc);
        } catch (IOException e) {
            throw new CodecException("Failed to encode type descriptor due to IOException.", e);
        }
    }

    private static boolean hasLabel(String fieldName) {
        return fieldName != null && !fieldName.isBlank();
    }

    private static byte labelFlag(String fieldName) {
        return hasLabel(fieldName) ? (byte) 64 : (byte) 0;
    }

    private static int labelLength(String fieldName) {
        return hasLabel(fieldName) ? fieldName.getBytes().length + 1 : 0; // +1 for the length prefix byte
    }

    private static void writeLabel(ByteBuffer byteBuffer, String fieldName) {
        if (hasLabel(fieldName)) {
            byte[] fieldNameBytes = fieldName.getBytes();
            byteBuffer.put((byte) fieldNameBytes.length);
            byteBuffer.put(fieldNameBytes);
        }
    }

    private static ByteBuffer padWithZeros(int totalLength) {
        if (totalLength % 2 == 0) {
            return ByteBuffer.allocate(totalLength);
        } else {
            ByteBuffer byteBuffer = ByteBuffer.allocate(totalLength + 1);
            byteBuffer.put(totalLength, (byte) 0);
            return byteBuffer;
        }
    }

    private static ByteBuffer header(String fieldName, byte typeCode, int bodyLength) {
        ByteBuffer byteBuffer = padWithZeros(4 + bodyLength + labelLength(fieldName));
        byteBuffer.putShort((short) byteBuffer.capacity());
        byteBuffer.put(labelFlag(fieldName));
        byteBuffer.put(typeCode);
        return byteBuffer;
    }

    /**
     * @param fieldName the label to write; array elements are written without label.
     */
    private static byte[] encode(String fieldName, LVTypeDescriptor desc) throws IOException, CodecException {
        LVType type = desc.getType();
        return switch (type) {
            case CLUSTER -> encodeCluster(fieldName, TypeLookup.getLVClusterTypeCode(), ((LVClusterDescriptor) desc).getFieldDescriptors());
            case ENUM_SET -> encodeCluster(fieldName, TypeLookup.getLVEnumSetTypeCode(), booleans((LVEnumSetDescriptor) desc));
            case DISCRETE_FUNCTION -> encodeArray(fieldName, TypeLookup.getLVDiscreteFunctionTypeCode(), 2, new LVTypeDescriptor(LVType.DBL, ""));
            case ARRAY -> {
                LVArrayDescriptor array = (LVArrayDescriptor) desc;
                yield encodeArray(fieldName, TypeLookup.getLVArrayTypeCode(), array.getDimension(), array.getElemDescriptor());
            }
            case ENUM_8, ENUM_16, ENUM_32 -> encodeEnum(fieldName, (LVEnumDescriptor) desc);
            case STRING -> encodeString(fieldName);
            case INT_8, INT_16, INT_32, INT_64, UINT_8, UINT_16, UINT_32, UINT_64, SGL, DBL, BOOL -> encodeNumeric(fieldName, TypeLookup.getLVTypeCode(type));
            default -> throw new CodecException("Unsupported LabVIEW type for type descriptor encoding: " + type);
        };
    }

    /**
     * @return the elements of the cluster of an enum set: one BOOL per item, labelled with the item name.
     */
    private static LVTypeDescriptor[] booleans(LVEnumSetDescriptor enumSet) {
        return Arrays.stream(enumSet.getItems()).map(item -> new LVTypeDescriptor(LVType.BOOL, item)).toArray(LVTypeDescriptor[]::new);
    }

    private static byte[] encodeNumeric(String fieldName, byte typeCode) {
        ByteBuffer byteBuffer = header(fieldName, typeCode, 0);
        writeLabel(byteBuffer, fieldName);
        return byteBuffer.array();
    }

    private static byte[] encodeString(String fieldName) {
        ByteBuffer byteBuffer = header(fieldName, TypeLookup.getLVTypeCode(LVType.STRING), 4);
        byteBuffer.putShort((short) 0xFFFF);
        byteBuffer.putShort((short) 0xFFFF);
        writeLabel(byteBuffer, fieldName);
        return byteBuffer.array();
    }

    /**
     * Enum: [nItems:u16][item pascal strings...][pad to even][label]
     */
    private static byte[] encodeEnum(String fieldName, LVEnumDescriptor desc) throws CodecException {
        if (desc.size() > 0xFFFF) {
            throw new CodecException("Too many enum items for a LabVIEW enum: " + desc.size());
        }
        int itemsLength = 0;
        for (String item : desc.getItems()) {
            int len = item.getBytes().length;
            if (len > 255) {
                throw new CodecException("Enum item longer than 255 bytes: " + item);
            }
            itemsLength += 1 + len;
        }
        int pad = (4 + 2 + itemsLength) % 2;
        ByteBuffer byteBuffer = header(fieldName, TypeLookup.getLVTypeCode(desc.getType()), 2 + itemsLength + pad);
        byteBuffer.putShort((short) desc.size());
        for (String item : desc.getItems()) {
            byte[] bytes = item.getBytes();
            byteBuffer.put((byte) bytes.length);
            byteBuffer.put(bytes);
        }
        if (pad == 1) {
            byteBuffer.put((byte) 0);
        }
        writeLabel(byteBuffer, fieldName);
        return byteBuffer.array();
    }

    /**
     * Cluster: [nElements:u16][element TDs][label]. The enum set has the same layout, with another type code.
     */
    private static byte[] encodeCluster(String fieldName, byte typeCode, LVTypeDescriptor[] fieldDescs) throws IOException, CodecException {
        ByteArrayOutputStream nested = new ByteArrayOutputStream();
        for (LVTypeDescriptor fieldDesc : fieldDescs) {
            nested.write(encode(fieldDesc.getName(), fieldDesc));
        }
        ByteBuffer byteBuffer = header(fieldName, typeCode, 2 + nested.size());
        byteBuffer.putShort((short) fieldDescs.length);
        byteBuffer.put(nested.toByteArray());
        writeLabel(byteBuffer, fieldName);
        return byteBuffer.array();
    }

    /**
     * Array: [ndims:u16][ndims x FFFFFFFF][element TD][label]. The discrete function has the same layout, with another type code.
     */
    private static byte[] encodeArray(String fieldName, byte typeCode, int dimension, LVTypeDescriptor elemDescriptor) throws IOException, CodecException {
        // omit labels of array elements
        byte[] elemTypeDesc = encode(null, elemDescriptor);
        ByteBuffer byteBuffer = header(fieldName, typeCode, 2 + dimension * 4 + elemTypeDesc.length);
        byteBuffer.putShort((short) dimension);
        for (int i = 0; i < dimension * 2; i++) {
            byteBuffer.putShort((short) 0xFFFF);
        }
        byteBuffer.put(elemTypeDesc);
        writeLabel(byteBuffer, fieldName);
        return byteBuffer.array();
    }
}
