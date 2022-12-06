package codec.lvtojava;

import types.*;

import java.nio.ByteBuffer;

public final class LVDecoder {
    private LVDecoder(){}
    public static JavaContainer decode(byte[] bytes){
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int length = buffer.getShort();
        ByteBuffer typeBuffer = buffer.slice(0, length);
        ByteBuffer dataBuffer = buffer.slice(length, bytes.length - length);
        return LVDataDeserialiser.deserialise(dataBuffer, LVTypeDecoder.decode(typeBuffer));
    }

}
