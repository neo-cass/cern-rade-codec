package cern.rade.services.codec.labview;


import cern.rade.services.codec.CodecException;
import cern.japc.value.ParameterValue;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;

import java.nio.ByteBuffer;

public final class LVDecoder {
    public static ParameterValue decode(ByteBuffer typeBuffer, ByteBuffer dataBuffer) throws CodecException {
        LVTypeDescriptor lvTypeDescriptor = LVTypeDecoder.decode(typeBuffer);
        return LVDataDecoder.decode(dataBuffer, lvTypeDescriptor);
    }
}
