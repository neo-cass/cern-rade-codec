package cern.rade.services.codec.labview;

import cern.japc.core.factory.SimpleParameterValueFactory;
import cern.japc.value.EnumItem;
import cern.japc.value.EnumItemSet;
import cern.japc.value.SimpleParameterValue;
import cern.japc.value.ValueType;
import cern.japc.value.factory.DomainValueFactory;
import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.TypeLookup;
import cern.rade.services.common.containers.labview.LVArrayDescriptor;
import cern.rade.services.common.containers.labview.LVClusterDescriptor;
import cern.rade.services.common.containers.labview.LVEnumSetDescriptor;
import cern.rade.services.common.containers.labview.LVType;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;
import org.junit.jupiter.api.Test;

import static cern.rade.services.codec.labview.LVTestSupport.decodeData;
import static cern.rade.services.codec.labview.LVTestSupport.decodeTypeDescriptor;
import static cern.rade.services.codec.labview.LVTestSupport.hex;
import static cern.rade.services.codec.labview.LVTestSupport.roundTrip;
import static cern.rade.services.codec.labview.LVTestSupport.simple;
import static org.junit.jupiter.api.Assertions.*;

class LVEnumSetTest {
    private static final TestEnumType FLAGS = TestEnumType.flags();

    private static EnumItemSet set(String... symbols) {
        EnumItem[] items = new EnumItem[symbols.length];
        for (int i = 0; i < items.length; i++) {
            items[i] = FLAGS.valueOf(symbols[i]);
        }
        return DomainValueFactory.newEnumItemSet(FLAGS, items);
    }

    private static byte[] clusterBytes(LVTypeDescriptor... elements) throws CodecException {
        return LVTypeEncoder.encodeTypeDescriptorBytes(new LVClusterDescriptor("", elements));
    }

    private static byte[] markedAsEnumSet(byte[] clusterBytes) {
        assertEquals(TypeLookup.getLVClusterTypeCode(), clusterBytes[3]);
        clusterBytes[3] = TypeLookup.getLVEnumSetTypeCode();
        return clusterBytes;
    }

    @Test
    void enumSetDescriptorHasAllItemsOrderedByCode() throws CodecException {
        LVEnumSetDescriptor desc = assertInstanceOf(LVEnumSetDescriptor.class, LVTypeEncoder.describe("flags", simple(set("READY"))));
        assertEquals(LVType.ENUM_SET, desc.getType());
        assertEquals("flags", desc.getName());
        // BUSY = 1, FAULT = 2, READY = 4
        assertArrayEquals(new String[]{"BUSY", "FAULT", "READY"}, desc.getItems());
    }

    @Test
    void unnamedEnumSetTypeDescriptorBytesUseMarkerCode() throws CodecException {
        // size 0x12, flags 0, code 0x61, 2 elements; each element: size 6, flags 0x40 (labelled), code 0x21 (BOOL), label
        byte[] expected = hex("0012 0061 0002  0006 4021 0161  0006 4021 0162");
        assertArrayEquals(expected, LVTypeEncoder.encodeTypeDescriptorBytes(new LVEnumSetDescriptor("", new String[]{"a", "b"})));

        LVEnumSetDescriptor decoded = assertInstanceOf(LVEnumSetDescriptor.class, decodeTypeDescriptor(expected));
        assertEquals("", decoded.getName());
        assertArrayEquals(new String[]{"a", "b"}, decoded.getItems());
    }

    @Test
    void enumSetTypeDescriptorIsTheClusterOfBooleanWithAnotherTypeCode() throws CodecException {
        byte[] cluster = clusterBytes(new LVTypeDescriptor(LVType.BOOL, "a"), new LVTypeDescriptor(LVType.BOOL, "b"));
        byte[] enumSet = LVTypeEncoder.encodeTypeDescriptorBytes(new LVEnumSetDescriptor("", new String[]{"a", "b"}));
        assertArrayEquals(markedAsEnumSet(cluster), enumSet);
    }

