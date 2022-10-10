package codec.lvtojava;

import types.*;
import types.labview.ClusterDescriptor;
import types.labview.LVType;
import types.labview.LVTypeDescriptor;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class LVDeserialiser {

    public LVDeserialiser(){


    }

    private ArrayList<DataContainer> deserialiseCluster(IntermediateDataContainer intermediateDataContainer){
        ArrayList<DataContainer> javaDatums = new ArrayList<>();
        ByteBuffer buffer = intermediateDataContainer.getByteBuffer();
        LVTypeDescriptor LVTypeDescriptor = intermediateDataContainer.getTypeDescriptor();
        ClusterDescriptor clusterDescriptor = (ClusterDescriptor) LVTypeDescriptor.getTypeSpecificInfo();
        LVTypeDescriptor[] lvTypeDescriptors = clusterDescriptor.getTypeDescriptors();
        for (int i = 0; i<clusterDescriptor.getnTypeDescriptors(); i++){
            IntermediateDataContainer nestedContainer = new IntermediateDataContainer(buffer, lvTypeDescriptors[i]);
            javaDatums.addAll(deserialise(nestedContainer));
        }
        return javaDatums;
    }

    private DataContainer decodeNumeric(IntermediateDataContainer intermediateDataContainer){
        ByteBuffer buffer = intermediateDataContainer.getByteBuffer();
        LVTypeDescriptor lvTypeDescriptor = intermediateDataContainer.getTypeDescriptor();
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
            case BOOL -> {javaData = buffer.getShort();}

        }
        DataContainer dataContainer = new DataContainer(javaData, lvTypeDescriptor);
        return dataContainer;

    }

    public ArrayList<DataContainer> deserialise(IntermediateDataContainer intermediateDataContainer){
        ByteBuffer buffer = intermediateDataContainer.getByteBuffer();
        LVTypeDescriptor LVTypeDescriptor = intermediateDataContainer.getTypeDescriptor();
        LVType type = LVTypeDescriptor.getLvType();
        ArrayList<DataContainer> data = new ArrayList<>();
        switch(type){
            case INT_8, INT_16, INT_32, INT_64, UINT_8, UINT_16, UINT_32, UINT_64 -> {data.add(decodeNumeric(intermediateDataContainer));}
            case CLUSTER -> {data.addAll(deserialiseCluster(intermediateDataContainer));}
            case ARRAY -> {;}
        }

        return data;
    }


}
