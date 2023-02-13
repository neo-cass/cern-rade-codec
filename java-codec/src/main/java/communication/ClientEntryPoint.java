package communication;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.common.

import java.io.IOException;

public class ClientEntryPoint extends HttpServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        String jsonMsg = request.getHeader("jsonMessage");
        JSONObj
    }
}
