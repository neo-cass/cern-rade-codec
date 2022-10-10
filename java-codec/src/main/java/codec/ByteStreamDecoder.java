package codec;

import codec.lvtojava.LVDeserialiser;
import codec.lvtojava.LVTypeDecoder;
import types.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
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

    public ArrayList<DataContainer> decode(){
        LVTypeDecoder LVTypeDecoder = new LVTypeDecoder();
        IntermediateDataContainer idc = new IntermediateDataContainer(ByteBuffer.wrap(getData()), LVTypeDecoder.decodeTypeFull(getType()));
        LVDeserialiser lvDeserialiser = new LVDeserialiser();
        ArrayList<DataContainer> dataContainers = lvDeserialiser.deserialise(idc);
        for (DataContainer dataContainer : dataContainers) {
            System.out.println("HI");
            System.out.println(dataContainer.getData());
        }

        return dataContainers;
    }


}