    @Test
    void labelledEnumSetTypeDescriptorRoundTrip() throws CodecException {
        LVEnumSetDescriptor desc = new LVEnumSetDescriptor("flags", new String[]{"READY", "BUSY"});
        LVEnumSetDescriptor decoded = assertInstanceOf(LVEnumSetDescriptor.class, decodeTypeDescriptor(LVTypeEncoder.encodeTypeDescriptorBytes(desc)));
        assertEquals("flags", decoded.getName());
        assertArrayEquals(new String[]{"READY", "BUSY"}, decoded.getItems());
    }

    @Test
    void enumSetTypeDescriptorWithAnElementWhichIsNotABooleanFails() throws CodecException {
        byte[] notBool = markedAsEnumSet(clusterBytes(new LVTypeDescriptor(LVType.BOOL, "a"), new LVTypeDescriptor(LVType.DBL, "b")));
        assertThrows(CodecException.class, () -> decodeTypeDescriptor(notBool));
    }

    @Test
    void enumSetTypeDescriptorWithoutLabelDuplicatedLabelOrItemFails() throws CodecException {
        byte[] unlabelled = markedAsEnumSet(clusterBytes(new LVTypeDescriptor(LVType.BOOL, "a"), new LVTypeDescriptor(LVType.BOOL, "")));
        byte[] duplicated = markedAsEnumSet(clusterBytes(new LVTypeDescriptor(LVType.BOOL, "a"), new LVTypeDescriptor(LVType.BOOL, "a")));
        byte[] empty = markedAsEnumSet(clusterBytes());
        assertThrows(CodecException.class, () -> decodeTypeDescriptor(unlabelled));
        assertThrows(CodecException.class, () -> decodeTypeDescriptor(duplicated));
        assertThrows(CodecException.class, () -> decodeTypeDescriptor(empty));
    }

    @Test
    void enumSetDataIsOneByteForEachItem() throws CodecException {
        LVTypeDescriptor desc = LVTypeEncoder.describe("flags", simple(set("READY")));
        assertArrayEquals(new byte[]{1, 0, 1}, LVDataEncoder.encode(simple(set("READY", "BUSY")), desc));
        assertArrayEquals(new byte[]{0, 0, 0}, LVDataEncoder.encode(simple(set()), desc));
    }

    @Test
    void enumSetRoundTrip() throws CodecException {
        SimpleParameterValue decoded = roundTrip("flags", simple(set("READY", "FAULT")), TestDescriptors.simple(FLAGS)).castAsSimple();
        assertEquals(ValueType.ENUM_SET, decoded.getValueType());
        assertEquals(set("FAULT", "READY"), decoded.getEnumItemSet());
        assertEquals(6, decoded.getEnumItemSet().asLong());
    }

    @Test
    void emptyEnumSetRoundTrip() throws CodecException {
        SimpleParameterValue decoded = roundTrip("flags", simple(set()), TestDescriptors.simple(FLAGS)).castAsSimple();
        assertEquals(ValueType.ENUM_SET, decoded.getValueType());
        assertTrue(decoded.getEnumItemSet().isEmpty());
    }

    @Test
    void enumSetOfSubsetOfTheItemsIsDecodedByLabel() throws CodecException {
        // LabVIEW only knows READY and BUSY (in that order), the other items are not in the set
        LVEnumSetDescriptor desc = new LVEnumSetDescriptor("flags", new String[]{"READY", "BUSY"});
        EnumItemSet decoded = decodeData(new byte[]{1, 0}, desc, TestDescriptors.simple(FLAGS)).castAsSimple().getEnumItemSet();
        assertEquals(set("READY"), decoded);
        assertEquals(FLAGS, decoded.getEnumType());
        assertEquals(set("READY", "BUSY"), decodeData(new byte[]{1, 1}, desc, TestDescriptors.simple(FLAGS)).castAsSimple().getEnumItemSet());
    }

    @Test
    void enumSetWithALabelWhichIsNotAnItemFailsToDecode() {
        LVEnumSetDescriptor desc = new LVEnumSetDescriptor("flags", new String[]{"READY", "BOGUS"});
        // even if the unknown item is not in the set
        CodecException e = assertThrows(CodecException.class, () -> decodeData(new byte[]{1, 0}, desc, TestDescriptors.simple(FLAGS)));
        assertTrue(e.getMessage().contains("BOGUS"), e.getMessage());
    }

