package types;

import types.labview.LVTypeDescriptor;

import java.nio.ByteBuffer;

public class IntermediateDataContainer {
    private types.labview.LVTypeDescriptor LVTypeDescriptor;
    private ByteBuffer byteBuffer;

    public IntermediateDataContainer(byte[] data, LVTypeDescriptor LVTypeDescriptor){
        this.byteBuffer = ByteBuffer.wrap(data);;
        this.LVTypeDescriptor = LVTypeDescriptor;
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }
    public LVTypeDescriptor getTypeDescriptor(){
        return LVTypeDescriptor;
    }
}
