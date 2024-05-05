package com.learnkafkastreams.serdes;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

@Slf4j
public class JsonGenericDeserializer<T> implements Deserializer<T> {

    private final ObjectMapper objectMapper;
    private final Class<T> destinationClass;

    public JsonGenericDeserializer(ObjectMapper objectMapper, Class<T> destinationClass) {
        this.objectMapper = objectMapper;
        this.destinationClass = destinationClass;
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            return objectMapper.readValue(data, destinationClass);
        } catch (IOException e) {
            log.error("Exception In Json Generic Deserializer: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
