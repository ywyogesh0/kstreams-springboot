package com.springboot.kstream.greetings.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.springboot.kstream.greetings.utils.Constants;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class GreetingStreamsConfiguration {

    @Bean
    public ObjectMapper createObjectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Bean
    public NewTopic createTopicConsumer() {
        return TopicBuilder
                .name(Constants.TOPIC_GREETINGS_CONSUMER)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic createTopicProducer() {
        return TopicBuilder
                .name(Constants.TOPIC_GREETINGS_PRODUCER)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
