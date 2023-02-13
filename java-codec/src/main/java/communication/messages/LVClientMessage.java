package communication.messages;

import codec.lvtojava.LVDecoder;
import types.labview.LVRawTypeDescriptor;

public class LVClientMessage extends ClientMessage{
    public String username;
    public String password;
    public byte[] data;
    public byte[] typeString;
    public RequestType reqType;
    public String selector;
    public String device;
    public LVClientMessage(String username,
     String password,
     String data,
     String typeString,
     String reqType,
     String selector,
     String device)
    {
        super(username, password, data, reqType, selector, device);
        this.typeString = typeString.getBytes();
    }

    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.toString());
        stringBuilder.append("type string: ");
        stringBuilder.append(LVRawTypeDescriptor.wrap(typeString));
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    public Request toRequest() {
        return new Request(username, password, LVDecoder.decode(typeString, data), selector, device);
    }
}
