package cern.rade.services.codec.labview;

import cern.japc.core.factory.SimpleParameterValueFactory;
import cern.japc.value.MapParameterValue;
import cern.japc.value.SimpleParameterValue;
import cern.japc.value.ValueType;
import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.labview.LVArrayDescriptor;
import cern.rade.services.common.containers.labview.LVClusterDescriptor;
import cern.rade.services.common.containers.labview.LVType;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static cern.rade.services.codec.labview.LVTestSupport.decodeData;
import static cern.rade.services.codec.labview.LVTestSupport.decodeTypeDescriptor;
import static cern.rade.services.codec.labview.LVTestSupport.hex;
import static cern.rade.services.codec.labview.LVTestSupport.roundTrip;
import static cern.rade.services.codec.labview.LVTestSupport.simple;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JAPC has no unsigned types: they are decoded as the next larger signed type, and encoded from any JAPC number in range.
 */
class LVNumberTest {
    private static LVTypeDescriptor lv(LVType type) {
        return new LVTypeDescriptor(type, "n");
    }

    private static LVArrayDescriptor lvArray(LVType type, int dimension) {
        return new LVArrayDescriptor("arr", dimension, new LVTypeDescriptor(type, ""));
    }

    private static SimpleParameterValue decode(String data, LVTypeDescriptor desc) throws CodecException {
        return decodeData(hex(data), desc, null).castAsSimple();
    }

    // ---- decoding

    @Test
    void unsignedScalarsAreDecodedAsTheNextLargerSignedType() throws CodecException {
        SimpleParameterValue u8 = decode("FF", lv(LVType.UINT_8));
        assertEquals(ValueType.SHORT, u8.getValueType());
        assertEquals(255, u8.getShort());

        SimpleParameterValue u16 = decode("FFFF", lv(LVType.UINT_16));
        assertEquals(ValueType.INT, u16.getValueType());
        assertEquals(65535, u16.getInt());

        SimpleParameterValue u32 = decode("FFFFFFFF", lv(LVType.UINT_32));
        assertEquals(ValueType.LONG, u32.getValueType());
        assertEquals(4294967295L, u32.getLong());

        SimpleParameterValue u64 = decode("7FFFFFFFFFFFFFFF", lv(LVType.UINT_64));
        assertEquals(ValueType.LONG, u64.getValueType());
        assertEquals(Long.MAX_VALUE, u64.getLong());
    }

    @Test
    void smallUnsignedValuesAreUnchanged() throws CodecException {
        assertEquals(7, decode("07", lv(LVType.UINT_8)).getShort());
        assertEquals(258, decode("0102", lv(LVType.UINT_16)).getInt());
        assertEquals(0, decode("0000000000000000", lv(LVType.UINT_64)).getLong());
    }

    @Test
    void unsigned64AboveLongMaxValueFails() {
        assertThrows(CodecException.class, () -> decode("8000000000000000", lv(LVType.UINT_64)));
        assertThrows(CodecException.class, () -> decode("FFFFFFFFFFFFFFFF", lv(LVType.UINT_64)));
        assertThrows(CodecException.class, () -> decode("00000001 8000000000000000", lvArray(LVType.UINT_64, 1)));
    }

    @Test
    void unsignedArraysAreDecoded() throws CodecException {
        SimpleParameterValue u8 = decode("00000003 01 80 FF", lvArray(LVType.UINT_8, 1));
        assertEquals(ValueType.SHORT_ARRAY, u8.getValueType());
        assertArrayEquals(new short[]{1, 128, 255}, u8.getShorts());

        SimpleParameterValue u16 = decode("00000002 0001 FFFF", lvArray(LVType.UINT_16, 1));
        assertEquals(ValueType.INT_ARRAY, u16.getValueType());
        assertArrayEquals(new int[]{1, 65535}, u16.getInts());

        SimpleParameterValue u32 = decode("00000001 FFFFFFFF", lvArray(LVType.UINT_32, 1));
        assertEquals(ValueType.LONG_ARRAY, u32.getValueType());
        assertArrayEquals(new long[]{4294967295L}, u32.getLongs());

        SimpleParameterValue u64 = decode("00000001 7FFFFFFFFFFFFFFF", lvArray(LVType.UINT_64, 1));
        assertEquals(ValueType.LONG_ARRAY, u64.getValueType());
        assertArrayEquals(new long[]{Long.MAX_VALUE}, u64.getLongs());
    }

