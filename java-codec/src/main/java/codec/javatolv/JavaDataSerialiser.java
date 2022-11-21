package codec.javatolv;

import cern.cmw.data.DataType;
import types.JavaDataContainer;

import java.nio.ByteBuffer;
import java.util.Arrays;

public final class JavaDataSerialiser {
    private static int currentLen;
    public static byte[] serialise(JavaDataContainer javaDataContainer){
        currentLen = 0;
        ByteBuffer byteBuffer = ByteBuffer.allocate(256);
        switch(javaDataContainer.getDataType()){
            case DT_DATA -> {byteBuffer.put(serialiseData(javaDataContainer));}
            default -> {byteBuffer.put(serialiseNumeric(javaDataContainer));}
        }

        return Arrays.copyOfRange(byteBuffer.array(),0,currentLen);
    }

    private static byte[] serialiseData(JavaDataContainer javaDataContainer) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(128);
        JavaDataContainer[] javaDataContainers = (JavaDataContainer[])javaDataContainer.getData();
        for(JavaDataContainer nestedJavaDataContainer : javaDataContainers){
            byteBuffer.put(serialise(nestedJavaDataContainer));
        }
        return byteBuffer.array();
    }

    private static byte[] serialiseNumeric(JavaDataContainer javaDataContainer){
        ByteBuffer byteBuffer = ByteBuffer.allocate(128);
        switch(javaDataContainer.getDataType()){
            case DT_BYTE -> {byteBuffer.put((Byte) javaDataContainer.getData()); currentLen+=1;}
            case DT_SHORT -> {byteBuffer.putShort((Short) javaDataContainer.getData()); currentLen+=2;}
            case DT_INT -> {byteBuffer.putInt((Integer) javaDataContainer.getData()); currentLen+=4;}
            case DT_LONG -> {byteBuffer.putLong((Long) javaDataContainer.getData()); currentLen+=8;}
            case DT_FLOAT -> {byteBuffer.putFloat((Float) javaDataContainer.getData());currentLen+=4;}
            case DT_DOUBLE -> {byteBuffer.putDouble((Double) javaDataContainer.getData()); currentLen+=8;}
            case DT_BOOL -> {byteBuffer.put((Byte) javaDataContainer.getData()); currentLen+=1;}
            case DT_STRING -> {
                byte[] rawString = ((String) javaDataContainer.getData()).getBytes();
                byteBuffer.putInt(rawString.length);
                byteBuffer.put(rawString);
                currentLen+=4 + rawString.length;
            }
        }
        return byteBuffer.array();
    }
}
