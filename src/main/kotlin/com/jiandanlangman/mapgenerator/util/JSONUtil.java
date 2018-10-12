package com.jiandanlangman.mapgenerator.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public final class JSONUtil {

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.setSerializationInclusion(JsonInclude.Include.ALWAYS);
    }

    private JSONUtil() {

    }


    public static String toJSON(Object obj) {
        String result;
        try {
            result =  MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException ignore) {
            result = null;
        }
        return result;
    }


    public static <T> T fromJSON(String json, Class<T> clazz) {
        T result;
        try {
            result = MAPPER.readValue(json, clazz);
        } catch (IOException ignore) {
            result = null;
        }
        return result;
    }

}
