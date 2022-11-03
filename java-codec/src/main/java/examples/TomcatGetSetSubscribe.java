package examples;

import cern.japc.core.AcquiredParameterValue;
import cern.japc.core.Parameter;
import cern.japc.core.ParameterException;
import cern.japc.core.SubscriptionHandle;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;
import cern.japc.core.Selector;
import cern.japc.core.factory.SimpleParameterValueFactory;
import cern.japc.value.ParameterValue;
import cern.japc.value.Type;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

@WebServlet(name = "tomcatGetSetSubscribe", value = "/get-set-subscribe")
public class TomcatGetSetSubscribe extends HttpServlet{
    private SubscriptionHandle sh;
    private HashMap<UDPSender, Thread> threadPool;
    private ArrayList<Integer> portPool;
    public void init() {
        threadPool = new HashMap<UDPSender, Thread>();
        portPool = new ArrayList<Integer>();
        for (int i = 1024; i<49152; i++){
            portPool.add(i);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response){
        String requestType = request.getHeader("RequestType");
        //String deviceName = request.getHeader("DeviceName");
        String parameterName = request.getHeader("PropertyName");


        try {
            switch (requestType) {
                case "Get" -> get(parameterName, response);
                case "Subscribe" -> subscribe(parameterName, response);
                case "Set" -> {String valueString = request.getHeader("Value");set(Double.parseDouble(valueString), parameterName, response);}
            }
        }
        catch(ParameterException | IOException e){
            e.printStackTrace();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        out.println("Build Successful");
    }


    private void get(String parameterName, HttpServletResponse response) throws ParameterException, IOException {
        PrintWriter out = response.getWriter();
        Parameter p = ParameterFactory.newInstance().newParameter(parameterName);
        cern.japc.core.Selector s = SelectorFactory.newSelector("");
        AcquiredParameterValue apv = p.getValue(s);
        out.println(apv.getValue());
        apv.getValue().castAsSimple().getDouble();
    }

    private void set(double value, String parameterName, HttpServletResponse response) throws ParameterException, IOException {
        PrintWriter out = response.getWriter();
        Parameter p = ParameterFactory.newInstance().newParameter(parameterName);
        Selector s = SelectorFactory.newSelector("");
        ParameterValue pv = SimpleParameterValueFactory.newSimpleParameterValue(value);
        p.setValue(s, pv);
        out.println("Value set");
    }

    public void subscribe(String parameterName, HttpServletResponse response) throws ParameterException, IOException {
        LinkedBlockingQueue<AcquiredParameterValue> queue = new LinkedBlockingQueue<AcquiredParameterValue>();
        Parameter p = ParameterFactory.newInstance().newParameter(parameterName);
        PrintWriter out = response.getWriter();
        //set selector string to "" as the parameter is cycle-independent
        Selector s = SelectorFactory.newSelector("");

        //pass in queue which will be used to pass values to this class when pvl's valueReceived method is called by JAPC
        TestParameterValueListener pvl = new TestParameterValueListener(queue);
        sh = p.createSubscription(s, pvl);

        //start subscription
        sh.startMonitoring();
        int hostPort = portPool.get(0);
        portPool.remove(0);
        int sendPort = portPool.get(0);
        portPool.remove(0);
        out.println(sendPort);
        UDPSender udpSender = new UDPSender(sendPort, hostPort, "localhost", sh, queue, response);
        Thread udpThread = new Thread(udpSender);
        threadPool.put(udpSender, udpThread);
        udpThread.start();
    }


    public void destroy() {
        sh.stopMonitoring();
    }
    
}








