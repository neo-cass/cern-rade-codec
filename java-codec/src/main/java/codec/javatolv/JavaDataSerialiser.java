package codec.javatolv;

import types.JavaContainer;

import java.nio.ByteBuffer;
import java.util.Arrays;

public final class JavaDataSerialiser {
    private static int currentLen;
    public static byte[] serialise(JavaContainer javaContainer){
        currentLen = 0;
        ByteBuffer byteBuffer = ByteBuffer.allocate(256);
        switch(javaContainer.getDataType()){
            case DT_DATA -> {byteBuffer.put(serialiseData(javaContainer));}
            default -> {byteBuffer.put(serialiseNumeric(javaContainer));}
        }

        return Arrays.copyOfRange(byteBuffer.array(),0,currentLen);
    }

    private static byte[] serialiseData(JavaContainer javaContainer) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(128);
        JavaContainer[] javaContainers = (JavaContainer[]) javaContainer.getData();
        for(JavaContainer nestedJavaContainer : javaContainers){
            byteBuffer.put(serialise(nestedJavaContainer));
        }
        return byteBuffer.array();
    }

    private static byte[] serialiseNumeric(JavaContainer javaContainer){
        ByteBuffer byteBuffer = ByteBuffer.allocate(128);
        switch(javaContainer.getDataType()){
            case DT_BYTE -> {byteBuffer.put((Byte) javaContainer.getData()); currentLen+=1;}
            case DT_SHORT -> {byteBuffer.putShort((Short) javaContainer.getData()); currentLen+=2;}
            case DT_INT -> {byteBuffer.putInt((Integer) javaContainer.getData()); currentLen+=4;}
            case DT_LONG -> {byteBuffer.putLong((Long) javaContainer.getData()); currentLen+=8;}
            case DT_FLOAT -> {byteBuffer.putFloat((Float) javaContainer.getData());currentLen+=4;}
            case DT_DOUBLE -> {byteBuffer.putDouble((Double) javaContainer.getData()); currentLen+=8;}
            case DT_BOOL -> {byteBuffer.put((Byte) javaContainer.getData()); currentLen+=1;}
            case DT_STRING -> {
                byte[] rawString = ((String) javaContainer.getData()).getBytes();
                byteBuffer.putInt(rawString.length);
                byteBuffer.put(rawString);
                currentLen+=4 + rawString.length;
            }
        }
        return byteBuffer.array();
    }
}
