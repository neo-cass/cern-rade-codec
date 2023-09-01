package cern.rade.services.codec.lvtojava;

import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.TypeLookup;
import cern.rade.services.common.containers.java.ArrayContainer;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.java.PropertyContainer;
import cern.rade.services.common.containers.labview.LVArrayDescriptor;
import cern.rade.services.common.containers.labview.LVClusterDescriptor;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;

import java.util.Objects;


public final class LVDataContainerFactory {

    public static DataContainer createDataContainer(LVTypeDescriptor lvTypeDescriptor) throws CodecException {
        String name = lvTypeDescriptor.getName();
        switch(Objects.requireNonNull(lvTypeDescriptor.getType())){
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
            case ENUM_8, ENUM_16, ENUM_32 -> {throw new CodecException("Unimplemented type");}
            case VARIANT, UINT_64, REFNUM, WAVE_FORM, PATH, PICTURE,
                    EXT, EXT_CPX, EXT_PHS, EXT_PHS_CPX,
                    DBL_CPX, DBL_PHS, DBL_PHS_CPX,
                    SGL_PHS, SGL_CPX, SGL_PHS_CPX -> throw new CodecException("Unsupported type.");
            default -> throw new CodecException("Type descriptor has unknown type.");
        }
    }

    public static PropertyContainer createPropertyContainer(LVClusterDescriptor lvClusterDescriptor) throws CodecException {
        PropertyContainer propertyContainer = new PropertyContainer(lvClusterDescriptor.getName());
        for(LVTypeDescriptor lvTypeDescriptor : lvClusterDescriptor.getFieldDescriptors()){
            DataContainer field = createDataContainer(lvTypeDescriptor);
            propertyContainer.setDataContainer(lvTypeDescriptor.getName(), field);
        }
        return propertyContainer;
    }

    public static PropertyContainer[] createPropertyContainerArray(LVClusterDescriptor lvClusterDescriptor, int len) throws CodecException {
        PropertyContainer[] propertyContainers = new PropertyContainer[len];
        for(int i = 0; i<len; i++){
            propertyContainers[i] = createPropertyContainer(lvClusterDescriptor);
        }
        return propertyContainers;
    }
}
