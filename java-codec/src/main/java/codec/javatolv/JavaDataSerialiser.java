package codec.javatolv;

import cern.cmw.data.DataType;
import types.JavaContainer;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public final class JavaDataSerialiser {
    public static byte[] serialise(JavaContainer javaContainer){
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        switch(javaContainer.getDataType()){
            case DT_DATA -> {byteBuffer.put(serialiseData(javaContainer));}
            default -> {byteBuffer.put(serialiseNumeric(javaContainer));}
        }

        return Arrays.copyOfRange(byteBuffer.array(),0,currentLen);
    }

    private static void serialiseData(JavaContainer javaContainer, ByteArrayOutputStream byteStream) {

        JavaContainer[] javaContainers = (JavaContainer[]) javaContainer.getData();
        for(JavaContainer nestedJavaContainer : javaContainers){
            byteStream.writeBytes(serialise(nestedJavaContainer));
        }
    }

    private static void serialiseNumeric(JavaContainer javaContainer, ByteBuffer buffer){
        switch(javaContainer.getDataType()){
            case DT_BYTE -> {buffer.put((Byte) javaContainer.getData()); }
            case DT_SHORT -> {buffer.putShort((Short) javaContainer.getData()); }
            case DT_INT -> {buffer.putInt((Integer) javaContainer.getData()); }
            case DT_LONG -> {buffer.putLong((Long) javaContainer.getData());}
            case DT_FLOAT -> {buffer.putFloat((Float) javaContainer.getData());}
            case DT_DOUBLE -> {buffer.putDouble((Double) javaContainer.getData()); }
            case DT_BOOL -> {buffer.put((Byte) javaContainer.getData()); }
            case DT_STRING -> {
                byte[] rawString = ((String) javaContainer.getData()).getBytes();
                buffer.putInt(rawString.length);
                buffer.put(rawString);

            }
        }

    }
}
