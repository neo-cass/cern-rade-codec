package cern.rade.services.codec.labview;

import cern.japc.core.factory.MapParameterValueFactory;
import cern.japc.value.DiscreteFunction;
import cern.japc.value.DiscreteFunctionList;
import cern.japc.value.MapParameterValue;
import cern.japc.value.SimpleParameterValue;
import cern.japc.value.ValueType;
import cern.japc.value.factory.DomainValueFactory;
import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.TypeLookup;
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

class LVDiscreteFunctionTest {
    private static DiscreteFunction df(double[] x, double[] y) {
        return DomainValueFactory.newDiscreteFunction(x, y);
    }

    private static byte[] dfData(int rows, int columns, double... values) {
        ByteBuffer buffer = ByteBuffer.allocate(8 + values.length * 8).putInt(rows).putInt(columns);
        for (double v : values) {
            buffer.putDouble(v);
        }
        return buffer.array();
    }

    private static final LVTypeDescriptor DF_DESCRIPTOR = new LVTypeDescriptor(LVType.DISCRETE_FUNCTION, "fn");

    @Test
    void discreteFunctionTypeDescriptorIsPatched2dDoubleArray() throws CodecException {
        LVTypeDescriptor desc = LVTypeEncoder.describe("fn", simple(df(new double[]{0, 1, 2}, new double[]{10, 20, 30})));
        byte[] td = LVTypeEncoder.encodeTypeDescriptorBytes(desc);
        assertEquals(TypeLookup.getLVDiscreteFunctionTypeCode(), td[3]);

        // LabVIEW side: a 2D array of DBL whose type code byte has been patched
        byte[] array = LVTypeEncoder.encodeTypeDescriptorBytes(new LVArrayDescriptor("fn", 2, new LVTypeDescriptor(LVType.DBL, "")));
        assertEquals(TypeLookup.getLVArrayTypeCode(), array[3]);
        array[3] = TypeLookup.getLVDiscreteFunctionTypeCode();
        assertArrayEquals(array, td);

        LVTypeDescriptor decoded = decodeTypeDescriptor(td);
        assertEquals(LVType.DISCRETE_FUNCTION, decoded.getType());
        assertEquals("fn", decoded.getName());
    }

    @Test
    void discreteFunctionTypeDescriptorHex() throws CodecException {
        // size 0x12, flags 0, code 0x60, 2 dims, 2 x FFFFFFFF, element DBL (size 4, code 0x0A)
        byte[] expected = hex("0012 0060 0002 FFFFFFFF FFFFFFFF 0004 000A");
        assertArrayEquals(expected, LVTypeEncoder.encodeTypeDescriptorBytes(new LVTypeDescriptor(LVType.DISCRETE_FUNCTION, "")));
        LVTypeDescriptor decoded = decodeTypeDescriptor(expected);
        assertEquals(LVType.DISCRETE_FUNCTION, decoded.getType());
        assertEquals("", decoded.getName());
    }

    @Test
    void discreteFunctionTypeDescriptorWithWrongLayoutFails() throws CodecException {
        byte[] oneDim = LVTypeEncoder.encodeTypeDescriptorBytes(new LVArrayDescriptor("fn", 1, new LVTypeDescriptor(LVType.DBL, "")));
        oneDim[3] = TypeLookup.getLVDiscreteFunctionTypeCode();
        assertThrows(CodecException.class, () -> decodeTypeDescriptor(oneDim));

        byte[] notDouble = LVTypeEncoder.encodeTypeDescriptorBytes(new LVArrayDescriptor("fn", 2, new LVTypeDescriptor(LVType.SGL, "")));
        notDouble[3] = TypeLookup.getLVDiscreteFunctionTypeCode();
        assertThrows(CodecException.class, () -> decodeTypeDescriptor(notDouble));

        // the old layout, a cluster of two 1D DBL arrays with the custom code, is rejected too
        byte[] cluster = LVTypeEncoder.encodeTypeDescriptorBytes(new LVClusterDescriptor("fn", new LVTypeDescriptor[]{
                new LVArrayDescriptor("X", 1, new LVTypeDescriptor(LVType.DBL, "")),
                new LVArrayDescriptor("Y", 1, new LVTypeDescriptor(LVType.DBL, ""))}));
        cluster[3] = TypeLookup.getLVDiscreteFunctionTypeCode();
        assertThrows(CodecException.class, () -> decodeTypeDescriptor(cluster));
    }

