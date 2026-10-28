package cern.rade.services.codec.json;

import cern.japc.json.JapcModule;
import cern.japc.value.ParameterValue;
import cern.rade.services.codec.CodecException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JSONCodec {
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JapcModule());

    private JSONCodec() {
    }

    public static String encode(ParameterValue value) throws CodecException {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new CodecException("Cannot encode ParameterValue to JSON", e);
        }
    }

    public static ParameterValue decode(String json) throws CodecException {
        try {
            return mapper.readValue(json, ParameterValue.class);
        } catch (JsonProcessingException e) {
            throw new CodecException("Cannot decode JSON to ParameterValue", e);
        }
    }
}
