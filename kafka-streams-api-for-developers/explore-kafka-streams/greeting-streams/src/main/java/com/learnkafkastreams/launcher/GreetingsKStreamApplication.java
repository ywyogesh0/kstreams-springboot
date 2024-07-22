package com.learnkafkastreams.launcher;

import com.learnkafkastreams.exceptionhandler.CustomDeserializerExceptionHandler;
import com.learnkafkastreams.exceptionhandler.CustomProcessorExceptionHandler;
import com.learnkafkastreams.exceptionhandler.CustomProductionExceptionHandler;
import com.learnkafkastreams.topology.GreetingsTopology;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.learnkafkastreams.utils.Constant.*;

@Slf4j
public class GreetingsKStreamApplication {

    public static void main(String[] args) {

        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, GREETINGS_APPLICATION_ID);
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_RESET);

        // Default Key/Value Serde using Application configuration
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);

        // num.stream.threads - consumer threads
        properties.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, "1");

        // Default Deserializer Exception Handler - default.deserialization.exception.handler
        properties.put(
                StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
                CustomDeserializerExceptionHandler.class
        );

        // Default Production (Serializer) Exception Handler - default.production.exception.handler
        properties.put(
                StreamsConfig.DEFAULT_PRODUCTION_EXCEPTION_HANDLER_CLASS_CONFIG,
                CustomProductionExceptionHandler.class
        );

        // Create Topics
        /*createTopics(properties, List.of(
                TOPIC_GREETINGS_CONSUMER,
                TOPIC_GREETINGS_PRODUCER,
                TOPIC_GREETINGS_CONSUMER_2
        ));*/

        // Launch KStream Application
        try {
            //noinspection resource
            KafkaStreams kafkaStreams = new KafkaStreams(
                    GreetingsTopology.createTopology(),
                    properties
            );

            // Set custom stream processor uncaught exception handler
            kafkaStreams.setUncaughtExceptionHandler(new CustomProcessorExceptionHandler());

            Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));

            kafkaStreams.start();
        } catch (Exception e) {
            log.error("Exception while launching Kafka Greeting App : {}", e.getMessage(), e);
        }
    }

    private static void createTopics(Properties config, List<String> greetings) {

        AdminClient admin = AdminClient.create(config);
        var partitions = 2;
        short replication = 1;

        var newTopics = greetings
                .stream()
                .map(topic -> new NewTopic(topic, partitions, replication))
                .collect(Collectors.toList());

        try {
            var createTopicResult = admin.createTopics(newTopics);
            createTopicResult
                    .all().get();
            log.info("Topics are created successfully...");
        } catch (Exception e) {
            log.error("Exception In Topics : {} ", e.getMessage(), e);
        }
    }
}
