package com.learnkafkastreams.utils;

public class Constant {

    // Topology
    public static final String TOPIC_GREETINGS_CONSUMER = "greetings-consumer";
    public static final String TOPIC_GREETINGS_PRODUCER = "greetings-producer";
    public static final String GREETINGS_SOURCE_STREAM_LABEL = "greetings-source-stream";
    public static final String GREETINGS_TRANSFORMED_STREAM_LABEL = "greetings-transformed-stream";

    // Launcher
    public static final String GREETINGS_APPLICATION_ID = "greetings-application";
    public static final String BOOTSTRAP_SERVER = "localhost:9092";
    public static final String AUTO_OFFSET_RESET = "latest";
}
