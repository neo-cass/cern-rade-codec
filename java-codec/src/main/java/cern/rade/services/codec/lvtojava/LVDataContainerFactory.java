package cern.rade.services.codec.lvtojava;

import cern.rade.services.common.containers.TypeLookup;
import cern.rade.services.common.containers.java.ArrayContainer;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.java.PropertyContainer;
import cern.rade.services.common.containers.labview.LVArrayDescriptor;
import cern.rade.services.common.containers.labview.LVClusterDescriptor;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;


public final class LVDataContainerFactory {

    public static DataContainer createDataContainer(LVTypeDescriptor lvTypeDescriptor) {
        String name = lvTypeDescriptor.getName();
        switch(lvTypeDescriptor.getType()){
            case CLUSTER -> {
                return createPropertyContainer((LVClusterDescriptor) lvTypeDescriptor);
            }
            case INT_8, INT_16, INT_32, INT_64, UINT_8, UINT_16, UINT_32, BOOL, SGL, DBL, STRING -> {
                return new DataContainer(name, TypeLookup.getCMWType(lvTypeDescriptor.getType()));
            }
            case ARRAY -> {
                LVArrayDescriptor arrayDescriptor = (LVArrayDescriptor) lvTypeDescriptor;
                return new ArrayContainer(name, arrayDescriptor.getElemDescriptor().getName(), TypeLookup.getCMWType(lvTypeDescriptor.getType()));
            }
            default -> throw new RuntimeException("");
        }
    }

    public static PropertyContainer createPropertyContainer(LVClusterDescriptor lvClusterDescriptor){
        PropertyContainer propertyContainer = new PropertyContainer(lvClusterDescriptor.getName());
        for(LVTypeDescriptor lvTypeDescriptor : lvClusterDescriptor.getFieldDescriptors()){
            DataContainer field = createDataContainer(lvTypeDescriptor);
            propertyContainer.setDataContainer(lvTypeDescriptor.getName(), field);
        }
        return propertyContainer;
    }

    public static PropertyContainer[] createPropertyContainerArray(LVClusterDescriptor lvClusterDescriptor, int len){
        PropertyContainer[] propertyContainers = new PropertyContainer[len];
        for(int i = 0; i<len; i++){
            propertyContainers[i] = createPropertyContainer(lvClusterDescriptor);
        }
        return propertyContainers;
    }
}
