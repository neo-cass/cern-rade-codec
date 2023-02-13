package communication.messages;

import types.JavaContainer;

public class Request {
    public String username;
    public String password;
    public byte[] data;
    public RequestType reqType;
    public String selector;
    public String device;

    public Request(String username,
                   String password,
                   JavaContainer<?> data,
                   String selector,
                   String device)
    {

    }

}
