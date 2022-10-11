package codec.lvtojava;

import types.JavaDataContainer;
import types.labview.ClusterDescriptor;
import types.labview.LVType;
import types.labview.LVTypeDescriptor;

import java.nio.ByteBuffer;

public class LVDataSerialiserImp {
    private ByteBuffer buffer;
    public LVDataSerialiserImp(ByteBuffer buffer){
        this.buffer = buffer;
    }

    private JavaDataContainer deserialiseCluster(LVTypeDescriptor lvTypeDescriptor){
        ClusterDescriptor clusterDescriptor = (ClusterDescriptor) lvTypeDescriptor.getTypeSpecificInfo();
        int nTDs = clusterDescriptor.getnTypeDescriptors();
        LVTypeDescriptor[] lvTypeDescriptors = clusterDescriptor.getTypeDescriptors();
        Object[] items = new Object[nTDs];
        JavaDataContainer[] javaData = new JavaDataContainer[nTDs];
        for (int i = 0; i< nTDs; i++){
            javaData[i] = deserialise(lvTypeDescriptors[i]);
        }
        return new JavaDataContainer(lvTypeDescriptor.getFieldName(), javaData);
    }

    private JavaDataContainer deserialiseNumeric(LVTypeDescriptor lvTypeDescriptor){
        LVType type = lvTypeDescriptor.getLvType();
        Object javaData = null;
        switch(type){
            case INT_8 -> {javaData = buffer.get();}
            case INT_16 -> {javaData = buffer.getShort();}
            case INT_32 -> {javaData = buffer.getInt();}
            case INT_64 -> {javaData = buffer.getLong();}
            case UINT_8 -> {javaData = buffer.getShort();}
            case UINT_16 -> {javaData = buffer.getInt();}
            case UINT_32 -> {javaData = buffer.getLong();}
            case UINT_64 -> {javaData = buffer.getLong();}//TODO: choose different datatype
            case SGL -> {javaData = buffer.getFloat();}
            case DBL -> {javaData = buffer.getDouble();}
            case BOOL -> {javaData = buffer.get();}
        }
        System.out.println(javaData);

        return new JavaDataContainer(lvTypeDescriptor.getFieldName(), javaData);

    }

    private JavaDataContainer deserialiseString(LVTypeDescriptor lvTypeDescriptor){
        int stringLen = buffer.getInt();
        System.out.println("SIZE: "+ stringLen);
        byte[] rawString = new byte[stringLen];
        buffer.get(rawString);
        String string = new String(rawString);
        System.out.println(string);
        return new JavaDataContainer(lvTypeDescriptor.getFieldName(), string);
    }

    public JavaDataContainer deserialise(LVTypeDescriptor lvTypeDescriptor){
        JavaDataContainer javaDataContainer = null;
        switch(lvTypeDescriptor.getLvType()){
            case CLUSTER -> {javaDataContainer = deserialiseCluster(lvTypeDescriptor);}
            case INT_8, INT_16, INT_32, INT_64, UINT_8, UINT_16, UINT_32, UINT_64, BOOL -> {
                javaDataContainer = deserialiseNumeric(lvTypeDescriptor);
            }
            case STRING -> {deserialiseString(lvTypeDescriptor);}
        }
        return javaDataContainer;
    }
}
