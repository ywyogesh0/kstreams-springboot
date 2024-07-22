package com.learnkafkastreams.domain;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class GreetingTest {

    @Test
    void greetingsJson() throws JsonProcessingException {
        var objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        var greeting = new Greeting("Good Morning", LocalDateTime.now());

        var greetingJSON = objectMapper.writeValueAsString(greeting);
        System.out.println("greetingJSON:" + greetingJSON);

        var greetingObject = objectMapper.readValue(greetingJSON, Greeting.class);
        System.out.println("greetingObject : " + greetingObject);

        Assertions.assertEquals("Good Morning", greetingObject.getMessage());
    }
}
