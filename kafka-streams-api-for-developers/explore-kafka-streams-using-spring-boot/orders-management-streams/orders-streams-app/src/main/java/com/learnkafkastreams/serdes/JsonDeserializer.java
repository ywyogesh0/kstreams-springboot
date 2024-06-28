package com.learnkafkastreams.serdes;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

@Slf4j
public class JsonDeserializer<T> implements Deserializer<T> {

    private final ObjectMapper objectMapper;
    private final Class<T> genericClassType;

    public JsonDeserializer(ObjectMapper objectMapper, Class<T> genericClassType) {
        this.objectMapper = objectMapper;
        this.genericClassType = genericClassType;
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            return objectMapper.readValue(data, genericClassType);
        } catch (IOException e) {
            log.error("Exception during Json Deserializer: {} ", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
