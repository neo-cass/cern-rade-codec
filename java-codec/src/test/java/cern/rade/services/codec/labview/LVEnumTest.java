package cern.rade.services.codec.labview;

import cern.japc.core.factory.SimpleParameterValueFactory;
import cern.japc.value.EnumItem;
import cern.japc.value.SimpleParameterValue;
import cern.japc.value.ValueDescriptor;
import cern.japc.value.ValueType;
import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.labview.LVArrayDescriptor;
import cern.rade.services.common.containers.labview.LVEnumDescriptor;
import cern.rade.services.common.containers.labview.LVType;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static cern.rade.services.codec.labview.LVTestSupport.decodeData;
import static cern.rade.services.codec.labview.LVTestSupport.decodeTypeDescriptor;
import static cern.rade.services.codec.labview.LVTestSupport.roundTrip;
import static cern.rade.services.codec.labview.LVTestSupport.simple;
import static org.junit.jupiter.api.Assertions.*;

class LVEnumTest {
    private static final TestEnumType MODE = TestEnumType.mode();

    private static LVEnumDescriptor describedEnum() throws CodecException {
        return assertInstanceOf(LVEnumDescriptor.class, LVTypeEncoder.describe("mode", simple(MODE.valueOf(5))));
    }

    @Test
    void enumDescriptorUsesSymbolsOrderedByCode() throws CodecException {
        LVEnumDescriptor desc = describedEnum();
        assertEquals(LVType.ENUM_16, desc.getType());
        assertArrayEquals(new String[]{"OFF", "ON", "STANDBY"}, desc.getItems());
    }

    @Test
    void enumTypeDescriptorBytes() throws CodecException {
        // unlabelled U16 enum {a, bc}: 2+1+1 header, 2 count, 2+3 items = 11, padded to 12
        byte[] bytes = LVTypeEncoder.encodeTypeDescriptorBytes(new LVEnumDescriptor("", LVType.ENUM_16, new String[]{"a", "bc"}));
        assertArrayEquals(new byte[]{0x00, 0x0C, 0x00, 0x16, 0x00, 0x02, 0x01, 'a', 0x02, 'b', 'c', 0x00}, bytes);

        LVEnumDescriptor decoded = (LVEnumDescriptor) decodeTypeDescriptor(bytes);
        assertArrayEquals(new String[]{"a", "bc"}, decoded.getItems());
        assertEquals("", decoded.getName());
    }

    @Test
    void labelledEnumTypeDescriptorWithPaddingBeforeLabel() throws CodecException {
        // items {a, bc} end on an odd offset: pad byte, then the label
        LVEnumDescriptor desc = new LVEnumDescriptor("mode", LVType.ENUM_8, new String[]{"a", "bc"});
        LVEnumDescriptor decoded = (LVEnumDescriptor) decodeTypeDescriptor(LVTypeEncoder.encodeTypeDescriptorBytes(desc));
        assertEquals("mode", decoded.getName());
        assertEquals(LVType.ENUM_8, decoded.getType());
        assertArrayEquals(new String[]{"a", "bc"}, decoded.getItems());
    }

    @Test
    void enumValueIsLabVIEWPosition() throws CodecException {
        assertArrayEquals(new byte[]{0x00, 0x02}, LVDataEncoder.encode(simple(MODE.valueOf(10)), describedEnum()));
    }

    @Test
    void enumRoundTripResolvesJapcCode() throws CodecException {
        SimpleParameterValue decoded = roundTrip("mode", simple(MODE.valueOf(10)), TestDescriptors.simple(MODE)).castAsSimple();
        assertEquals(ValueType.ENUM, decoded.getValueType());
        assertEquals(10, decoded.getEnumItem().getCode());
    }

    @Test
    void singleFieldEnumIsDecodedWithItsSimpleDescriptor() throws CodecException {
        // 16 bit enum {OFF, ON, STANDBY}, value 1
        SimpleParameterValue decoded = decodeData(new byte[]{0, 1}, describedEnum(), TestDescriptors.simple(MODE)).castAsSimple();
        assertEquals("ON", decoded.getEnumItem().getSymbol());
        assertEquals(5, decoded.getEnumItem().getCode());
    }

