package com.learnkafkastreams.launcher;

import com.learnkafkastreams.topology.GreetingsTopology;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.learnkafkastreams.utils.Constant.*;

@Slf4j
public class GreetingsStreamApp {

    public static void main(String[] args) {

        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, GREETINGS_APPLICATION_ID);
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_RESET);

        // Create Topics
        createTopics(properties, List.of(TOPIC_GREETINGS_CONSUMER, TOPIC_GREETINGS_PRODUCER));

        // Launch KStream Application
        try {
            //noinspection resource
            KafkaStreams kafkaStreams = new KafkaStreams(
                    GreetingsTopology.createTopology(),
                    properties
            );

            Runtime.getRuntime().addShutdownHook(
                    new Thread(kafkaStreams::close)
            );

            kafkaStreams.start();
        } catch (Exception e) {
            log.error("Exception while launching Kafka Greeting App : {}", e.getMessage(), e);
        }
    }

    private static void createTopics(Properties config, List<String> greetings) {

        AdminClient admin = AdminClient.create(config);
        var partitions = 1;
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
