package cern.rade.services.codec.labview;

import cern.japc.core.factory.MapParameterValueFactory;
import cern.japc.value.MapParameterValue;
import cern.japc.value.ParameterValue;
import cern.japc.value.SimpleDescriptor;
import cern.japc.value.SimpleParameterValue;
import cern.japc.value.ValueDescriptor;
import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.labview.LVArrayDescriptor;
import cern.rade.services.common.containers.labview.LVClusterDescriptor;
import cern.rade.services.common.containers.labview.LVType;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Decodes flattened LabVIEW data, as described by a LabVIEW type descriptor, into a JAPC value.
 * <p>
 * A cluster is decoded as a JAPC map (a property) of its fields, any other type as a single JAPC value. Everything is
 * decided from the LV type descriptor, except which JAPC enum type the enums and enum sets are items of: LabVIEW only
 * knows their item names. That one comes from the JAPC {@link ValueDescriptor} of the parameter, if any.
 * <p>
 * JAPC has no unsigned types: unsigned integers are decoded as the next larger signed JAPC type (U8 as short, U16 as
 * int, U32 as long), and U64 as a long (a value above {@link Long#MAX_VALUE} is an error).
 */
public final class LVDataDecoder {
    private LVDataDecoder() {
    }

    /**
     * Decodes data which contains no enum nor enum set, see {@link #decode(ByteBuffer, LVTypeDescriptor, ValueDescriptor)}.
     *
     * @param buffer           the flattened data (big endian)
     * @param lvTypeDescriptor the type descriptor of the data
     * @return a map parameter value if the type descriptor is a cluster, otherwise a simple parameter value.
     * @throws CodecException if the data does not match the type descriptor, or contains an enum or an enum set
     */
    public static ParameterValue decode(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor) throws CodecException {
        return decode(buffer, lvTypeDescriptor, null);
    }

    /**
     * Decodes flattened LabVIEW data, using the JAPC descriptor to find the JAPC type of the enums and enum sets.
     *
     * @param buffer           the flattened data (big endian)
     * @param lvTypeDescriptor the type descriptor of the data
     * @param descriptor     the JAPC descriptor of the parameter, consulted for enum and enum set fields only (their
     *                       LV items are mapped to the JAPC items of the enum type by name). If the type descriptor is
     *                       a cluster (a property) the descriptor is a map descriptor and the one of each field is
     *                       looked up by field name, otherwise it must be the simple descriptor of the field.
     *                       May be null if there is no enum nor enum set.
     * @return a map parameter value if the type descriptor is a cluster, otherwise a simple parameter value.
     * @throws CodecException if the data does not match the type descriptor, or a JAPC enum type is needed and is not
     *                        provided by the descriptor
     */
    public static ParameterValue decode(ByteBuffer buffer, LVTypeDescriptor lvTypeDescriptor, ValueDescriptor descriptor) throws CodecException {
        try {
            if (lvTypeDescriptor.getType() == LVType.CLUSTER) {
                return decodeCluster(buffer, (LVClusterDescriptor) lvTypeDescriptor, descriptor);
            }
            return decodeField(buffer, lvTypeDescriptor, EnumTypes.singleDescriptor(descriptor));
        } catch (BufferUnderflowException e) {
            throw new CodecException("Exception while decoding LabVIEW data, buffer underflow occurred while reading data. Check your type descriptor is correct and that you are providing the correct data.", e);
        }
    }

    private static MapParameterValue decodeCluster(ByteBuffer buffer, LVClusterDescriptor cluster, ValueDescriptor descriptor) throws CodecException {
        MapParameterValue map = MapParameterValueFactory.newMapParameterValue();
        for (LVTypeDescriptor field : cluster.getFieldDescriptors()) {
            map.put(field.getName(), decodeField(buffer, field, EnumTypes.fieldDescriptor(descriptor, field.getName())));
        }
        return map;
    }

    /**
     * @param descriptor the JAPC descriptor of the field, may be null
     */
    private static SimpleParameterValue decodeField(ByteBuffer buffer, LVTypeDescriptor field, SimpleDescriptor descriptor) throws CodecException {
        if (field instanceof LVArrayDescriptor array) {
            LVElement element = LVElements.forDecoding(LVArrays.elementDescriptor(array), descriptor, array.getName());
            return JapcValues.ofArray(LVArrays.read(buffer, array, element));
        }
        return JapcValues.ofScalar(LVElements.forDecoding(field, descriptor, field.getName()).read(buffer));
    }
}