    @Test
    void unsigned2DArraysAreDecoded() throws CodecException {
        SimpleParameterValue u8 = decode("00000002 00000002  01 FF 02 FE", lvArray(LVType.UINT_8, 2));
        assertEquals(ValueType.SHORT_ARRAY_2D, u8.getValueType());
        assertEquals(2, u8.getRowCount());
        assertEquals(2, u8.getColumnCount());
        assertArrayEquals(new short[]{1, 255, 2, 254}, (short[]) u8.getArray2D().getArray1D());

        SimpleParameterValue u16 = decode("00000001 00000002  8000 FFFF", lvArray(LVType.UINT_16, 2));
        assertEquals(ValueType.INT_ARRAY_2D, u16.getValueType());
        assertArrayEquals(new int[]{32768, 65535}, (int[]) u16.getArray2D().getArray1D());
    }

    @Test
    void unsignedFieldsOfAClusterAreDecoded() throws CodecException {
        LVTypeDescriptor cluster = new LVClusterDescriptor("Setting", new LVTypeDescriptor[]{
                new LVTypeDescriptor(LVType.UINT_8, "a"), new LVTypeDescriptor(LVType.UINT_16, "b"),
                new LVTypeDescriptor(LVType.UINT_32, "c"), new LVTypeDescriptor(LVType.UINT_64, "d")});
        MapParameterValue decoded = decodeData(hex("FF FFFF FFFFFFFF 0000000000000010"), cluster, null).castAsMap();
        assertEquals(255, decoded.get("a").getShort());
        assertEquals(65535, decoded.get("b").getInt());
        assertEquals(4294967295L, decoded.get("c").getLong());
        assertEquals(16, decoded.get("d").getLong());
    }

    @Test
    void unsigned64TypeDescriptorIsDecoded() throws CodecException {
        for (LVType type : new LVType[]{LVType.UINT_8, LVType.UINT_16, LVType.UINT_32, LVType.UINT_64}) {
            byte[] bytes = LVTypeEncoder.encodeTypeDescriptorBytes(new LVTypeDescriptor(type, "n"));
            LVTypeDescriptor decoded = decodeTypeDescriptor(bytes);
            assertEquals(type, decoded.getType());
            assertEquals("n", decoded.getName());
        }
        // U8 = 0x05 ... U64 = 0x08 (NI "Type descriptors")
        assertArrayEquals(hex("0004 0008"), LVTypeEncoder.encodeTypeDescriptorBytes(new LVTypeDescriptor(LVType.UINT_64, "")));
    }

    @Test
    void arrayDimensionsWhichDoNotFitTheDataFail() {
        assertThrows(CodecException.class, () -> decode("7FFFFFFF", lvArray(LVType.UINT_8, 1)));
        assertThrows(CodecException.class, () -> decode("FFFFFFFF", lvArray(LVType.UINT_8, 1)));
    }

    // ---- encoding

    @Test
    void descriptionOfJapcNumbersIsSigned() throws CodecException {
        assertEquals(LVType.INT_8, LVTypeEncoder.describe("n", simple((byte) 1)).getType());
        assertEquals(LVType.INT_16, LVTypeEncoder.describe("n", simple((short) 1)).getType());
        assertEquals(LVType.INT_32, LVTypeEncoder.describe("n", simple(1)).getType());
        assertEquals(LVType.INT_64, LVTypeEncoder.describe("n", simple(1L)).getType());
        assertEquals(LVType.SGL, LVTypeEncoder.describe("n", simple(1f)).getType());
        assertEquals(LVType.DBL, LVTypeEncoder.describe("n", simple(1d)).getType());
    }

