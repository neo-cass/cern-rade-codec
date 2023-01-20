package codec.javatolv;

import cern.cmw.data.DataType;
import types.ArrayContainer;
import types.JavaContainer;
import types.PropertyContainer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;

public final class LVDataEncoder {
    public static byte[] serialise(JavaContainer javaContainer) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        switch(javaContainer.getDataType()){
            case DT_DATA -> {serialiseData((PropertyContainer) javaContainer, dos);}
            case DT_BYTE, DT_BOOL, DT_SHORT, DT_INT, DT_LONG, DT_FLOAT, DT_DOUBLE, DT_STRING -> serialiseSingle(javaContainer,dos);
            case DT_BYTE_ARRAY, DT_BOOL_ARRAY, DT_SHORT_ARRAY, DT_INT_ARRAY, DT_LONG_ARRAY, DT_FLOAT_ARRAY, DT_DOUBLE_ARRAY, DT_STRING_ARRAY, DT_DATA_ARRAY ,
                    DT_BYTE_ARRAY_2D, DT_BOOL_ARRAY_2D, DT_SHORT_ARRAY_2D, DT_INT_ARRAY_2D, DT_LONG_ARRAY_2D, DT_FLOAT_ARRAY_2D, DT_DOUBLE_ARRAY_2D, DT_STRING_ARRAY_2D, DT_DATA_ARRAY_2D,
                    DT_BYTE_MULTI_ARRAY, DT_BOOL_MULTI_ARRAY, DT_SHORT_MULTI_ARRAY, DT_INT_MULTI_ARRAY, DT_LONG_MULTI_ARRAY, DT_FLOAT_MULTI_ARRAY, DT_DOUBLE_MULTI_ARRAY, DT_STRING_MULTI_ARRAY, DT_DATA_MULTI_ARRAY -> serialiseArray((ArrayContainer) javaContainer,dos);
        }

        return bos.toByteArray();
    }

    private static void serialiseData(PropertyContainer propertyContainer, DataOutputStream dos) throws IOException {
        JavaContainer[] nestedContainers = propertyContainer.getData();
        for(JavaContainer nestedJavaContainer : nestedContainers){
            dos.write(serialise(nestedJavaContainer));
        }
    }

    private static void serialiseSingle(JavaContainer javaContainer, DataOutputStream dos) throws IOException {
        switch(javaContainer.getDataType()){
            case DT_BYTE -> {dos.writeByte((Byte) javaContainer.getData()); }
            case DT_BOOL -> {
                boolean bool = (Boolean) javaContainer.getData();
                byte boolByte = 0;
                if (bool){boolByte = 1;}
                dos.writeByte(boolByte);
            }
            case DT_SHORT -> {dos.writeShort((Short) javaContainer.getData()); }
            case DT_INT -> {dos.writeInt((Integer) javaContainer.getData()); }
            case DT_LONG -> {dos.writeLong((Long) javaContainer.getData());}
            case DT_FLOAT -> {dos.writeFloat((Float) javaContainer.getData());}
            case DT_DOUBLE -> {dos.writeDouble((Double) javaContainer.getData()); }
            case DT_STRING -> {
                byte[] rawString = ((String) javaContainer.getData()).getBytes();
                dos.writeInt(rawString.length);
                dos.write(rawString);
            }
        }
    }

    private static void serialiseArray(ArrayContainer arrayContainer, DataOutputStream dos) throws IOException {
        int[] dimensions = arrayContainer.getDimensions();
        JavaContainer[] data = arrayContainer.getData();
        for (int dimension : dimensions) {
            dos.writeInt(dimension);
        }
        for (JavaContainer datum : data) {
            dos.write(serialise(datum));
        }
    }
}
