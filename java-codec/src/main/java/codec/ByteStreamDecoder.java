package codec;

import cern.accsoft.commons.util.value.Pair;
import codec.lvtojava.LVDataDeserialiser;
import codec.lvtojava.LVTypeDecoder;
import codec.lvtojava.LVTypeDecoderImp;
import types.*;
import types.labview.LVTypeDescriptor;

import java.nio.ByteBuffer;

public class ByteStreamDecoder {
    public ByteStreamDecoder(){

    }

    public JavaDataContainer decode(byte[] rawData){
        ByteBuffer buffer = ByteBuffer.wrap(rawData);
        LVTypeDescriptor lvTypeDescriptor = LVTypeDecoderImp.decodeTypeDescriptor(buffer);
        return LVDataDeserialiser.deserialise(buffer, lvTypeDescriptor);
    }

}
