package cern.rade.services.codec.lvtojava;

import cern.rade.services.common.types.*;
import cern.rade.services.common.types.labview.LVTypeDescriptor;

import java.nio.ByteBuffer;

public final class LVDecoder {
    public static JavaContainer<?> decode(ByteBuffer typeBuffer, ByteBuffer dataBuffer) {
        LVTypeDescriptor lvTypeDescriptor = LVTypeDecoder.decode(typeBuffer);
        return LVDataDecoder.decode(dataBuffer, lvTypeDescriptor);
    }
}
