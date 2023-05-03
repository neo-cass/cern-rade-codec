package cern.rade.services.codec;

import cern.rade.services.codec.lvtojava.LVDecoder;
import cern.rade.services.common.communication.client.ClientMetaData;
import cern.rade.services.common.communication.client.Request;
import cern.rade.services.common.communication.client.RequestAction;
import cern.rade.services.common.communication.client.RequestLanguage;
import com.google.gson.Gson;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.nio.ByteBuffer;

import static cern.rade.services.common.BytesUtil.shortArrToByteArr;


public final class RequestDecoder {
    public static Request decodeMessage(HttpServletRequest request) throws IOException {
        Gson gson = new Gson();
        String jsonMsg = request.getHeader("MetaData");

        ClientMetaData metaData = gson.fromJson(jsonMsg, ClientMetaData.class);
        switch(RequestLanguage.valueOf(metaData.getRequest_language())){
            case LABVIEW -> {return decodeLVRequest(request, metaData);}
            case PYTHON -> {return decodePYRequest(request, metaData);}
            //TODO: cases for other languages
        }
        return null;
    }

    private static Request decodeLVRequest(HttpServletRequest request, ClientMetaData metaData) throws IOException {
        ServletInputStream reader = request.getInputStream();
        byte[] data = reader.readAllBytes();

        //TODO: change how to data is converted or how it arrives from LabVIEW
        byte[] typeDescriptor = shortArrToByteArr(metaData.getType_descriptor());

        ByteBuffer dataBuf = ByteBuffer.wrap(data);
        ByteBuffer typeBuf = ByteBuffer.wrap(typeDescriptor);
        return new Request(metaData, LVDecoder.decode(typeBuf, dataBuf), metaData.getRequest_action(), metaData.getRequest_language());
    }

    private static Request decodePYRequest(HttpServletRequest request, ClientMetaData metaData)  {
        return new Request(metaData, null, metaData.getRequest_action(), metaData.getRequest_language());
    }

}
