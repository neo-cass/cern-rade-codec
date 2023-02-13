package communication;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import communication.messages.LVClientMessage;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LVEntryPoint extends HttpServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) {

    }

    public LVClientMessage decodeMessage(HttpServletRequest request){
        Gson gson = new Gson();
        String jsonMsg = request.getHeader("jsonMessage");
        LVClientMessage lvClientMessage = gson.fromJson(jsonMsg, LVClientMessage.class);
        return lvClientMessage;
    }

    public void requestCaller(LVClientMessage lvClientMessage){
//        switch(lvClientMessage.reqType){
//            case GET ->
//            case SET ->
//            case SUBSCRIBE ->
//        }
    }
}