    @Test
    void enumSetWithoutJapcEnumTypeFailsNamingTheField() {
        LVEnumSetDescriptor desc = new LVEnumSetDescriptor("flags", new String[]{"READY"});
        CodecException e = assertThrows(CodecException.class, () -> decodeData(new byte[]{1}, desc, null));
        assertTrue(e.getMessage().contains("JAPC descriptor with enum type required for field flags"), e.getMessage());
    }

    @Test
    void enumSetContainingAnItemWhichIsNotInTheDescriptorFailsToEncode() {
        LVEnumSetDescriptor desc = new LVEnumSetDescriptor("flags", new String[]{"READY", "BUSY"});
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(simple(set("FAULT")), desc));
        // a subset of the items is fine
        assertDoesNotThrow(() -> LVDataEncoder.encode(simple(set("BUSY")), desc));
    }

    @Test
    void enumSetArrayTypeDescriptorIsAnArrayOfEnumSets() throws CodecException {
        SimpleParameterValue value = simple(new EnumItemSet[]{set("READY"), set("BUSY", "FAULT")});
        LVArrayDescriptor array = assertInstanceOf(LVArrayDescriptor.class, LVTypeEncoder.describe("flags", value));
        assertEquals(1, array.getDimension());
        assertEquals(LVType.ENUM_SET, array.getElemDescriptor().getType());
        byte[] td = LVTypeEncoder.encodeTypeDescriptorBytes(array);
        assertEquals(TypeLookup.getLVArrayTypeCode(), td[3]);
        assertEquals(TypeLookup.getLVEnumSetTypeCode(), td[4 + 2 + 4 + 3]);
        LVArrayDescriptor decoded = assertInstanceOf(LVArrayDescriptor.class, decodeTypeDescriptor(td));
        assertArrayEquals(new String[]{"BUSY", "FAULT", "READY"}, ((LVEnumSetDescriptor) decoded.getElemDescriptor()).getItems());
    }

    @Test
    void enumSetArrayRoundTrip() throws CodecException {
        EnumItemSet[] sets = {set("READY"), set(), set("BUSY", "FAULT", "READY")};
        SimpleParameterValue decoded = roundTrip("flags", simple(sets), TestDescriptors.simple(FLAGS)).castAsSimple();
        assertEquals(ValueType.ENUM_SET_ARRAY, decoded.getValueType());
        assertArrayEquals(sets, decoded.getEnumItemSets());
    }

    @Test
    void enumSetArrayDataIsDimensionsThenTheSets() throws CodecException {
        SimpleParameterValue value = simple(new EnumItemSet[]{set("READY"), set("BUSY")});
        byte[] data = LVDataEncoder.encode(value, LVTypeEncoder.describe("flags", value));
        assertArrayEquals(hex("00000002 000001 010000"), data);
    }

    @Test
    void enumSetArray2DRoundTrip() throws CodecException {
        EnumItemSet[] sets = {set("READY"), set(), set("BUSY"), set("FAULT", "READY"), set("BUSY", "FAULT"), set("BUSY")};
        SimpleParameterValue value = SimpleParameterValueFactory.newSimpleParameterValue(sets, new int[]{3, 2});
        LVArrayDescriptor desc = assertInstanceOf(LVArrayDescriptor.class, LVTypeEncoder.describe("grid", value));
        assertEquals(2, desc.getDimension());

        SimpleParameterValue decoded = roundTrip("grid", value, TestDescriptors.simple(FLAGS)).castAsSimple();
        assertEquals(ValueType.ENUM_SET_ARRAY_2D, decoded.getValueType());
        assertEquals(3, decoded.getRowCount());
        assertEquals(2, decoded.getColumnCount());
        assertArrayEquals(sets, (EnumItemSet[]) decoded.getArray2D().getArray1D());
    }

    @Test
    void emptyEnumSetArrayCannotBeDescribed() {
        CodecException e = assertThrows(CodecException.class, () -> LVTypeEncoder.describe("flags", simple(new EnumItemSet[0])));
        assertTrue(e.getMessage().contains("flags"), e.getMessage());
    }
}
