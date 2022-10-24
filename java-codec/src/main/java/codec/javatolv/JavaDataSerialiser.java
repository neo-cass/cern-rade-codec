package codec.javatolv;

import cern.cmw.data.DataType;
import types.JavaDataContainer;

import java.nio.ByteBuffer;

public final class JavaDataSerialiser {

    public static byte[] serialise(JavaDataContainer javaDataContainer){
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[50]);
        switch(javaDataContainer.getDataType()){
            case DT_DATA -> {serialiseData(javaDataContainer);}
            default -> {serialiseNumeric(javaDataContainer, byteBuffer);}
        }
        return byteBuffer.array();
    }

    private static void serialiseData(JavaDataContainer javaDataContainer) {
        JavaDataContainer[] javaDataContainers = (JavaDataContainer[])javaDataContainer.getData();
        for(JavaDataContainer nestedJavaDataContainer : javaDataContainers){
            serialise(nestedJavaDataContainer);
        }
    }

    private static void serialiseNumeric(JavaDataContainer javaDataContainer, ByteBuffer byteBuffer){
        switch(javaDataContainer.getDataType()){
            case DT_BYTE -> byteBuffer.put((Byte) javaDataContainer.getData());
            case DT_SHORT -> byteBuffer.putShort((Short) javaDataContainer.getData());
            case DT_INT -> byteBuffer.putInt((Integer) javaDataContainer.getData());
            case DT_LONG -> byteBuffer.putLong((Long) javaDataContainer.getData());
            case DT_FLOAT -> byteBuffer.putFloat((Float) javaDataContainer.getData());
            case DT_DOUBLE -> byteBuffer.putDouble((Double) javaDataContainer.getData());
            case DT_BOOL -> byteBuffer.put((Byte) javaDataContainer.getData());
            case DT_STRING -> {
                byte[] rawString = ((String) javaDataContainer.getData()).getBytes();
                byteBuffer.putInt(rawString.length);
                byteBuffer.put(rawString);
            }
        }
    }
}
