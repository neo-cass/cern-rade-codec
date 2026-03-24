package cern.rade.services.codec.javatolv;

import cern.cmw.data.DataType;
import cern.japc.value.Type;
import cern.japc.value.ValueType;
import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.java.ArrayContainer;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.java.FieldContainer;
import cern.rade.services.common.containers.java.PropertyContainer;
import cern.rade.services.common.containers.TypeLookup;
import static cern.rade.services.common.BytesUtil.byteArrToShortArr;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

public final class LVTypeEncoder {

    public static short[] encodeTypeDescriptor(DataContainer dataContainer) throws CodecException {
        try {
            return byteArrToShortArr(encodeTypeDescriptor_bytes(dataContainer));
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

    private static byte[] encodeTypeDescriptor_bytes(DataContainer dataContainer) throws IOException, CodecException {
        if (dataContainer.getType() == Type.MAP) {
            return encodeClusterDescriptor((PropertyContainer) dataContainer);
        } else {
            return encodeFieldDescriptor((FieldContainer) dataContainer);
        }
    }

    private static byte[] encodeFieldDescriptor(FieldContainer field) throws IOException, CodecException {
        ValueType type = field.getValueType();
        if (type.isScalar()) {
            return encodeScalarDescriptor(field.getName(), type);
        } else if (type.isArray() || type.isArray2d()) {
            return encodeArrayDescriptor(field);
        } else {
            throw new CodecException("Unsupported ValueType: " + type);
        }
    }

    private static byte[] encodeScalarDescriptor(String fieldName, ValueType type) throws CodecException {
        if (type.equals(ValueType.STRING)) {
            return encodeStringDescriptor(fieldName, type);
        } else {
            return encodeNumericDescriptor(fieldName, type);
        }
    }

    private static ByteBuffer padWithZeros(int totalLength) {
        if (totalLength % 2 == 0) {
            return ByteBuffer.allocate(totalLength);
        } else {
            ByteBuffer byteBuffer = ByteBuffer.allocate(totalLength + 1);
            byteBuffer.put(totalLength - 1, (byte) 0);
            return byteBuffer;
        }
    }

    private static byte[] encodeNumericDescriptor(String fieldName, ValueType type) {
        byte typeCode = TypeLookup.getLVTypeCode(type);
        short totalLength = (short) (labelLength(fieldName) + 4);
        ByteBuffer byteBuffer = padWithZeros(totalLength);
        byteBuffer.putShort((short) byteBuffer.capacity());
        byteBuffer.put(labelFlag(fieldName));
        byteBuffer.put(typeCode);
        writeLabel(byteBuffer, fieldName);
        return byteBuffer.array();
    }

    private static byte[] encodeStringDescriptor(String fieldName, ValueType type) {
        byte typeCode = TypeLookup.getLVTypeCode(type);
        short totalLength = (short) (labelLength(fieldName) + 8);
        ByteBuffer byteBuffer = padWithZeros(totalLength);
        byteBuffer.putShort((short) byteBuffer.capacity());
        byteBuffer.put(labelFlag(fieldName));
        byteBuffer.put(typeCode);
        byteBuffer.putShort((short) 0xFFFF);
        byteBuffer.putShort((short) 0xFFFF);
        writeLabel(byteBuffer, fieldName);
        return byteBuffer.array();
    }

    private static byte[] encodeClusterDescriptor(PropertyContainer propertyContainer) throws IOException, CodecException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        byte typeCode = TypeLookup.getLVClusterTypeCode();
        String fieldName = propertyContainer.getName();
        String[] names = propertyContainer.getNames();
        short nTDs = (short) names.length;
        short totalLength = 0;

        for (String name : names) {
            byte[] nestedTypeDesc = encodeTypeDescriptor_bytes(propertyContainer.get(name));
            totalLength += nestedTypeDesc.length;
            dos.write(nestedTypeDesc);
        }

        totalLength += (short) (labelLength(fieldName) + 6);
        ByteBuffer byteBuffer = padWithZeros(totalLength);
        byteBuffer.putShort((short) byteBuffer.capacity());
        byteBuffer.put(labelFlag(fieldName));
        byteBuffer.put(typeCode);
        byteBuffer.putShort(nTDs);
        byteBuffer.put(bos.toByteArray());
        writeLabel(byteBuffer, fieldName);
        return byteBuffer.array();
    }

    private static byte[] encodeArrayDescriptor(FieldContainer field) throws IOException, CodecException {
        ValueType type = field.getValueType();
        ValueType componentType = type.getComponentType();
        int dimension = type.isArray2d() ? 2 : 1;
        String fieldName = field.getName();
        byte typeCode = TypeLookup.getLVTypeCode(type);

        // omit labels of array elements
        byte[] elemTypeDesc = encodeScalarDescriptor(null, componentType);

        short totalLength = (short) (labelLength(fieldName) + 6 + (dimension * 4) + elemTypeDesc.length);
        ByteBuffer byteBuffer = padWithZeros(totalLength);
        byteBuffer.putShort((short) byteBuffer.capacity());
        byteBuffer.put(labelFlag(fieldName));
        byteBuffer.put(typeCode);
        byteBuffer.putShort((short) dimension);
        for (int i = 0; i < dimension * 2; i++) {
            byteBuffer.putShort((short) 0xFFFF);
        }
        byteBuffer.put(elemTypeDesc);
        writeLabel(byteBuffer, fieldName);
        return byteBuffer.array();
    }
}