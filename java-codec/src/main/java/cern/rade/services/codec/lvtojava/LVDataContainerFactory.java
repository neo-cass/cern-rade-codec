package cern.rade.services.codec.lvtojava;

import cern.cmw.data.DataType;
import cern.japc.core.MapParameterDescriptor;
import cern.japc.core.ParameterDescriptor;
import cern.japc.core.SimpleParameterDescriptor;
import cern.japc.core.spi.MapParameterDescriptorImpl;
import cern.japc.core.spi.ParameterDescriptorImpl;
import cern.japc.core.spi.ParameterDescriptorSupport;
import cern.japc.core.spi.SimpleParameterDescriptorImpl;
import cern.japc.core.spi.value.MapDescriptorImpl;
import cern.japc.core.spi.value.SimpleDescriptorImpl;
import cern.japc.value.*;
import cern.japc.value.spi.value.SimpleParameterValueImpl;
import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.TypeLookup;
import cern.rade.services.common.containers.java.ArrayContainer;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.java.FieldContainer;
import cern.rade.services.common.containers.java.PropertyContainer;
import cern.rade.services.common.containers.labview.LVArrayDescriptor;
import cern.rade.services.common.containers.labview.LVClusterDescriptor;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;


public final class LVDataContainerFactory {

    public static DataContainer createDataContainer(LVTypeDescriptor lvTypeDescriptor) throws CodecException {
        String name = lvTypeDescriptor.getName();
        switch(Objects.requireNonNull(lvTypeDescriptor.getType())){
            case CLUSTER -> {
                return createPropertyContainer((LVClusterDescriptor) lvTypeDescriptor);
            }
            case INT_8, INT_16, INT_32, INT_64, UINT_8, UINT_16, UINT_32, BOOL, SGL, DBL, STRING, ARRAY -> {
                return new FieldContainer(name);
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
        PropertyContainer property = new PropertyContainer(lvClusterDescriptor.getName());

        for(LVTypeDescriptor lvTypeDescriptor : lvClusterDescriptor.getFieldDescriptors()){
            ParameterValue simple = createDataContainer(lvTypeDescriptor);
            property.put(lvTypeDescriptor.getName(), simple.castAsSimple());
            property.setValueType(lvTypeDescriptor.getName(), TypeLookup.getJAPCType(lvTypeDescriptor.getType()));
        }
        return property;
    }

    public static FieldContainer createSimpleParameterValue(LVTypeDescriptor lvTypeDescriptor) {
        return new FieldContainer(lvTypeDescriptor.getName());
    }

}