    @Test
    void discreteFunctionDataIsTwoRowArray() throws CodecException {
        byte[] data = LVDataEncoder.encode(simple(df(new double[]{1.5}, new double[]{2.5})), DF_DESCRIPTOR);
        assertArrayEquals(dfData(2, 1, 1.5, 2.5), data);
    }

    @Test
    void discreteFunctionDataWithWrongRowCountFails() {
        assertThrows(CodecException.class, () -> decodeData(dfData(3, 1, 1, 2, 3), DF_DESCRIPTOR, null));
        assertThrows(CodecException.class, () -> decodeData(dfData(1, 2, 1, 2), DF_DESCRIPTOR, null));
    }

    @Test
    void discreteFunctionDataWithMorePointsThanDataFails() {
        assertThrows(CodecException.class, () -> decodeData(dfData(2, Integer.MAX_VALUE, 1, 2), DF_DESCRIPTOR, null));
    }

    @Test
    void discreteFunctionDataIsDecodedFromRows() throws CodecException {
        DiscreteFunction decoded = decodeData(dfData(2, 3, 1, 2, 3, 4, 5, 6), DF_DESCRIPTOR, null).castAsSimple().getDiscreteFunction();
        assertArrayEquals(new double[]{1, 2, 3}, decoded.toXArray());
        assertArrayEquals(new double[]{4, 5, 6}, decoded.toYArray());
        // an empty LabVIEW array may be flattened as 0 x 0
        assertEquals(0, decodeData(dfData(0, 0), DF_DESCRIPTOR, null).castAsSimple().getDiscreteFunction().toXArray().length);
    }

    @Test
    void discreteFunctionRoundTrip() throws CodecException {
        DiscreteFunction df = df(new double[]{0, 1, 2}, new double[]{10, 20, 30});
        SimpleParameterValue decoded = roundTrip("fn", simple(df), null).castAsSimple();
        assertEquals(ValueType.DISCRETE_FUNCTION, decoded.getValueType());
        assertArrayEquals(df.toXArray(), decoded.getDiscreteFunction().toXArray());
        assertArrayEquals(df.toYArray(), decoded.getDiscreteFunction().toYArray());
    }

    @Test
    void emptyDiscreteFunctionRoundTrip() throws CodecException {
        DiscreteFunction empty = df(new double[]{}, new double[]{});
        assertArrayEquals(dfData(2, 0), LVDataEncoder.encode(simple(empty), DF_DESCRIPTOR));
        SimpleParameterValue decoded = roundTrip("fn", simple(empty), null).castAsSimple();
        assertEquals(ValueType.DISCRETE_FUNCTION, decoded.getValueType());
        assertEquals(0, decoded.getDiscreteFunction().toXArray().length);
        assertEquals(0, decoded.getDiscreteFunction().toYArray().length);
    }

    private static DiscreteFunctionList sampleList() {
        return DomainValueFactory.newDiscreteFunctionList(
                df(new double[]{0, 1}, new double[]{5, 6}),
                df(new double[]{}, new double[]{}),
                df(new double[]{3, 4, 5}, new double[]{7, 8, 9}));
    }

    @Test
    void discreteFunctionListTypeDescriptorIsArrayOfOneElementCluster() throws CodecException {
        LVTypeDescriptor desc = LVTypeEncoder.describe("fns", simple(sampleList()));
        byte[] td = LVTypeEncoder.encodeTypeDescriptorBytes(desc);

        LVArrayDescriptor array = assertInstanceOf(LVArrayDescriptor.class, decodeTypeDescriptor(td));
        assertEquals("fns", array.getName());
        assertEquals(1, array.getDimension());
        LVClusterDescriptor cluster = assertInstanceOf(LVClusterDescriptor.class, array.getElemDescriptor());
        assertEquals(1, cluster.getFieldDescriptors().length);
        assertEquals(LVType.DISCRETE_FUNCTION, cluster.getFieldDescriptors()[0].getType());
        assertTrue(LVArrays.isDiscreteFunctionList(array));

        // 1D array (0x40) -> cluster (0x50) -> discrete function (0x60)
        assertEquals(0x40, td[3]);
        assertEquals(0x50, td[4 + 2 + 4 + 3]);
        assertEquals(0x60, td[4 + 2 + 4 + 4 + 2 + 3]);
        // size 0x30, array "fns" / size 0x22 cluster, one element / size 0x1C labelled "function" DF / DBL element
        assertArrayEquals(hex("0030 4040 0001 FFFFFFFF  0022 0050 0001  001C 4060 0002 FFFFFFFF FFFFFFFF 0004 000A 08 66756E6374696F6E 00  03 666E73"), td);
    }

