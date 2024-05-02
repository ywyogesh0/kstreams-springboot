package com.learnkafkastreams.topology;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;

import static com.learnkafkastreams.utils.Constant.*;

public class GreetingsTopology {
    public static Topology createTopology() {

        // Provides the high-level Kafka Streams DSL to specify a Kafka Streams topology.
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        // KStream - Source Processor
        var greetingsSourceKStream = streamsBuilder.stream(
                TOPIC_GREETINGS_CONSUMER,
                Consumed.with(Serdes.String(), Serdes.String())
        );

        // Print - KStream Source Processor
        greetingsSourceKStream.print(
                Printed.<String, String>toSysOut().withLabel(GREETINGS_SOURCE_STREAM_LABEL)
        );

        // KStream - Stream Processor
        var greetingsTransformedKStream = greetingsSourceKStream.mapValues(
                (readOnlyKey, value) -> value.toUpperCase()
        );

        // Print - KStream Stream Processor
        greetingsTransformedKStream.print(
                Printed.<String, String>toSysOut().withLabel(GREETINGS_TRANSFORMED_STREAM_LABEL)
        );

        // KStream - Sink Processor
        greetingsTransformedKStream.to(
                TOPIC_GREETINGS_PRODUCER,
                Produced.with(Serdes.String(), Serdes.String())
        );

        return streamsBuilder.build();
    }
}
