package codec.javatocmw;
import cern.cmw.data.Data;
import cern.cmw.data.DataFactory;
import cern.cmw.data.DataType;
import cern.japc.ext.cmwrda3.DataTranslator;
public class CMWDataEncoder {
    public CMWDataEncoder(){

    }

    public void encode(){
        Data data = DataFactory.createData();
        data.append("test", 5);

        DataTranslator.createParameterValue("test", data, null);
        //DataTranslator.create
    }

}
