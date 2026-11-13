package cern.rade.services.codec.labview;

import cern.japc.core.factory.MapParameterValueFactory;
import cern.japc.core.factory.SimpleParameterValueFactory;
import cern.japc.value.MapParameterValue;
import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.labview.LVClusterDescriptor;
import cern.rade.services.common.containers.labview.LVType;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;

/**
 * Encodes an error as the LabVIEW "error out" cluster: {@code status} (BOOL), {@code code} (I32) and {@code source} (string).
 */
public final class LVErrorEncoder {
    private static final LVClusterDescriptor ERROR_DESCRIPTOR = new LVClusterDescriptor("error out", new LVTypeDescriptor[]{
            new LVTypeDescriptor(LVType.BOOL, "status"),
            new LVTypeDescriptor(LVType.INT_32, "code"),
            new LVTypeDescriptor(LVType.STRING, "source")
    });

    private LVErrorEncoder() {
    }

    /**
     * @return the flattened data of the error cluster: status (1 byte), code (i32), source (i32 length, then the bytes).
     */
    public static byte[] encode(boolean status, int code, String source) throws CodecException {
        return LVDataEncoder.encode(toValue(status, code, source), ERROR_DESCRIPTOR);
    }

    /**
     * @return the flattened type descriptor of the error cluster, see {@link LVTypeEncoder#encodeTypeDescriptor(cern.rade.services.common.containers.labview.LVTypeDescriptor)}.
     */
    public static short[] encodeTypeDescriptor() throws CodecException {
        return LVTypeEncoder.encodeTypeDescriptor(ERROR_DESCRIPTOR);
    }

    private static MapParameterValue toValue(boolean status, int code, String source) {
        MapParameterValue value = MapParameterValueFactory.newMapParameterValue();
        value.put("status", SimpleParameterValueFactory.newSimpleParameterValue(status));
        value.put("code", SimpleParameterValueFactory.newSimpleParameterValue(code));
        value.put("source", SimpleParameterValueFactory.newSimpleParameterValue(source));
        return value;
    }
}
