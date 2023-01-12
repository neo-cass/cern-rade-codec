package codec.javatolv;

import cern.cmw.data.DataType;
import types.JavaContainer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;

public final class LVDataEncoder {
    public static byte[] serialise(JavaContainer javaContainer) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        switch(javaContainer.getDataType()){
            case DT_DATA -> {serialiseData(javaContainer, dos);}
            case DT_BYTE, DT_BOOL, DT_SHORT, DT_INT, DT_LONG, DT_FLOAT, DT_DOUBLE, DT_STRING -> serialiseNumeric(javaContainer,dos);
            case DT_BYTE_ARRAY, DT_BOOL_ARRAY, DT_SHORT_ARRAY, DT_INT_ARRAY, DT_LONG_ARRAY, DT_FLOAT_ARRAY, DT_DOUBLE_ARRAY, DT_STRING_ARRAY,
                    DT_BYTE_ARRAY_2D, DT_BOOL_ARRAY_2D, DT_SHORT_ARRAY_2D, DT_INT_ARRAY_2D, DT_LONG_ARRAY_2D, DT_FLOAT_ARRAY_2D, DT_DOUBLE_ARRAY_2D, DT_STRING_ARRAY_2D,
                    DT_BYTE_MULTI_ARRAY, DT_BOOL_MULTI_ARRAY, DT_SHORT_MULTI_ARRAY, DT_INT_MULTI_ARRAY, DT_LONG_MULTI_ARRAY, DT_FLOAT_MULTI_ARRAY, DT_DOUBLE_MULTI_ARRAY, DT_STRING_MULTI_ARRAY -> serialiseArray(javaContainer,dos);
        }

        return bos.toByteArray();
    }

    private static int dimensionOf(Object arr) {
        int dimensionCount = 0;
        Class c = arr.getClass(); // getting the runtime class of an object
        while (c.isArray()) // check whether the object is an array {
            c = c.getComponentType(); // returns the class denoting the component type of the array
        dimensionCount++;
        return dimensionCount;
    }

    private static Object getArrayElem(Object arr) {
        if(!arr.getClass().isArray()){return null;}

        Object data = Array.get(arr, 0);
        while (data.getClass().isArray()) {
            data = Array.get(data, 0);
        }
        return data;
    }

    private static void serialiseData(JavaContainer<JavaContainer[]> javaContainer, DataOutputStream dos) throws IOException {
        JavaContainer[] javaContainers = javaContainer.getData();
        for(JavaContainer nestedJavaContainer : javaContainers){
            dos.write(serialise(nestedJavaContainer));
        }
    }

    private static void serialiseNumeric(JavaContainer javaContainer, DataOutputStream dos) throws IOException {
        switch(javaContainer.getDataType()){
            case DT_BYTE, DT_BOOL -> {dos.writeByte((Byte) javaContainer.getData()); }
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

    private static void serialiseArray(JavaContainer javaContainer, DataOutputStream dos) throws IOException {
        Object data = javaContainer.getData();
        serialiseJavaContainerArray(data, dos);
    }

    private static void serialiseJavaContainerArray(Object arr, DataOutputStream dos) throws IOException {
        if (dimensionOf(arr)==1){
            serialise1DArr(arr, dos);
            return;
        }
        int dimLength=Array.getLength(arr);
        dos.writeInt(dimLength);
        for (int i = 0; i<dimLength; i++){
            serialiseJavaContainerArray(Array.get(arr, i), dos);
        }
    }

    private static void serialise1DArr(Object arr, DataOutputStream dos) throws IOException {
        int dimLength = Array.getLength(arr);
        dos.writeInt(dimLength);
        for(int i = 0; i<dimLength; i++){
            dos.write(serialise((JavaContainer) Array.get(arr, i)));
        }
    }
}
