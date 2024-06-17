package com.springboot.kstream.greetings.topology;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.kstream.greetings.domain.Greeting;
import com.springboot.kstream.greetings.utils.Constants;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

@Component
public class GreetingTopology {

    private final ObjectMapper objectMapper;

    public GreetingTopology(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void process(StreamsBuilder streamsBuilder) {
        // consumer kstream
        KStream<String, Greeting> greetingsConsumerKStream = streamsBuilder
                .stream(
                        Constants.TOPIC_GREETINGS_CONSUMER,
                        Consumed.with(
                                Serdes.String(),
                                new JsonSerde<>(Greeting.class, objectMapper)
                        )
                );

        // print consumer kstream
        greetingsConsumerKStream
                .print(
                        Printed.<String, Greeting>toSysOut().withLabel("greetings-consumer-stream")
                );

        // transform stream to upper case
        KStream<String, Greeting> greetingsUpperCaseKStream = greetingsConsumerKStream
                .mapValues(
                        (readOnlyKey, value) -> {
                            if (value.getMessage().equals("Transient Error")) {
                                throw new IllegalStateException("Throwing RuntTime Exception...");
                            }
                            return Greeting
                                    .builder()
                                    .message(value.getMessage().toUpperCase())
                                    .timeStamp(value.getTimeStamp())
                                    .build();
                        }
                );

        // print upper-case kstream
        greetingsUpperCaseKStream
                .print(
                        Printed.<String, Greeting>toSysOut().withLabel("greetings-uppercase-stream")
                );

        // produce upper-case kstream to producer topic
        greetingsUpperCaseKStream
                .to(
                        Constants.TOPIC_GREETINGS_PRODUCER,
                        Produced.with(
                                Serdes.String(),
                                new JsonSerde<>(Greeting.class, objectMapper)
                        )
                );
    }
}
