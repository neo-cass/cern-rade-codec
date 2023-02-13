package communication.messages;

import types.labview.LVRawTypeDescriptor;

public class ClientMessage {
    public String username;
    public String password;
    public byte[] data;
    public RequestType reqType;
    public String selector;
    public String device;
    public ClientMessage(String username,
                           String password,
                           String data,
                           String reqType,
                           String selector,
                           String device)
    {
        this.username=username;
        this.password=password;
        this.data=data.getBytes();
        this.reqType=setReqType(reqType);
        this.selector=selector;
        this.device=device;
    }

    protected RequestType setReqType(String reqString){
        if(reqString.equals(RequestType.GET.getStr())){
            return RequestType.GET;
        }
        else if(reqString.equals(RequestType.SET.getStr())){
            return RequestType.SET;
        }
        else{
            return RequestType.SUBSCRIBE;
        }
    }

    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("username: ");
        stringBuilder.append(username);
        stringBuilder.append("\n");

        stringBuilder.append("data: ");
        stringBuilder.append(data);
        stringBuilder.append("\n");

        stringBuilder.append("request type: ");
        stringBuilder.append(reqType);
        stringBuilder.append("\n");

        stringBuilder.append("selector: ");
        stringBuilder.append(selector);
        stringBuilder.append("\n");

        stringBuilder.append("device: ");
        stringBuilder.append(device);
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}
