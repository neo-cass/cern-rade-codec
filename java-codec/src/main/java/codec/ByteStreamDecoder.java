package codec;

import codec.lvtojava.LVTypeDecoder;
import types.*;

import java.util.Arrays;

public class ByteStreamDecoder {
    private byte[] stream;
    private byte[] typeDesc;
    private byte[] data;
    private int typeDescLen;
    public ByteStreamDecoder(byte[] stream){
        this.stream = stream;
        this.typeDescLen = ((stream[0] & 0xFF) << 8) | (stream[1] & 0xFF);
    }

    private byte[] getData(){
        return Arrays.copyOfRange(stream, typeDescLen, stream.length);
    }
    private byte[] getType(){
        return Arrays.copyOfRange(stream, 0, typeDescLen);
    }

    public IntermediateDataContainer decode(){
        LVTypeDecoder LVTypeDecoder = new LVTypeDecoder();
        return new IntermediateDataContainer(getData(), LVTypeDecoder.decodeTypeFull(getType()));
    }


}
