package cern.rade.services.codec.javatolv;

import cern.cmw.data.DataType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;

import cern.japc.value.Array2D;
import cern.japc.value.Type;
import cern.japc.value.ValueType;
import cern.japc.value.spi.value.SimpleParameterValueImpl;
import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.java.ArrayContainer;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.java.FieldContainer;
import cern.rade.services.common.containers.java.PropertyContainer;
public final class LVDataEncoder {
    public static byte[] encode(DataContainer DataContainer) throws CodecException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            encode(DataContainer, dos);
        } catch (IOException e) {
            throw new CodecException("Cannot encode type ",e);
        }
        return bos.toByteArray();
    }

    private static void encode(DataContainer dataContainer, DataOutputStream dos) throws IOException, CodecException {
        if(dataContainer.getType() == Type.MAP){
            encodeMap((PropertyContainer) dataContainer, dos);
        }
        else {
            encodeSingle((FieldContainer) dataContainer, dos);
        }
    }

    private static void encodeSingle(FieldContainer field, DataOutputStream dos) throws CodecException, IOException {
        ValueType type = field.getValueType();
        if (type.isScalar()){
            serialiseScalar(field, dos);
        }
        else if (type.isArray() || type.isArray2d()){
            serialiseArray(field, dos);
        }
    }

    private static void encodeMap(PropertyContainer propertyContainer, DataOutputStream dos) throws IOException, CodecException {
        String[] names = propertyContainer.getNames();
        for(String name : names){
            dos.write(encode(propertyContainer.get(name)));
        }
    }

    private static void serialiseScalar(FieldContainer field, DataOutputStream dos) throws IOException, CodecException {
        serialisePrimitive(field.getObject(), field.getValueType(), dos);
    }

    private static void serialiseArray(FieldContainer field, DataOutputStream dos) throws IOException, CodecException {
        int[] dimensions;
        Object arr;
        if(field.getValueType().isArray2d()){
            Array2D array2D = field.getArray2D();
            arr = array2D.getArray1D();
            dimensions = new int[]{array2D.getRowCount(), array2D.getColumnCount()};
        }
        else {
            arr = field.getObject();
            dimensions = new int[] {field.getLength()};
        }
        for (int dimension : dimensions) {dos.writeInt(dimension);}
        ValueType type = field.getValueType().getComponentType();
        int len = Array.getLength(arr);
        for (int i = 0; i < len; i++) {
            serialisePrimitive(Array.get(arr, i), type, dos);
        }
    }

    private static void serialisePrimitive(Object value, ValueType type, DataOutputStream dos) throws IOException, CodecException {
        if (type.equals(ValueType.BYTE)) {
            dos.writeByte((Byte) value);
        } else if (type.equals(ValueType.BOOLEAN)) {
            dos.writeByte((Boolean) value ? 1 : 0);
        } else if (type.equals(ValueType.SHORT)) {
            dos.writeShort((Short) value);
        } else if (type.equals(ValueType.INT)) {
            dos.writeInt((Integer) value);
        } else if (type.equals(ValueType.LONG)) {
            dos.writeLong((Long) value);
        } else if (type.equals(ValueType.FLOAT)) {
            dos.writeFloat((Float) value);
        } else if (type.equals(ValueType.DOUBLE)) {
            dos.writeDouble((Double) value);
        } else if (type.equals(ValueType.STRING)) {
            byte[] rawString = ((String) value).getBytes();
            dos.writeInt(rawString.length);
            dos.write(rawString);
        } else {
            throw new CodecException("Unsupported type for serialisation: " + type);
        }
    }
}
