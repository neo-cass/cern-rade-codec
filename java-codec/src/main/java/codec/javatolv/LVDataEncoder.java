package codec.javatolv;

import types.JavaContainer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class LVDataEncoder {
    public static byte[] serialise(JavaContainer javaContainer) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        switch(javaContainer.getDataType()){
            case DT_DATA -> {serialiseData(javaContainer, dos);}
            default -> {serialiseNumeric(javaContainer,dos);}
        }

        return bos.toByteArray();
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
}
