package cern.rade.services.codec.labview;

import cern.japc.core.factory.MapParameterValueFactory;
import cern.japc.core.factory.SimpleParameterValueFactory;
import cern.japc.value.EnumItem;
import cern.japc.value.EnumItemSet;
import cern.japc.value.MapParameterValue;
import cern.japc.value.ValueDescriptor;
import cern.japc.value.ValueType;
import cern.japc.value.factory.DomainValueFactory;
import cern.rade.services.codec.CodecException;
import cern.rade.services.common.BytesUtil;
import cern.rade.services.common.containers.labview.LVClusterDescriptor;
import cern.rade.services.common.containers.labview.LVType;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static cern.rade.services.codec.labview.LVTestSupport.decodeData;
import static cern.rade.services.codec.labview.LVTestSupport.decodeTypeDescriptor;
import static cern.rade.services.codec.labview.LVTestSupport.hex;
import static cern.rade.services.codec.labview.LVTestSupport.roundTrip;
import static cern.rade.services.codec.labview.LVTestSupport.simple;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A JAPC property is a LabVIEW cluster: its fields are decoded from the type descriptor, and the JAPC map descriptor
 * is only used for the enum and enum set fields.
 */
class LVPropertyTest {
    private static final TestEnumType MODE = TestEnumType.mode();
    private static final TestEnumType FLAGS = TestEnumType.flags();

    private static EnumItemSet flags(String... symbols) {
        return DomainValueFactory.newEnumItemSet(FLAGS, Arrays.stream(symbols).map(FLAGS::valueOf).toArray(EnumItem[]::new));
    }

    private static MapParameterValue property() {
        MapParameterValue map = MapParameterValueFactory.newMapParameterValue();
        map.put("mode", simple(MODE.valueOf(10)));
        map.put("flags", simple(flags("READY", "BUSY")));
        map.put("modes", simple(new EnumItem[]{MODE.valueOf(1), MODE.valueOf(5)}));
        map.put("flagsList", simple(new EnumItemSet[]{flags("FAULT"), flags()}));
        map.put("fn", simple(DomainValueFactory.newDiscreteFunction(new double[]{1, 2}, new double[]{3, 4})));
        map.put("fns", simple(DomainValueFactory.newDiscreteFunctionList(
                DomainValueFactory.newDiscreteFunction(new double[]{1}, new double[]{2}),
                DomainValueFactory.newDiscreteFunction(new double[]{}, new double[]{}))));
        map.put("u8", simple((short) 200));
        map.put("u32", simple(3000000000L));
        map.put("count", simple(42));
        map.put("name", simple("abc"));
        map.put("ok", simple(true));
        map.put("values", simple(new double[]{1.0, 2.0}));
        map.put("matrix", SimpleParameterValueFactory.newSimpleParameterValue(new int[]{1, 2, 3, 4, 5, 6}, new int[]{2, 3}));
        return map;
    }

    /** The descriptor of the property as LabVIEW would define it: the fields u8 and u32 are unsigned. */
    private static LVClusterDescriptor lvDescriptor(MapParameterValue map) throws CodecException {
        LVClusterDescriptor described = (LVClusterDescriptor) LVTypeEncoder.describe("Setting", map);
        LVTypeDescriptor[] fields = described.getFieldDescriptors().clone();
        for (int i = 0; i < fields.length; i++) {
            switch (fields[i].getName()) {
                case "u8" -> fields[i] = new LVTypeDescriptor(LVType.UINT_8, "u8");
                case "u32" -> fields[i] = new LVTypeDescriptor(LVType.UINT_32, "u32");
                default -> {
                }
            }
        }
        return new LVClusterDescriptor("Setting", fields);
    }

    /** The JAPC descriptor of the property: it has the enum types of the enum and enum set fields only. */
    private static ValueDescriptor japcDescriptor() {
        return TestDescriptors.property(
                "mode", TestDescriptors.simple(MODE),
                "flags", TestDescriptors.simple(FLAGS),
                "modes", TestDescriptors.simple(MODE),
                "flagsList", TestDescriptors.simple(FLAGS));
    }

