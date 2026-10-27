package cern.rade.services.codec.json;

import cern.japc.core.factory.MapParameterValueFactory;
import cern.japc.json.JapcModule;
import cern.japc.value.MapParameterValue;
import cern.japc.value.ParameterValue;
import cern.japc.value.SimpleParameterValue;
import cern.japc.value.Type;
import cern.rade.services.codec.CodecException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

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

    public static String encode(ParameterValue value, List<String> fields) throws CodecException {
        if (fields == null) {
            return encode(value);
        }
        if (value.getType() != Type.MAP) {
            throw new CodecException("Fields can only be selected from a property, not a single field");
        }
        MapParameterValue map = value.castAsMap();
        MapParameterValue subset = MapParameterValueFactory.newMapParameterValue();
        for (String field : fields) {
            SimpleParameterValue fieldValue = map.get(field);
            if (fieldValue == null) {
                throw new CodecException("Field " + field + " not found");
            }
            subset.put(field, fieldValue);
        }
        return encode(subset);
    }

    public static ParameterValue decode(String json) throws CodecException {
        try {
            return mapper.readValue(json, ParameterValue.class);
        } catch (JsonProcessingException e) {
            throw new CodecException("Cannot decode JSON to ParameterValue", e);
        }
    }
}
