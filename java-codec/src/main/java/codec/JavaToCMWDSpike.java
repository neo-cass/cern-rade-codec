package codec;

import cern.cmw.data.Data;
import cern.cmw.data.DataFactory;
import types.JavaContainer;

import java.util.function.Function;

public class JavaToCMWDSpike<T> implements Function<JavaContainer<T>, Data> {
    @Override
    public Data apply(JavaContainer<T> javaContainer) {
        return null;
    }

    private Data encodeSingle(JavaContainer<T> javaContainer){
        Data cmwData = DataFactory.createData();
        cmwData.append();

    }

}
