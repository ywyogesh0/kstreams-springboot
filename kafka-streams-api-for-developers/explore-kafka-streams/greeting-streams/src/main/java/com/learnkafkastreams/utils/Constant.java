package com.learnkafkastreams.utils;

public class Constant {

    // Topology
    public static final String TOPIC_GREETINGS_CONSUMER = "greetings-consumer";
    public static final String TOPIC_GREETINGS_CONSUMER_2 = "greetings-consumer-2";
    public static final String TOPIC_GREETINGS_PRODUCER = "greetings-producer";
    public static final String GREETINGS_SOURCE_STREAM_LABEL = "greetings-source-stream";
    public static final String GREETINGS_SOURCE_STREAM_LABEL_2 = "greetings-source-stream-2";
    public static final String GREETINGS_TRANSFORMED_STREAM_LABEL = "greetings-transformed-stream";
    public static final String GREETINGS_MERGED_STREAM_LABEL = "greetings-merged-stream";

    // Launcher
    public static final String GREETINGS_APPLICATION_ID = "greetings-application";
    public static final String BOOTSTRAP_SERVER = "localhost:9092";
    public static final String AUTO_OFFSET_RESET = "latest";
}
