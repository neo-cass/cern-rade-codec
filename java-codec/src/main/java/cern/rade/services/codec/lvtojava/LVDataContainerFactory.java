package cern.rade.services.codec.lvtojava;

import cern.rade.services.common.containers.java.ArrayContainer;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.java.PropertyContainer;
import cern.rade.services.common.containers.labview.LVClusterDescriptor;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;

import java.nio.ByteBuffer;

public final class LVDataContainerFactory {

    public static DataContainer decode(LVTypeDescriptor lvTypeDescriptor) {
        String name = lvTypeDescriptor.getName();
        switch(lvTypeDescriptor.getType()){
            case CLUSTER -> {
                return createPropertyContainer((LVClusterDescriptor) lvTypeDescriptor);}
            case INT_8, INT_16, INT_32, INT_64, UINT_8, UINT_16, UINT_32, BOOL, SGL, DBL, STRING -> {
                return new DataContainer(name);
            }
            case ARRAY -> {
                if lvTypeDescriptor.getType()
                return new ArrayContainer(name);
            }
        }
        return null;
    }

    public static PropertyContainer createPropertyContainer(LVClusterDescriptor lvClusterDescriptor){
        PropertyContainer propertyContainer = new PropertyContainer(lvClusterDescriptor.getName());
        for(LVTypeDescriptor lvTypeDescriptor : lvClusterDescriptor.getFieldDescriptors()){
            DataContainer field = decode(lvTypeDescriptor);
            propertyContainer.setDataContainer(field);
        }
        return propertyContainer;
    }
}
