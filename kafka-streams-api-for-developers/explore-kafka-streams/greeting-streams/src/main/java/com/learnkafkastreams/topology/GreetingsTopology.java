package com.learnkafkastreams.topology;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;

import java.util.ArrayList;
import java.util.List;

import static com.learnkafkastreams.utils.Constant.*;

@Slf4j
public class GreetingsTopology {
    public static Topology createTopology() {

        // Provides the high-level Kafka Streams DSL to specify a Kafka Streams topology.
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        // KStream - Source Processor
        var greetingsSourceKStream = streamsBuilder.stream(
                TOPIC_GREETINGS_CONSUMER,
                Consumed.with(Serdes.String(), Serdes.String())
        );

        // KStream 2 - Source Processor
        var greetingsSourceKStream2 = streamsBuilder.stream(
                TOPIC_GREETINGS_CONSUMER_2,
                Consumed.with(Serdes.String(), Serdes.String())
        );

        // Print - KStream Source Processor
        greetingsSourceKStream.print(
                Printed.<String, String>toSysOut().withLabel(GREETINGS_SOURCE_STREAM_LABEL)
        );

        // Print - KStream 2 - Source Processor
        greetingsSourceKStream2.print(
                Printed.<String, String>toSysOut().withLabel(GREETINGS_SOURCE_STREAM_LABEL_2)
        );

        // Operator - merge
        var mergedGreetingsKStream = greetingsSourceKStream.merge(greetingsSourceKStream2);

        // Operator - peek
        mergedGreetingsKStream.peek((key, value) ->
                log.info("After Merging, Key {} Length {} & Value {} Length {}",
                        key,
                        key.length(),
                        value,
                        value.length())
        );

        // Print - Merged Stream
        mergedGreetingsKStream.print(
                Printed.<String, String>toSysOut().withLabel(GREETINGS_MERGED_STREAM_LABEL)
        );

        // KStream - Stream Processor
        var greetingsTransformedKStream = mergedGreetingsKStream
                // Operator - filter
                .filter((key, value) -> value.length() > 5)
                // Operator - filterNot
                .filterNot((key, value) -> value.equalsIgnoreCase("Saturday"))
                // Operator - map
                .map((key, value) -> KeyValue.pair(
                        key != null ? key.toUpperCase() : null, value.concat("2024")
                ))
                // Operator - mapValues
                .mapValues((readOnlyKey, value) -> value.toUpperCase())
                // Operator - flatMap
                .flatMap((key, value) -> {
                    String[] values = value.split(" ");
                    List<KeyValue<String, String>> keyValueList = new ArrayList<>();
                    for (String splitValue : values) {
                        keyValueList.add(KeyValue.pair(key, splitValue.toLowerCase()));
                    }
                    return keyValueList;
                })
                // Operator - flatMapValues
                .flatMapValues(
                        (readOnlyKey, value) -> {
                            String[] values = value.split("");
                            List<String> valueList = new ArrayList<>();
                            for (String splitValue : values) {
                                valueList.add(splitValue.concat("month"));
                            }
                            return valueList;
                        }
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
