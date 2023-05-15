package cern.rade.services.codec;

import cern.rade.services.codec.lvtojava.LVDecoder;
import cern.rade.services.common.communication.client.*;
import cern.rade.services.common.containers.java.DataContainer;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.nio.ByteBuffer;

import static cern.rade.services.common.BytesUtil.shortArrToByteArr;


public final class RequestDecoder {
    private static JsonArray jsonArray;

    public static Request decodeMessage(HttpServletRequest request) throws IOException {
        String jsonMsg = request.getHeader("MetaData");

        MetaData metaData = new MetaData(JsonParser.parseString(jsonMsg).getAsJsonObject());
        switch(RequestLanguage.valueOf(metaData.getRequest_language())){
            case LABVIEW -> {return decodeLVRequest(request, metaData);}
            case PYTHON -> {return decodePYRequest(request, metaData);}
        }
        return null;
    }

    private static Request decodeLVRequest(HttpServletRequest request, MetaData metaData) throws IOException {
        ServletInputStream reader = request.getInputStream();
        DataContainer decodedData = null;
        byte[] data = reader.readAllBytes();
        short[] type_descriptor = metaData.getType_descriptor();
        if(data != null && type_descriptor != null){
            decodedData = decodeLVData(data, type_descriptor);
        }
        return new Request(metaData, decodedData, metaData.getRequest_action(), metaData.getRequest_language());
    }

    private static DataContainer decodeLVData(byte[] data, short[] type_descriptor){
        ByteBuffer dataBuf = ByteBuffer.wrap(data);
        ByteBuffer typeBuf = ByteBuffer.wrap(shortArrToByteArr(type_descriptor));
        return LVDecoder.decode(typeBuf, dataBuf);
    }

    private static Request decodePYRequest(HttpServletRequest request, MetaData metaData)  {
        return new Request(metaData, null, metaData.getRequest_action(), metaData.getRequest_language());
    }

}