    @Test
    void propertyWithAllTheFieldTypesRoundTrip() throws CodecException {
        MapParameterValue map = property();
        MapParameterValue decoded = roundTrip(lvDescriptor(map), map, japcDescriptor()).castAsMap();

        assertEquals(10, decoded.get("mode").getEnumItem().getCode());
        assertEquals(flags("BUSY", "READY"), decoded.get("flags").getEnumItemSet());
        assertArrayEquals(new EnumItem[]{MODE.valueOf(1), MODE.valueOf(5)}, decoded.get("modes").getEnumItems());
        assertArrayEquals(new EnumItemSet[]{flags("FAULT"), flags()}, decoded.get("flagsList").getEnumItemSets());
        assertArrayEquals(new double[]{3, 4}, decoded.get("fn").getDiscreteFunction().toYArray());
        assertEquals(2, decoded.get("fns").getDiscreteFunctionList().size());
        assertEquals(ValueType.SHORT, decoded.get("u8").getValueType());
        assertEquals(200, decoded.get("u8").getShort());
        assertEquals(ValueType.LONG, decoded.get("u32").getValueType());
        assertEquals(3000000000L, decoded.get("u32").getLong());
        assertEquals(42, decoded.get("count").getInt());
        assertEquals("abc", decoded.get("name").getString());
        assertTrue(decoded.get("ok").getBoolean());
        assertArrayEquals(new double[]{1.0, 2.0}, decoded.get("values").getDoubles());
        assertEquals(2, decoded.get("matrix").getRowCount());
        assertArrayEquals(new int[]{1, 2, 3, 4, 5, 6}, (int[]) decoded.get("matrix").getArray2D().getArray1D());
        assertEquals(map.size(), decoded.size());
    }

    @Test
    void propertyWithoutEnumsNeedsNoDescriptor() throws CodecException {
        MapParameterValue map = MapParameterValueFactory.newMapParameterValue();
        map.put("count", simple(42));
        map.put("fn", simple(DomainValueFactory.newDiscreteFunction(new double[]{1}, new double[]{2})));
        map.put("u8", simple((short) 3));
        MapParameterValue decoded = roundTrip("Setting", map, null).castAsMap();
        assertEquals(42, decoded.get("count").getInt());

        // and the descriptor is not looked at: the one of another property is fine
        assertEquals(42, roundTrip("Setting", map, japcDescriptor()).castAsMap().get("count").getInt());
    }

    @Test
    void enumFieldWithoutDescriptorFailsNamingTheField() throws CodecException {
        MapParameterValue map = property();
        LVClusterDescriptor desc = lvDescriptor(map);
        byte[] data = LVDataEncoder.encode(map, desc);

        CodecException noDescriptor = assertThrows(CodecException.class, () -> decodeData(data, desc, null));
        assertTrue(noDescriptor.getMessage().startsWith("JAPC descriptor with enum type required for field "), noDescriptor.getMessage());

        // no descriptor for the field "flags"
        ValueDescriptor missingField = TestDescriptors.property("mode", TestDescriptors.simple(MODE),
                "modes", TestDescriptors.simple(MODE), "flagsList", TestDescriptors.simple(FLAGS));
        CodecException noField = assertThrows(CodecException.class, () -> decodeData(data, desc, missingField));
        assertTrue(noField.getMessage().endsWith("for field flags"), noField.getMessage());

        // the descriptor of a field is not the one of a property
        CodecException notAProperty = assertThrows(CodecException.class, () -> decodeData(data, desc, TestDescriptors.simple(MODE)));
        assertTrue(notAProperty.getMessage().startsWith("JAPC descriptor with enum type required for field "), notAProperty.getMessage());
    }

    @Test
    void propertyTypeDescriptorIsAClusterOfTheFields() throws CodecException {
        MapParameterValue map = property();
        LVClusterDescriptor decoded = assertInstanceOf(LVClusterDescriptor.class,
                decodeTypeDescriptor(LVTypeEncoder.encodeTypeDescriptorBytes(lvDescriptor(map))));
        assertEquals("Setting", decoded.getName());
        LVType[] types = Arrays.stream(decoded.getFieldDescriptors()).map(LVTypeDescriptor::getType).toArray(LVType[]::new);
        assertEquals(map.size(), types.length);
        assertTrue(Arrays.asList(types).containsAll(Arrays.asList(LVType.ENUM_16, LVType.ENUM_SET, LVType.DISCRETE_FUNCTION, LVType.UINT_8, LVType.UINT_32)));
    }

    @Test
    void missingFieldFailsToEncode() throws CodecException {
        MapParameterValue map = property();
        LVClusterDescriptor desc = lvDescriptor(map);
        map.remove("count");
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(map, desc));
    }

    @Test
    void errorClusterIsUnchanged() throws CodecException {
        LVTypeDescriptor desc = decodeTypeDescriptor(BytesUtil.shortArrToByteArr(LVErrorEncoder.encodeTypeDescriptor()));
        assertEquals(LVType.CLUSTER, desc.getType());
        assertEquals("error out", desc.getName());
        // status (BOOL), code (I32), source (string)
        assertArrayEquals(hex("01 00000005 00000003 737263"), LVErrorEncoder.encode(true, 5, "src"));
    }
}
