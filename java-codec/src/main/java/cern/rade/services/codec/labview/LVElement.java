package cern.rade.services.codec.labview;

import cern.rade.services.codec.CodecException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Flattening and unflattening of one LabVIEW element (a field, or an element of an array) as its JAPC counterpart.
 * <p>
 * The JAPC counterpart is a boxed value ({@link Byte}, {@link Boolean}, {@link String}...), a {@link cern.japc.value.EnumItem},
 * a {@link cern.japc.value.EnumItemSet} or a {@link cern.japc.value.DiscreteFunction}. Instances are created by {@link LVElements}.
 */
interface LVElement {
    /**
     * @return the type of the components of a Java array of values of this element (e.g. {@code byte.class} for I8).
     */
    Class<?> javaType();

    /**
     * Reads the next element from the (flattened) LabVIEW data.
     */
    Object read(ByteBuffer buffer) throws CodecException;

    /**
     * Writes the element as (flattened) LabVIEW data.
     *
     * @param value the JAPC counterpart of the element
     * @throws CodecException if the value cannot be written as this element
     */
    void write(Object value, DataOutputStream out) throws IOException, CodecException;
}
