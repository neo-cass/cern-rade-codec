package codec.javatolv;

import cern.cmw.data.DataType;
import types.JavaDataContainer;

import java.nio.ByteBuffer;

public final class JavaDataSerialiser {

    public static byte[] serialise(JavaDataContainer javaDataContainer){
        ByteBuffer
        switch(javaDataContainer.getDataType()){
            case DT_DATA -> {serialiseData();}
        }
        return null;
    }

    private static byte[] serialiseData(JavaDataContainer javaDataContainer) {

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
            case DT_STRING -> byteBuffer.put(((String) javaDataContainer.getData()).getBytes());
        }
    }
}
