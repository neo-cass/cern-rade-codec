package cern.rade.services.codec.labview;

import cern.japc.core.factory.MapParameterValueFactory;
import cern.japc.core.factory.SimpleParameterValueFactory;
import cern.japc.value.MapParameterValue;
import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.labview.LVClusterDescriptor;
import cern.rade.services.common.containers.labview.LVType;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;

public final class LVErrorEncoder {
    private static final LVClusterDescriptor ERROR_DESCRIPTOR = new LVClusterDescriptor("error out", new LVTypeDescriptor[]{
            new LVTypeDescriptor(LVType.BOOL, "status"),
            new LVTypeDescriptor(LVType.INT_32, "code"),
            new LVTypeDescriptor(LVType.STRING, "source")
    });

    private LVErrorEncoder() {
    }

    public static byte[] encode(boolean status, int code, String source) throws CodecException {
        return LVDataEncoder.encode(toValue(status, code, source), ERROR_DESCRIPTOR);
    }

    public static short[] encodeTypeDescriptor() throws CodecException {
        return LVTypeEncoder.encodeTypeDescriptor(ERROR_DESCRIPTOR, toValue(false, 0, ""));
    }

    private static MapParameterValue toValue(boolean status, int code, String source) {
        MapParameterValue value = MapParameterValueFactory.newMapParameterValue();
        value.put("status", SimpleParameterValueFactory.newSimpleParameterValue(status));
        value.put("code", SimpleParameterValueFactory.newSimpleParameterValue(code));
        value.put("source", SimpleParameterValueFactory.newSimpleParameterValue(source));
        return value;
    }
}
