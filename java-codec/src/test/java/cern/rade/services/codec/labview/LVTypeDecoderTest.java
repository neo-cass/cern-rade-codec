package cern.rade.services.codec.labview;

import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.labview.LVArrayDescriptor;
import cern.rade.services.common.containers.labview.LVClusterDescriptor;
import cern.rade.services.common.containers.labview.LVType;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Malformed type descriptors must raise CodecException, never an unchecked exception. */
class LVTypeDecoderTest {

    private static void assertMalformed(byte[] bytes) {
        assertThrows(CodecException.class, () -> LVTypeDecoder.decode(ByteBuffer.wrap(bytes)));
    }

    @Test
    void clusterWithHugeElementCountIsRejected() {
        assertMalformed(new byte[]{0, 6, 0, 0x50, (byte) 0xFF, (byte) 0xFF});
    }

    @Test
    void clusterDeclaringMoreElementsThanFollowIsRejected() throws CodecException {
        byte[] bytes = LVTypeEncoder.encodeTypeDescriptorBytes(
                new LVClusterDescriptor("", new LVTypeDescriptor[]{new LVTypeDescriptor(LVType.INT_32, "a")}));
        bytes[5] = 3; // element count, big endian at offset 4
        assertMalformed(bytes);
    }

    @Test
    void arrayWithHugeDimensionCountIsRejected() {
        assertMalformed(new byte[]{0, 6, 0, 0x40, (byte) 0xFF, (byte) 0xFF});
    }

    @Test
    void arrayWithMissingDimensionSizesIsRejected() {
        assertMalformed(new byte[]{0, 10, 0, 0x40, 0, 2, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
    }

    @Test
    void truncatedBuffersAreRejected() {
        assertMalformed(new byte[0]);
        assertMalformed(new byte[]{0, 3, 0});
    }

    @Test
    void validClusterStillDecodes() throws CodecException {
        byte[] bytes = LVTypeEncoder.encodeTypeDescriptorBytes(
                new LVClusterDescriptor("cl", new LVTypeDescriptor[]{new LVTypeDescriptor(LVType.INT_32, "count")}));
        LVClusterDescriptor decoded = (LVClusterDescriptor) LVTestSupport.decodeTypeDescriptor(bytes);
        assertEquals("cl", decoded.getName());
        assertEquals(LVType.CLUSTER, decoded.getType());
        assertEquals(1, decoded.getFieldDescriptors().length);
        assertEquals("count", decoded.getFieldDescriptors()[0].getName());
        assertEquals(LVType.INT_32, decoded.getFieldDescriptors()[0].getType());
    }

    @Test
    void validArrayStillDecodes() throws CodecException {
        byte[] bytes = LVTypeEncoder.encodeTypeDescriptorBytes(
                new LVArrayDescriptor("arr", 1, new LVTypeDescriptor(LVType.DBL, "")));
        LVArrayDescriptor decoded = (LVArrayDescriptor) LVTestSupport.decodeTypeDescriptor(bytes);
        assertEquals("arr", decoded.getName());
        assertEquals(LVType.ARRAY, decoded.getType());
        assertEquals(1, decoded.getDimension());
        assertEquals(LVType.DBL, decoded.getElemDescriptor().getType());
    }
}
