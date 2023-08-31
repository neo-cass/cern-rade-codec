package cern.rade.services.codec.lvtojava;


import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.DataMismatchException;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;

import java.nio.ByteBuffer;

public final class LVDecoder {
    public static DataContainer decode(ByteBuffer typeBuffer, ByteBuffer dataBuffer) throws CodecException{
        LVTypeDescriptor lvTypeDescriptor = LVTypeDecoder.decode(typeBuffer);
        DataContainer dataContainer = LVDataContainerFactory.createDataContainer(lvTypeDescriptor);
        LVDataDecoder.decode(dataBuffer, dataContainer, lvTypeDescriptor);
        return dataContainer;
    }
}
