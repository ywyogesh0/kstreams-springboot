package com.learnkafkastreams.launcher;


import com.learnkafkastreams.topology.OrdersTopology;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.learnkafkastreams.util.Constants.*;

@Slf4j
public class OrdersKafkaStreamApp {


    public static void main(String[] args) {

        // create an instance of the topology
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "orders-windowed-app"); // consumer group
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // read only the new messages
        config.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, "1");
        config.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, "0");

        createTopics(config, List.of(ORDERS_TOPIC, GENERAL_ORDERS_TOPIC, RESTAURANT_ORDERS_TOPIC, STORES_TOPIC));
        //createTopics(config, List.of(ORDERS_TOPIC, STORES_TOPIC));

        //Create an instance of KafkaStreams
        var kafkaStreams = new KafkaStreams(OrdersTopology.buildTopology(), config);

        //This closes the streams anytime the JVM shuts down normally or abruptly.
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));
        try {
            kafkaStreams.start();
        } catch (Exception e) {
            log.error("Exception in starting the Streams : {}", e.getMessage(), e);
        }

    }

    private static void createTopics(Properties config, List<String> topics) {

        AdminClient admin = AdminClient.create(config);
        var partitions = 1;
        short replication = 1;

        var newTopics = topics
                .stream()
                .map(topic -> new NewTopic(topic, partitions, replication))
                .collect(Collectors.toList());

        var createTopicResult = admin.createTopics(newTopics);
        try {
            createTopicResult
                    .all().get();
            log.info("topics are created successfully");
        } catch (Exception e) {
            log.error("Exception creating topics : {} ", e.getMessage(), e);
        }
    }

}
