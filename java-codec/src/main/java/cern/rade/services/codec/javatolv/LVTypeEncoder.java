package cern.rade.services.codec.javatolv;

import cern.cmw.data.DataType;
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
        byte[] fieldNameBytes = fieldName.getBytes();
        short totalLength = (short) (fieldNameBytes.length + 1 + 4);
        ByteBuffer byteBuffer = padWithZeros(totalLength);
        byteBuffer.putShort((short) byteBuffer.capacity());
        byteBuffer.put((byte) 64);
        byteBuffer.put(typeCode);
        byteBuffer.put((byte) fieldName.length());
        byteBuffer.put(fieldNameBytes);
        return byteBuffer.array();
    }

    private static byte[] encodeStringDescriptor(String fieldName, ValueType type) {
        byte typeCode = TypeLookup.getLVTypeCode(type);
        byte[] fieldNameBytes = fieldName.getBytes();
        short totalLength = (short) (fieldNameBytes.length + 1 + 8);
        ByteBuffer byteBuffer = padWithZeros(totalLength);
        byteBuffer.putShort((short) byteBuffer.capacity());
        byteBuffer.put((byte) 64);
        byteBuffer.put(typeCode);
        byteBuffer.putShort((short) 0xFFFF);
        byteBuffer.putShort((short) 0xFFFF);
        byteBuffer.put((byte) fieldName.length());
        byteBuffer.put(fieldNameBytes);
        return byteBuffer.array();
    }

    private static byte[] encodeClusterDescriptor(PropertyContainer propertyContainer) throws IOException, CodecException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        byte typeCode = TypeLookup.getLVTypeCode();
        String fieldName = propertyContainer.getName();
        byte[] fieldNameBytes = fieldName.getBytes();
        String[] names = propertyContainer.getNames();
        short nTDs = (short) names.length;
        short totalLength = 0;

        for (String name : names) {
            byte[] nestedTypeDesc = encodeTypeDescriptor_bytes(propertyContainer.get(name));
            totalLength += nestedTypeDesc.length;
            dos.write(nestedTypeDesc);
        }

        totalLength += (short) (fieldNameBytes.length + 1 + 6);
        ByteBuffer byteBuffer = padWithZeros(totalLength);
        byteBuffer.putShort((short) byteBuffer.capacity());
        byteBuffer.put((byte) 64);
        byteBuffer.put(typeCode);
        byteBuffer.putShort(nTDs);
        byteBuffer.put(bos.toByteArray());
        byteBuffer.put((byte) fieldName.length());
        byteBuffer.put(fieldNameBytes);
        return byteBuffer.array();
    }

    private static byte[] encodeArrayDescriptor(FieldContainer field) throws IOException, CodecException {
        ValueType type = field.getValueType();
        ValueType componentType = type.getComponentType();
        int dimension = type.isArray2d() ? 2 : 1;
        String fieldName = field.getName();
        byte[] fieldNameBytes = fieldName.getBytes();
        byte typeCode = TypeLookup.getLVTypeCode(type);

        // element descriptor uses empty name — LabVIEW ignores it for array elements
        byte[] elemTypeDesc = encodeScalarDescriptor("", componentType);

        short totalLength = (short) (fieldNameBytes.length + 1 + 6 + (dimension * 4) + elemTypeDesc.length);
        ByteBuffer byteBuffer = padWithZeros(totalLength);
        byteBuffer.putShort((short) byteBuffer.capacity());
        byteBuffer.put((byte) 64);
        byteBuffer.put(typeCode);
        byteBuffer.putShort((short) dimension);
        for (int i = 0; i < dimension * 2; i++) {
            byteBuffer.putShort((short) 0xFFFF);
        }
        byteBuffer.put(elemTypeDesc);
        byteBuffer.put((byte) fieldName.length());
        byteBuffer.put(fieldNameBytes);
        return byteBuffer.array();
    }
}