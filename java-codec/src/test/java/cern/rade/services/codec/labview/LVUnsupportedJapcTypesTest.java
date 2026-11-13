package cern.rade.services.codec.labview;

import cern.japc.core.ObjectParameterValue;
import cern.japc.core.factory.MapParameterValueFactory;
import cern.japc.core.factory.ObjectParameterValueFactory;
import cern.japc.core.factory.SimpleParameterValueFactory;
import cern.japc.value.ParameterValue;
import cern.japc.value.SimpleParameterValue;
import cern.japc.value.Type;
import cern.japc.value.ValueType;
import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.TypeLookup;
import cern.rade.services.common.containers.labview.LVClusterDescriptor;
import cern.rade.services.common.containers.labview.LVType;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;
import org.junit.jupiter.api.Test;

import static cern.rade.services.codec.labview.LVTestSupport.simple;
import static org.junit.jupiter.api.Assertions.*;

/**
 * OBJECT parameter values and UNDEFINED values cannot be represented in LabVIEW: they are rejected with a clear message.
 */
class LVUnsupportedJapcTypesTest {
    private static final LVTypeDescriptor ANY_DESCRIPTOR = new LVTypeDescriptor(LVType.DBL, "v");

    private static ObjectParameterValue object() {
        return ObjectParameterValueFactory.newObjectParameterValue("anything");
    }

    /** A simple value which has not been set has the type UNDEFINED, JAPC does not even let get its type. */
    private static SimpleParameterValue undefined() {
        return SimpleParameterValueFactory.newSimpleParameterValue();
    }

    private static void assertNotRepresentable(ParameterValue value) {
        CodecException describe = assertThrows(CodecException.class, () -> LVTypeEncoder.describe("v", value));
        assertTrue(describe.getMessage().contains("not representable in LabVIEW"), describe.getMessage());
        CodecException encode = assertThrows(CodecException.class, () -> LVDataEncoder.encode(value, ANY_DESCRIPTOR));
        assertTrue(encode.getMessage().contains("not representable in LabVIEW"), encode.getMessage());
    }

    @Test
    void objectValueIsRejected() {
        assertEquals(Type.OBJECT, object().getType());
        assertNotRepresentable(object());
    }

    @Test
    void undefinedValueIsRejected() {
        assertNotRepresentable(undefined());
    }

    @Test
    void objectValueIsRejectedAgainstAClusterDescriptor() {
        CodecException e = assertThrows(CodecException.class, () -> LVDataEncoder.encode(object(),
                new LVClusterDescriptor("c", new LVTypeDescriptor[0])));
        assertTrue(e.getMessage().contains("not representable in LabVIEW"), e.getMessage());
    }

    @Test
    void mapAndSimpleValuesDoNotMatchTheOtherKindOfDescriptor() {
        ParameterValue map = MapParameterValueFactory.newMapParameterValue();
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(map, ANY_DESCRIPTOR));
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(simple(1.0),
                new LVClusterDescriptor("c", new LVTypeDescriptor[0])));
    }

    @Test
    void japcTypesWithoutLvTypeAreLookedUpWithoutExceptions() {
        assertTrue(TypeLookup.findLVType(ValueType.UNDEFINED).isEmpty());
        assertTrue(TypeLookup.findLVTypeCode(ValueType.UNDEFINED).isEmpty());
        assertEquals(LVType.INT_32, TypeLookup.findLVType(ValueType.INT).orElseThrow());
        assertEquals(LVType.ENUM_SET, TypeLookup.findLVType(ValueType.ENUM_SET).orElseThrow());
        assertEquals((byte) 0x61, TypeLookup.findLVTypeCode(ValueType.ENUM_SET).orElseThrow().byteValue());
        assertThrows(IllegalArgumentException.class, () -> TypeLookup.getLVTypeCode(ValueType.UNDEFINED));
    }
}
