package codec.javatocmw;
import cern.cmw.data.Data;
import cern.cmw.data.DataFactory;
import cern.cmw.data.DataType;
import cern.japc.ext.cmwrda3.DataTranslator;
import types.JavaDataContainer;

public class CMWDataEncoder {
    public CMWDataEncoder(){

    }

    public static Data encode(JavaDataContainer javaData){
        Data data = DataFactory.createData();
        if (javaData.getDataType() == DataType.DT_DATA){
            encodeMulti(javaData, data);
        }
        else{
            encodeSingle(javaData, data);
        }
        return data;
    }

    private static void encodeSingle(JavaDataContainer javaData, Data data) {
        switch(javaData.getDataType()){
            case DT_BYTE -> data.append(javaData.getFieldName(), (byte)javaData.getData());
            case DT_SHORT -> data.append(javaData.getFieldName(), (short)javaData.getData());
            case DT_INT -> data.append(javaData.getFieldName(), (int)javaData.getData());
            case DT_LONG -> data.append(javaData.getFieldName(), (long)javaData.getData());
            case DT_FLOAT -> data.append(javaData.getFieldName(), (float)javaData.getData());
            case DT_DOUBLE -> data.append(javaData.getFieldName(), (double)javaData.getData());
            case DT_BOOL -> data.append(javaData.getFieldName(), (boolean)javaData.getData());
            case DT_STRING -> data.append(javaData.getFieldName(), (String)javaData.getData());
        }
    }

    private static void encodeMulti(JavaDataContainer javaData, Data data) {

    }

}
