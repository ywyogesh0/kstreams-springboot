package com.learnkafkastreams.serdes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.learnkafkastreams.domain.Greeting;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

public class SerdesFactory {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    public static Serde<Greeting> greetingSerde() {
        return new GreetingSerde(objectMapper);
    }

    public static Serde<Greeting> greetingGenericSerde() {
        return new JsonGenericSerde<>(objectMapper, Greeting.class);
    }

    public static Serde<Greeting> greetingGenericSerdeFromSerdes() {
        Serializer<Greeting> serializer = new JsonGenericSerializer<>(objectMapper);
        Deserializer<Greeting> deserializer = new JsonGenericDeserializer<>(objectMapper, Greeting.class);

        return Serdes.serdeFrom(serializer, deserializer);
    }
}
