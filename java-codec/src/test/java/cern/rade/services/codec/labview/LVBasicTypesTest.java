package cern.rade.services.codec.labview;

import cern.japc.core.factory.SimpleParameterValueFactory;
import cern.japc.value.SimpleParameterValue;
import cern.japc.value.ValueType;
import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.labview.LVArrayDescriptor;
import cern.rade.services.common.containers.labview.LVType;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static cern.rade.services.codec.labview.LVTestSupport.decodeData;
import static cern.rade.services.codec.labview.LVTestSupport.hex;
import static cern.rade.services.codec.labview.LVTestSupport.roundTrip;
import static cern.rade.services.codec.labview.LVTestSupport.simple;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The scalars and arrays of the types which JAPC and LabVIEW have in common.
 */
class LVBasicTypesTest {
    private static SimpleParameterValue roundTripped(SimpleParameterValue value) throws CodecException {
        return roundTrip("v", value, null).castAsSimple();
    }

    @Test
    void scalarsRoundTrip() throws CodecException {
        assertEquals(ValueType.BYTE, roundTripped(simple((byte) -5)).getValueType());
        assertEquals(-5, roundTripped(simple((byte) -5)).getByte());
        assertEquals(-300, roundTripped(simple((short) -300)).getShort());
        assertEquals(70000, roundTripped(simple(70000)).getInt());
        assertEquals(1L << 40, roundTripped(simple(1L << 40)).getLong());
        assertEquals(1.5f, roundTripped(simple(1.5f)).getFloat());
        assertEquals(-2.25, roundTripped(simple(-2.25)).getDouble());
        assertTrue(roundTripped(simple(true)).getBoolean());
        assertFalse(roundTripped(simple(false)).getBoolean());
        assertEquals("héllo", roundTripped(simple("héllo")).getString());
    }

    @Test
    void arraysRoundTrip() throws CodecException {
        assertArrayEquals(new byte[]{1, -2}, roundTripped(simple(new byte[]{1, -2})).getBytes());
        assertArrayEquals(new short[]{1, -2}, roundTripped(simple(new short[]{1, -2})).getShorts());
        assertArrayEquals(new int[]{1, -2}, roundTripped(simple(new int[]{1, -2})).getInts());
        assertArrayEquals(new long[]{1, -2}, roundTripped(simple(new long[]{1, -2})).getLongs());
        assertArrayEquals(new float[]{1.5f, -2}, roundTripped(simple(new float[]{1.5f, -2})).getFloats());
        assertArrayEquals(new double[]{1.5, -2}, roundTripped(simple(new double[]{1.5, -2})).getDoubles());
        assertArrayEquals(new boolean[]{true, false}, roundTripped(simple(new boolean[]{true, false})).getBooleans());
        assertArrayEquals(new String[]{"a", "", "bc"}, roundTripped(simple(new String[]{"a", "", "bc"})).getStrings());
    }

    @Test
    void emptyArraysRoundTrip() throws CodecException {
        SimpleParameterValue decoded = roundTripped(simple(new double[0]));
        assertEquals(ValueType.DOUBLE_ARRAY, decoded.getValueType());
        assertEquals(0, decoded.getDoubles().length);
    }

    @Test
    void arrays2DRoundTrip() throws CodecException {
        SimpleParameterValue doubles = roundTripped(SimpleParameterValueFactory.newSimpleParameterValue(new double[]{1, 2, 3, 4, 5, 6}, new int[]{3, 2}));
        assertEquals(ValueType.DOUBLE_ARRAY_2D, doubles.getValueType());
        assertEquals(3, doubles.getRowCount());
        assertEquals(2, doubles.getColumnCount());
        assertArrayEquals(new double[]{1, 2, 3, 4, 5, 6}, (double[]) doubles.getArray2D().getArray1D());

        SimpleParameterValue strings = roundTripped(SimpleParameterValueFactory.newSimpleParameterValue(new String[]{"a", "b", "c", "d"}, new int[]{2, 2}));
        assertEquals(ValueType.STRING_ARRAY_2D, strings.getValueType());
        assertEquals("c", strings.getArray2D().getString(1, 0));

        SimpleParameterValue booleans = roundTripped(SimpleParameterValueFactory.newSimpleParameterValue(new boolean[]{true, false}, new int[]{1, 2}));
        assertEquals(ValueType.BOOLEAN_ARRAY_2D, booleans.getValueType());
    }

    @Test
    void dataIsBigEndianWithDimensionsFirst() throws CodecException {
        assertArrayEquals(hex("00000102"), LVDataEncoder.encode(simple(258), LVTypeEncoder.describe("v", simple(1))));
        assertArrayEquals(hex("00000002 00000001 00000002"), LVDataEncoder.encode(simple(new int[]{1, 2}), LVTypeEncoder.describe("v", simple(new int[0]))));
        assertArrayEquals(hex("00000002 00000001 41 00000000"), LVDataEncoder.encode(simple(new String[]{"A", ""}), LVTypeEncoder.describe("v", simple(new String[0]))));
    }

    @Test
    void arrayWithAnotherNumberOfDimensionsThanTheDescriptorFails() {
        LVArrayDescriptor oneDim = new LVArrayDescriptor("v", 1, new LVTypeDescriptor(LVType.INT_32, ""));
        LVArrayDescriptor twoDim = new LVArrayDescriptor("v", 2, new LVTypeDescriptor(LVType.INT_32, ""));
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(SimpleParameterValueFactory.newSimpleParameterValue(new int[]{1, 2}, new int[]{1, 2}), oneDim));
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(simple(new int[]{1, 2}), twoDim));
    }

    @Test
    void scalarAndArrayDescriptorsDoNotMatchEachOther() {
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(simple(1), new LVArrayDescriptor("v", 1, new LVTypeDescriptor(LVType.INT_32, ""))));
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(simple(new int[]{1}), new LVTypeDescriptor(LVType.INT_32, "v")));
    }

    @Test
    void truncatedDataFails() {
        assertThrows(CodecException.class, () -> decodeData(hex("00000002 00000001"), new LVArrayDescriptor("v", 1, new LVTypeDescriptor(LVType.INT_32, "")), null));
        assertThrows(CodecException.class, () -> decodeData(hex("0000"), new LVTypeDescriptor(LVType.INT_32, "v"), null));
        assertThrows(CodecException.class, () -> decodeData(hex("00000009 6162"), new LVTypeDescriptor(LVType.STRING, "v"), null));
    }

    @Test
    void typesWithoutDataSupportFail() {
        assertThrows(CodecException.class, () -> LVDataDecoder.decode(ByteBuffer.wrap(new byte[16]), new LVTypeDescriptor(LVType.EXT, "v")));
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(simple(1), new LVTypeDescriptor(LVType.DBL_CPX, "v")));
    }
}
