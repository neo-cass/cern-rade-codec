package codec.lvtojava;

import types.*;

import java.nio.ByteBuffer;

public final class LVDecoder {
    public static JavaContainer<?> decode(byte[] typeDesc, byte[] data) {
        ByteBuffer dataBuffer = null;
        if (data!=null){dataBuffer = ByteBuffer.wrap(data);}
        ByteBuffer typeBuffer = ByteBuffer.wrap(typeDesc);
        return LVDataDecoder.deserialise(dataBuffer, LVTypeDecoder.decode(typeBuffer));
    }
}
