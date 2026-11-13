package cern.rade.services.codec.labview;

import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.labview.LVArrayDescriptor;
import cern.rade.services.common.containers.labview.LVClusterDescriptor;
import cern.rade.services.common.containers.labview.LVType;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;

/**
 * Everything about LabVIEW arrays which does not depend on the type of their elements: the dimensions, the flattened
 * layout, and the discrete function list (an LV array of clusters of one discrete function).
 * <p>
 * Flattened format (big endian): one i32 per dimension, then the elements in row-major order.
 */
final class LVArrays {
    /**
     * Label of the single cluster element holding the discrete function in a discrete function list element.
     * The decoder accepts any label (or none) there.
     */
    static final String DISCRETE_FUNCTION_LIST_ELEMENT_LABEL = "function";

    private LVArrays() {
    }

    /**
     * The content of an array: its dimensions and its elements in row-major order, as a Java array (of primitives for
     * the numeric and boolean types).
     */
    record Data(int[] dimensions, Object flat) {
        int length() {
            return Array.getLength(flat);
        }
    }

    /**
     * A discrete function list is a 1D LV array (LabVIEW cannot nest an array in an array) of unlabelled clusters
     * with exactly one element: the discrete function, labelled {@link #DISCRETE_FUNCTION_LIST_ELEMENT_LABEL}.
     */
    static LVArrayDescriptor describeDiscreteFunctionList(String name) {
        LVTypeDescriptor function = new LVTypeDescriptor(LVType.DISCRETE_FUNCTION, DISCRETE_FUNCTION_LIST_ELEMENT_LABEL);
        return new LVArrayDescriptor(name, 1, new LVClusterDescriptor("", new LVTypeDescriptor[]{function}));
    }

    /**
     * @return true if the descriptor is a 1D array of clusters with exactly one element, which is a discrete function.
     * Any other array of clusters is not a discrete function list.
     */
    static boolean isDiscreteFunctionList(LVTypeDescriptor desc) {
        return desc instanceof LVArrayDescriptor array
                && array.getDimension() == 1
                && array.getElemDescriptor() instanceof LVClusterDescriptor cluster
                && cluster.getFieldDescriptors().length == 1
                && cluster.getFieldDescriptors()[0].getType() == LVType.DISCRETE_FUNCTION;
    }

    /**
     * @return the descriptor of the elements of the array as flattened in the data: for a discrete function list the
     * discrete function (a cluster of one element is flattened as that element), otherwise the element descriptor.
     * @throws CodecException if the array is an array of clusters which is not a discrete function list.
     */
    static LVTypeDescriptor elementDescriptor(LVArrayDescriptor array) throws CodecException {
        if (isDiscreteFunctionList(array)) {
            return ((LVClusterDescriptor) array.getElemDescriptor()).getFieldDescriptors()[0];
        }
        LVType elementType = array.getElemDescriptor().getType();
        if (elementType == LVType.CLUSTER || elementType == LVType.ARRAY || elementType == LVType.DISCRETE_FUNCTION) {
            throw new CodecException("Array '" + array.getName() + "' of " + elementType + " is not supported: only a 1D array of clusters of one discrete function is");
        }
        return array.getElemDescriptor();
    }

    /**
     * Reads the dimensions and the elements of an array.
     *
     * @param element the element of the array, see {@link #elementDescriptor(LVArrayDescriptor)}
     */
    static Data read(ByteBuffer buffer, LVArrayDescriptor array, LVElement element) throws CodecException {
        int[] dimensions = new int[array.getDimension()];
        long length = 1;
        for (int i = 0; i < dimensions.length; i++) {
            dimensions[i] = buffer.getInt();
            if (dimensions[i] < 0) {
                throw new CodecException("Array '" + array.getName() + "' has a negative dimension: " + dimensions[i]);
            }
            length *= dimensions[i];
            if (length > buffer.remaining()) { // an element takes at least a byte
                throw new CodecException("Array '" + array.getName() + "' has more elements than the available data");
            }
        }
        Object flat = Array.newInstance(element.javaType(), (int) length);
        for (int i = 0; i < length; i++) {
            Array.set(flat, i, element.read(buffer));
        }
        return new Data(dimensions, flat);
    }

    /**
     * Writes the dimensions and the elements of an array.
     *
     * @param element the element of the array, see {@link #elementDescriptor(LVArrayDescriptor)}
     * @throws CodecException if the number of dimensions of the data is not the one of the LV array
     */
    static void write(DataOutputStream out, LVArrayDescriptor array, Data data, LVElement element) throws IOException, CodecException {
        if (data.dimensions().length != array.getDimension()) {
            throw new CodecException("Array '" + array.getName() + "' has " + array.getDimension() + " dimension(s) in LabVIEW but the JAPC value has " + data.dimensions().length);
        }
        for (int dimension : data.dimensions()) {
            out.writeInt(dimension);
        }
        for (int i = 0; i < data.length(); i++) {
            element.write(Array.get(data.flat(), i), out);
        }
    }
}
