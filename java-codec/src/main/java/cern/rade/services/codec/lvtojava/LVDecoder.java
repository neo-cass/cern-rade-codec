package cern.rade.services.codec.lvtojava;


import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;

import java.nio.ByteBuffer;

public final class LVDecoder {
    public static DataContainer<?> decode(ByteBuffer typeBuffer, ByteBuffer dataBuffer) {
        LVTypeDescriptor lvTypeDescriptor = LVTypeDecoder.decode(typeBuffer);
        return LVDataDecoder.decode(dataBuffer, lvTypeDescriptor);
    }
}
