package cern.rade.services.codec.labview;

import cern.japc.core.factory.SimpleParameterValueFactory;
import cern.japc.value.ParameterValue;
import cern.japc.value.SimpleParameterValue;
import cern.japc.value.ValueDescriptor;
import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Fixtures shared by the codec tests.
 */
final class LVTestSupport {
    private LVTestSupport() {
    }

    /**
     * Full LabVIEW round trip: describe, flatten the type descriptor, unflatten it, flatten the data, unflatten the data.
     *
     * @param descriptor the JAPC descriptor given to the decoder, may be null
     */
    static ParameterValue roundTrip(String name, ParameterValue value, ValueDescriptor descriptor) throws CodecException {
        return roundTrip(LVTypeEncoder.describe(name, value), value, descriptor);
    }

    /**
     * As {@link #roundTrip(String, ParameterValue, ValueDescriptor)} for a given LV type descriptor.
     */
    static ParameterValue roundTrip(LVTypeDescriptor described, ParameterValue value, ValueDescriptor descriptor) throws CodecException {
        LVTypeDescriptor decodedDesc = LVTypeDecoder.decode(ByteBuffer.wrap(LVTypeEncoder.encodeTypeDescriptorBytes(described)));
        ByteBuffer buffer = ByteBuffer.wrap(LVDataEncoder.encode(value, decodedDesc));
        ParameterValue decoded = LVDataDecoder.decode(buffer, decodedDesc, descriptor);
        assertFalse(buffer.hasRemaining(), "all data should be consumed");
        return decoded;
    }

    static LVTypeDescriptor decodeTypeDescriptor(byte[] bytes) throws CodecException {
        return LVTypeDecoder.decode(ByteBuffer.wrap(bytes));
    }

    static ParameterValue decodeData(byte[] data, LVTypeDescriptor desc, ValueDescriptor descriptor) throws CodecException {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        ParameterValue decoded = LVDataDecoder.decode(buffer, desc, descriptor);
        assertFalse(buffer.hasRemaining(), "all data should be consumed");
        return decoded;
    }

    static SimpleParameterValue simple(Object value) {
        return SimpleParameterValueFactory.newSimpleParameterValue(value);
    }

    /** Parses a hexadecimal string, spaces are ignored. */
    static byte[] hex(String s) {
        String clean = s.replaceAll("\\s", "");
        byte[] out = new byte[clean.length() / 2];
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) Integer.parseInt(clean.substring(2 * i, 2 * i + 2), 16);
        }
        return out;
    }
}
