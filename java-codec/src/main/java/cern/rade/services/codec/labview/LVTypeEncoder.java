package cern.rade.services.codec.labview;

import cern.japc.value.MapParameterValue;
import cern.japc.value.ParameterValue;
import cern.japc.value.SimpleParameterValue;
import cern.japc.value.Type;
import cern.japc.value.ValueType;
import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.TypeLookup;
import cern.rade.services.common.containers.labview.LVClusterDescriptor;
import cern.rade.services.common.containers.labview.LVType;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;
import static cern.rade.services.common.BytesUtil.byteArrToShortArr;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class LVTypeEncoder {

    public static LVTypeDescriptor describe(String name, ParameterValue value) {
        if (value.getType() == Type.MAP) {
            MapParameterValue map = value.castAsMap();
            String[] names = map.getNames();
            LVTypeDescriptor[] fields = new LVTypeDescriptor[names.length];
            for (int i = 0; i < names.length; i++) {
                fields[i] = describe(names[i], map.get(names[i]));
            }
            return new LVClusterDescriptor(name, fields);
        }
        return new LVTypeDescriptor(TypeLookup.getLVType(TypeLookup.getLVTypeCode(value.castAsSimple().getValueType())), name);
    }

    public static short[] encodeTypeDescriptor(LVTypeDescriptor desc, ParameterValue value) throws CodecException {
        try {
            return byteArrToShortArr(encodeTypeDescriptor_bytes(desc, value));
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

    private static byte[] encodeTypeDescriptor_bytes(LVTypeDescriptor desc, ParameterValue value) throws IOException, CodecException {
        if (desc.getType() == LVType.CLUSTER) {
            return encodeClusterDescriptor((LVClusterDescriptor) desc, value.castAsMap());
        } else {
            return encodeFieldDescriptor(desc.getName(), value.castAsSimple());
        }
    }

    private static byte[] encodeFieldDescriptor(String fieldName, SimpleParameterValue field) throws IOException, CodecException {
        ValueType type = field.getValueType();
        if (type.isScalar()) {
            return encodeScalarDescriptor(fieldName, type);
        } else if (type.isArray() || type.isArray2d()) {
            return encodeArrayDescriptor(fieldName, type);
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

    private static byte[] encodeClusterDescriptor(LVClusterDescriptor desc, MapParameterValue map) throws IOException, CodecException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        byte typeCode = TypeLookup.getLVClusterTypeCode();
        String fieldName = desc.getName();
        LVTypeDescriptor[] fieldDescs = desc.getFieldDescriptors();
        short nTDs = (short) fieldDescs.length;
        short totalLength = 0;

        for (LVTypeDescriptor fieldDesc : fieldDescs) {
            SimpleParameterValue field = map.get(fieldDesc.getName());
            if (field == null) {
                throw new CodecException("Field " + fieldDesc.getName() + " not found in value for cluster " + fieldName);
            }
            byte[] nestedTypeDesc = encodeFieldDescriptor(fieldDesc.getName(), field);
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

    private static byte[] encodeArrayDescriptor(String fieldName, ValueType type) throws IOException, CodecException {
        ValueType componentType = type.getComponentType();
        int dimension = type.isArray2d() ? 2 : 1;
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