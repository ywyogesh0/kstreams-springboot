package com.learnkafkastreams.serdes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class JsonGenericSerde<T> implements Serde<T> {

    private final ObjectMapper objectMapper;
    private final Class<T> destinationClass;

    public JsonGenericSerde(ObjectMapper objectMapper, Class<T> destinationClass) {
        this.objectMapper = objectMapper;
        this.destinationClass = destinationClass;
    }

    @Override
    public Serializer<T> serializer() {
        return new JsonGenericSerializer<>(objectMapper);
    }

    @Override
    public Deserializer<T> deserializer() {
        return new JsonGenericDeserializer<>(objectMapper, destinationClass);
    }
}