    @Test
    void japcNumbersAreEncodedAsTheUnsignedTypeOfTheDescriptor() throws CodecException {
        assertArrayEquals(hex("C8"), LVDataEncoder.encode(simple((short) 200), lv(LVType.UINT_8)));
        assertArrayEquals(hex("FF"), LVDataEncoder.encode(simple(255), lv(LVType.UINT_8)));
        assertArrayEquals(hex("FFFF"), LVDataEncoder.encode(simple(65535), lv(LVType.UINT_16)));
        assertArrayEquals(hex("FFFFFFFF"), LVDataEncoder.encode(simple(4294967295L), lv(LVType.UINT_32)));
        assertArrayEquals(hex("7FFFFFFFFFFFFFFF"), LVDataEncoder.encode(simple(Long.MAX_VALUE), lv(LVType.UINT_64)));
        assertArrayEquals(hex("00"), LVDataEncoder.encode(simple((byte) 0), lv(LVType.UINT_8)));
    }

    @Test
    void valuesOutOfTheRangeOfTheUnsignedTypeFail() {
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(simple((short) 256), lv(LVType.UINT_8)));
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(simple(-1), lv(LVType.UINT_8)));
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(simple((byte) -1), lv(LVType.UINT_16)));
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(simple(65536), lv(LVType.UINT_16)));
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(simple(4294967296L), lv(LVType.UINT_32)));
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(simple(-1L), lv(LVType.UINT_32)));
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(simple(-1L), lv(LVType.UINT_64)));
    }

    @Test
    void unsignedArraysAreEncodedWithTheRangeCheck() throws CodecException {
        assertArrayEquals(hex("00000002 0001 FFFF"), LVDataEncoder.encode(simple(new int[]{1, 65535}), lvArray(LVType.UINT_16, 1)));
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(simple(new int[]{1, 65536}), lvArray(LVType.UINT_16, 1)));
        SimpleParameterValue matrix = SimpleParameterValueFactory.newSimpleParameterValue(new short[]{1, 255, 2, 254}, new int[]{2, 2});
        assertArrayEquals(hex("00000002 00000002 01 FF 02 FE"), LVDataEncoder.encode(matrix, lvArray(LVType.UINT_8, 2)));
    }

    @Test
    void unsignedRoundTripThroughTheDescriptor() throws CodecException {
        SimpleParameterValue decoded = roundTrip(lv(LVType.UINT_32), simple(3000000000L), null).castAsSimple();
        assertEquals(3000000000L, decoded.getLong());
    }

    @Test
    void numbersAreConvertedToAnotherSignedTypeWithTheRangeCheck() throws CodecException {
        assertArrayEquals(hex("0005"), LVDataEncoder.encode(simple(5), lv(LVType.INT_16)));
        assertArrayEquals(hex("0000000000000005"), LVDataEncoder.encode(simple((byte) 5), lv(LVType.INT_64)));
        assertArrayEquals(hex("80"), LVDataEncoder.encode(simple(-128), lv(LVType.INT_8)));
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(simple(128), lv(LVType.INT_8)));
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(simple(40000), lv(LVType.INT_16)));
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(simple(Long.MAX_VALUE), lv(LVType.INT_32)));
    }

    @Test
    void integersAreConvertedToFloatingPointButNotTheOtherWayRound() throws CodecException {
        assertArrayEquals(ByteBuffer.allocate(8).putDouble(5).array(), LVDataEncoder.encode(simple(5), lv(LVType.DBL)));
        assertArrayEquals(ByteBuffer.allocate(4).putFloat(2.5f).array(), LVDataEncoder.encode(simple(2.5d), lv(LVType.SGL)));
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(simple(1e300), lv(LVType.SGL)));
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(simple(2.0d), lv(LVType.INT_32)));
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(simple(2.0f), lv(LVType.UINT_8)));
    }

    @Test
    void valueWhichIsNotANumberIsNotConverted() {
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(simple("1"), lv(LVType.INT_32)));
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(simple(true), lv(LVType.UINT_8)));
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(simple(1), lv(LVType.STRING)));
    }
}
