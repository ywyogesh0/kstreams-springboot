package com.learnkafkastreams.serdes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class JsonSerde<T> implements Serde<T> {

    private final ObjectMapper objectMapper;
    private final Class<T> genericClassType;

    public JsonSerde(ObjectMapper objectMapper, Class<T> genericClassType) {
        this.objectMapper = objectMapper;
        this.genericClassType = genericClassType;
    }

    @Override
    public Serializer<T> serializer() {
        return new JsonSerializer<>(objectMapper);
    }

    @Override
    public Deserializer<T> deserializer() {
        return new JsonDeserializer<>(objectMapper, genericClassType);
    }
}