    @Test
    void arrayOfOtherClustersIsNotADiscreteFunctionList() {
        LVTypeDescriptor twoFields = new LVArrayDescriptor("a", 1, new LVClusterDescriptor("", new LVTypeDescriptor[]{
                new LVTypeDescriptor(LVType.DISCRETE_FUNCTION, "f"), new LVTypeDescriptor(LVType.DBL, "d")}));
        LVTypeDescriptor otherField = new LVArrayDescriptor("a", 1, new LVClusterDescriptor("", new LVTypeDescriptor[]{
                new LVTypeDescriptor(LVType.DBL, "d")}));
        LVTypeDescriptor twoDim = new LVArrayDescriptor("a", 2, new LVClusterDescriptor("", new LVTypeDescriptor[]{
                new LVTypeDescriptor(LVType.DISCRETE_FUNCTION, "f")}));
        assertFalse(LVArrays.isDiscreteFunctionList(twoFields));
        assertFalse(LVArrays.isDiscreteFunctionList(otherField));
        assertFalse(LVArrays.isDiscreteFunctionList(twoDim));
        // other arrays of clusters stay unsupported by the data decoder
        assertThrows(CodecException.class, () -> decodeData(new byte[]{0, 0, 0, 0}, otherField, null));
        // and a discrete function list value needs the list descriptor
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(simple(sampleList()), otherField));
    }

    @Test
    void discreteFunctionListDataIsCountThenTwoRowArrays() throws CodecException {
        DiscreteFunctionList list = DomainValueFactory.newDiscreteFunctionList(df(new double[]{1.5}, new double[]{2.5}), df(new double[]{}, new double[]{}));
        byte[] data = LVDataEncoder.encode(simple(list), LVArrays.describeDiscreteFunctionList("fns"));
        ByteBuffer expected = ByteBuffer.allocate(4 + 24 + 8).putInt(2).putInt(2).putInt(1).putDouble(1.5).putDouble(2.5).putInt(2).putInt(0);
        assertArrayEquals(expected.array(), data);
    }

    @Test
    void discreteFunctionListRoundTrip() throws CodecException {
        DiscreteFunctionList list = sampleList();
        SimpleParameterValue decoded = roundTrip("fns", simple(list), null).castAsSimple();
        assertEquals(ValueType.DISCRETE_FUNCTION_LIST, decoded.getValueType());
        DiscreteFunctionList out = decoded.getDiscreteFunctionList();
        assertEquals(3, out.size());
        for (int i = 0; i < 3; i++) {
            assertArrayEquals(list.getFunction(i).toXArray(), out.getFunction(i).toXArray());
            assertArrayEquals(list.getFunction(i).toYArray(), out.getFunction(i).toYArray());
        }
    }

    @Test
    void emptyDiscreteFunctionListRoundTrip() throws CodecException {
        SimpleParameterValue decoded = roundTrip("fns", simple(DomainValueFactory.newDiscreteFunctionList()), null).castAsSimple();
        assertEquals(ValueType.DISCRETE_FUNCTION_LIST, decoded.getValueType());
        assertEquals(0, decoded.getDiscreteFunctionList().size());
    }

    @Test
    void discreteFunctionListInsideClusterRoundTrip() throws CodecException {
        MapParameterValue map = MapParameterValueFactory.newMapParameterValue();
        map.put("fns", simple(sampleList()));
        map.put("count", simple(7));
        MapParameterValue decoded = roundTrip("Setting", map, null).castAsMap();
        assertEquals(3, decoded.get("fns").getDiscreteFunctionList().size());
        assertArrayEquals(new double[]{7, 8, 9}, decoded.get("fns").getDiscreteFunctionList().getFunction(2).toYArray());
        assertEquals(7, decoded.get("count").getInt());
    }

    @Test
    void scalarValueDoesNotMatchAnArrayDescriptor() {
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(simple(df(new double[]{1}, new double[]{2})), LVArrays.describeDiscreteFunctionList("fns")));
        assertThrows(CodecException.class, () -> LVDataEncoder.encode(simple(sampleList()), DF_DESCRIPTOR));
    }
}
