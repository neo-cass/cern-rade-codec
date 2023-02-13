package RequestHandlers;

import cern.japc.core.AcquiredParameterValue;
import cern.japc.core.Parameter;
import cern.japc.core.Parameters;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;
import communication.messages.Request;
import types.JavaContainer;

public class GetHandler {
    public JavaContainer<?> get(Request request){
//        String name = Parameters.buildParameterName(request.device, propertyName, fieldName);
//        System.out.println(name);
//        Parameter p = ParameterFactory.newInstance().newParameter(name);
//        cern.japc.core.Selector s = SelectorFactory.newSelector("");
//        AcquiredParameterValue apv = p.getValue(s);
//        out.println(apv.getValue().castAsSimple().getDouble());
        return null;
    }
}
