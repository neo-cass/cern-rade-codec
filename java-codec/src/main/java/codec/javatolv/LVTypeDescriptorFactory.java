package codec.javatolv;

import cern.cmw.data.DataType;
import types.JavaDataContainer;

import java.nio.ByteBuffer;

public class LVTypeDescriptorFactory {
    public byte[] createTypeDescrptor(JavaDataContainer javaDataContainer){
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[50]);
        int len = 0;
        return null;
    }

    public byte[] createClusterType(){
        return null;
    }
}
