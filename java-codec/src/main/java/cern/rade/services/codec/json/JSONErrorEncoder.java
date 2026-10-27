package cern.rade.services.codec.json;

import cern.rade.services.codec.CodecException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

public final class JSONErrorEncoder {
    private static final ObjectMapper mapper = new ObjectMapper();

    private JSONErrorEncoder() {
    }

    public static String encode(boolean status, int code, String source, String message) throws CodecException {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status", status);
        error.put("code", code);
        error.put("source", source);
        error.put("message", message);
        try {
            return mapper.writeValueAsString(error);
        } catch (JsonProcessingException e) {
            throw new CodecException("Cannot encode error to JSON", e);
        }
    }
}