    @Test
    void enumWithoutJapcEnumTypeFailsNamingTheField() throws CodecException {
        LVEnumDescriptor desc = describedEnum();
        byte[] data = {0, 1};
        // no descriptor, a simple descriptor without enum type, and a descriptor of a property
        for (ValueDescriptor descriptor : new ValueDescriptor[]{null, TestDescriptors.simple(null), TestDescriptors.property("mode", TestDescriptors.simple(MODE))}) {
            CodecException e = assertThrows(CodecException.class, () -> LVDataDecoder.decode(ByteBuffer.wrap(data), desc, descriptor));
            assertTrue(e.getMessage().contains("JAPC descriptor with enum type required for field mode"), e.getMessage());
        }
        assertThrows(CodecException.class, () -> LVDataDecoder.decode(ByteBuffer.wrap(data), desc));
    }

    @Test
    void enumItemMissingFromJapcFails() {
        LVEnumDescriptor desc = new LVEnumDescriptor("mode", LVType.ENUM_16, new String[]{"OFF", "UNKNOWN"});
        assertThrows(CodecException.class, () -> decodeData(new byte[]{0, 1}, desc, TestDescriptors.simple(MODE)));
    }

    @Test
    void enumValueOutOfRangeFails() throws CodecException {
        assertThrows(CodecException.class, () -> decodeData(new byte[]{0, 3}, describedEnum(), TestDescriptors.simple(MODE)));
    }

    @Test
    void enumItemMissingFromLvEnumFailsOnEncode() {
        LVEnumDescriptor desc = new LVEnumDescriptor("mode", LVType.ENUM_16, new String[]{"OFF", "ON"});
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(simple(MODE.valueOf(10)), desc));
    }

    @Test
    void enumArrayRoundTrip() throws CodecException {
        EnumItem[] items = {MODE.valueOf(5), MODE.valueOf(1), MODE.valueOf(10)};
        SimpleParameterValue decoded = roundTrip("modes", simple(items), TestDescriptors.simple(MODE)).castAsSimple();
        assertEquals(ValueType.ENUM_ARRAY, decoded.getValueType());
        assertArrayEquals(items, decoded.getEnumItems());
    }

    @Test
    void enumArray2DRoundTrip() throws CodecException {
        EnumItem[] items = {MODE.valueOf(5), MODE.valueOf(1), MODE.valueOf(10), MODE.valueOf(10), MODE.valueOf(5), MODE.valueOf(1)};
        SimpleParameterValue value = SimpleParameterValueFactory.newSimpleParameterValue(items, new int[]{2, 3});

        LVArrayDescriptor desc = assertInstanceOf(LVArrayDescriptor.class, LVTypeEncoder.describe("grid", value));
        assertEquals(2, desc.getDimension());
        assertEquals(LVType.ENUM_16, desc.getElemDescriptor().getType());

        SimpleParameterValue decoded = roundTrip("grid", value, TestDescriptors.simple(MODE)).castAsSimple();
        assertEquals(ValueType.ENUM_ARRAY_2D, decoded.getValueType());
        assertEquals(2, decoded.getRowCount());
        assertEquals(3, decoded.getColumnCount());
        assertArrayEquals(items, (EnumItem[]) decoded.getArray2D().getArray1D());
        assertEquals(10, decoded.getArray2D().getEnumItem(1, 0).getCode());
    }

    @Test
    void enumArrayDataIsDimensionsThenPositions() throws CodecException {
        SimpleParameterValue value = simple(new EnumItem[]{MODE.valueOf(10), MODE.valueOf(1)});
        byte[] data = LVDataEncoder.encode(value, LVTypeEncoder.describe("modes", value));
        assertArrayEquals(new byte[]{0, 0, 0, 2, 0, 2, 0, 0}, data);
    }

    @Test
    void enumArrayWithoutJapcEnumTypeFailsNamingTheArray() throws CodecException {
        SimpleParameterValue value = simple(new EnumItem[]{MODE.valueOf(10)});
        LVTypeDescriptor desc = LVTypeEncoder.describe("modes", value);
        byte[] data = LVDataEncoder.encode(value, desc);
        CodecException e = assertThrows(CodecException.class, () -> decodeData(data, desc, null));
        assertTrue(e.getMessage().contains("JAPC descriptor with enum type required for field modes"), e.getMessage());
    }

    @Test
    void emptyEnumArrayCannotBeDescribed() {
        CodecException e = assertThrows(CodecException.class, () -> LVTypeEncoder.describe("modes", simple(new EnumItem[0])));
        assertTrue(e.getMessage().contains("modes"), e.getMessage());
    }
}
