package cern.rade.services.codec.labview;

import cern.japc.value.MapParameterValue;
import cern.japc.value.ParameterValue;
import cern.japc.value.SimpleParameterValue;
import cern.japc.value.ValueType;
import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.labview.LVArrayDescriptor;
import cern.rade.services.common.containers.labview.LVClusterDescriptor;
import cern.rade.services.common.containers.labview.LVType;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Flattens a JAPC value as LabVIEW data (big endian), as described by a LabVIEW type descriptor.
 * <p>
 * A JAPC map is flattened as a cluster of its fields, any other value as a single field. The type descriptor is the
 * master: a JAPC number is converted to the LV numeric type of the descriptor if it is in its range (e.g. to an
 * unsigned type, which JAPC does not have, or to a smaller type), otherwise the encoding fails. Enums and enum sets
 * are written by item name: the position of the item with the same symbol in the LV type.
 */
public final class LVDataEncoder {
    private LVDataEncoder() {
    }

    /**
     * Flattens a JAPC value as LabVIEW data.
     *
     * @param value the JAPC value; a map if the type descriptor is a cluster, otherwise a simple value
     * @param desc  the LV type descriptor of the data to produce
     * @return the flattened data
     * @throws CodecException if the value does not match the type descriptor, or is not representable in LabVIEW
     */
    public static byte[] encode(ParameterValue value, LVTypeDescriptor desc) throws CodecException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            DataOutputStream out = new DataOutputStream(bytes);
            if (desc.getType() == LVType.CLUSTER) {
                encodeCluster(JapcValues.asMap(value), (LVClusterDescriptor) desc, out);
            } else {
                encodeField(JapcValues.asSimple(value), desc, out);
            }
        } catch (IOException e) {
            throw new CodecException("Cannot encode value", e);
        }
        return bytes.toByteArray();
    }

    private static void encodeCluster(MapParameterValue map, LVClusterDescriptor cluster, DataOutputStream out) throws IOException, CodecException {
        for (LVTypeDescriptor fieldDesc : cluster.getFieldDescriptors()) {
            SimpleParameterValue field = map.get(fieldDesc.getName());
            if (field == null) {
                throw new CodecException("Field " + fieldDesc.getName() + " not found in value for cluster " + cluster.getName());
            }
            encodeField(field, fieldDesc, out);
        }
    }

    private static void encodeField(SimpleParameterValue field, LVTypeDescriptor desc, DataOutputStream out) throws IOException, CodecException {
        ValueType type = JapcValues.valueTypeOf(field);
        if (JapcValues.isArray(type) != (desc instanceof LVArrayDescriptor)) {
            throw new CodecException("JAPC " + type + " '" + desc.getName() + "' does not match the LV type descriptor " + desc.getType());
        }
        if (desc instanceof LVArrayDescriptor array) {
            if (ValueType.DISCRETE_FUNCTION_LIST.equals(type) && !LVArrays.isDiscreteFunctionList(array)) {
                throw new CodecException("JAPC discrete function list '" + desc.getName() + "' requires a 1D array of one-element clusters holding a discrete function, got " + desc);
            }
            LVElement element = LVElements.forEncoding(LVArrays.elementDescriptor(array));
            LVArrays.write(out, array, JapcValues.flatten(field), element);
        } else {
            LVElements.forEncoding(desc).write(JapcValues.scalar(field), out);
        }
    }
}
