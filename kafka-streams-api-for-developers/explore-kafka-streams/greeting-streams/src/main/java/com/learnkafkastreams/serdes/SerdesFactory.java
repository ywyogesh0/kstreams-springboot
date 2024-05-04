package com.learnkafkastreams.serdes;

import com.learnkafkastreams.domain.Greeting;
import org.apache.kafka.common.serialization.Serde;

public class SerdesFactory {

    public static Serde<Greeting> greetingSerde() {
        return new GreetingSerde();
    }
}
