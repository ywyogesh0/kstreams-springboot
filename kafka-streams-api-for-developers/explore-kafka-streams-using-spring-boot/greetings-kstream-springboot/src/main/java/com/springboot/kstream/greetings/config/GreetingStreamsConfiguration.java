package com.springboot.kstream.greetings.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.springboot.kstream.greetings.exceptionhandler.CustomProcessorExceptionHandler;
import com.springboot.kstream.greetings.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.streams.RecoveringDeserializationExceptionHandler;

import java.util.Map;

@Configuration
@Slf4j
public class GreetingStreamsConfiguration {

    @Autowired
    KafkaProperties kafkaProperties;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Bean
    public StreamsBuilderFactoryBeanConfigurer streamsBuilderFactoryBeanConfigurer() {
        log.error("Setting StreamsUncaughtExceptionHandler...");
        return factoryBean -> factoryBean
                .setStreamsUncaughtExceptionHandler(new CustomProcessorExceptionHandler());
    }

    // RecoveringDeserializationExceptionHandler using spring kafka streams
    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kStreamsConfigs() {
        Map<String, Object> properties = kafkaProperties.buildStreamsProperties(null);
        properties
                .put(
                        StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
                        RecoveringDeserializationExceptionHandler.class
                );
        properties
                .put(
                        RecoveringDeserializationExceptionHandler.KSTREAM_DESERIALIZATION_RECOVERER,
                        consumerRecordRecoverer);
        return new KafkaStreamsConfiguration(properties);
    }

    /*@Bean
    public DeadLetterPublishingRecoverer recoverer() {
        log.error("Error: Invoking DeadLetterPublishingRecoverer...");
        return new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new TopicPartition("recovererDLQ", record.partition())
        );
    }*/

    /*@Bean
    public ConsumerRecordRecoverer recoverer() {
        return (consumerRecord, exception) -> log.error(
                "Error: consumerRecord: {}, exception.message: {}", consumerRecord, exception.getMessage(), exception
        );
    }*/

    private final ConsumerRecordRecoverer consumerRecordRecoverer = (consumerRecord, exception) -> log.error(
            "Error: consumerRecord: {}, exception.message: {}", consumerRecord, exception.getMessage(), exception
    );

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
