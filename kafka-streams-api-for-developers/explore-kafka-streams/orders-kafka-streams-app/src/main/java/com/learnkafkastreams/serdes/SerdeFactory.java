package com.learnkafkastreams.serdes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.learnkafkastreams.domain.Order;
import com.learnkafkastreams.domain.Revenue;
import org.apache.kafka.common.serialization.Serde;

public class SerdeFactory {

    public static Serde<Order> generateOrderSerde() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        return new JsonSerde<>(objectMapper, Order.class);
        //return Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>());
    }

    public static Serde<Revenue> generateRevenueSerde() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        return new JsonSerde<>(objectMapper, Revenue.class);
        //return Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>());
    }
}