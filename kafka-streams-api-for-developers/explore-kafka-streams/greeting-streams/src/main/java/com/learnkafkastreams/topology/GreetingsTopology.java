package com.learnkafkastreams.topology;

import com.learnkafkastreams.domain.Greeting;
import com.learnkafkastreams.serdes.SerdesFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;

import static com.learnkafkastreams.utils.Constant.*;

@Slf4j
public class GreetingsTopology {
    public static Topology createTopology() {

        // Provides the high-level Kafka Streams DSL to specify a Kafka Streams topology.
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        // KStream - Source Processor
        KStream<String, Greeting> greetingsSourceKStream = streamsBuilder.stream(
                TOPIC_GREETINGS_CONSUMER,
                //Consumed.with(Serdes.String(), Serdes.String())
                Consumed.with(Serdes.String(), SerdesFactory.greetingGenericSerde())
        );

        // KStream 2 - Source Processor
        KStream<String, Greeting> greetingsSourceKStream2 = streamsBuilder.stream(
                TOPIC_GREETINGS_CONSUMER_2,
                Consumed.with(Serdes.String(), SerdesFactory.greetingGenericSerdeFromSerdes())
        );

        // Print - KStream Source Processor
        greetingsSourceKStream.print(
                Printed.<String, Greeting>toSysOut().withLabel(GREETINGS_SOURCE_STREAM_LABEL)
        );

        // Print - KStream 2 - Source Processor
        greetingsSourceKStream2.print(
                Printed.<String, Greeting>toSysOut().withLabel(GREETINGS_SOURCE_STREAM_LABEL_2)
        );

        // Operator - merge
        var mergedGreetingsKStream = greetingsSourceKStream.merge(greetingsSourceKStream2);

        // Operator - peek
        mergedGreetingsKStream.peek(
                (key, value) -> log.info("After Merging, Key {} & Value {}", key, value)
        );

        // Print - Merged Stream
        mergedGreetingsKStream.print(
                Printed.<String, Greeting>toSysOut().withLabel(GREETINGS_MERGED_STREAM_LABEL)
        );

        // KStream - Stream Processor
        //var greetingsTransformedKStream = getStringGreetingKStream(mergedGreetingsKStream);

        // KStream - Stream Processor - exception handler
        var greetingsTransformedKStream = callGreetingKStreamForErrors(mergedGreetingsKStream);

        // Operator - mapValues
        //.mapValues((readOnlyKey, value) -> value.toUpperCase())
        // Operator - flatMap
                /*.flatMap((key, value) -> {
                    String[] values = value.split(" ");
                    List<KeyValue<String, String>> keyValueList = new ArrayList<>();
                    for (String splitValue : values) {
                        keyValueList.add(KeyValue.pair(key, splitValue.toLowerCase()));
                    }
                    return keyValueList;
                })*/
        // Operator - flatMapValues
               /* .flatMapValues(
                        (readOnlyKey, value) -> {
                            String[] values = value.split("");
                            List<String> valueList = new ArrayList<>();
                            for (String splitValue : values) {
                                valueList.add(splitValue.concat("month"));
                            }
                            return valueList;
                        }
                );*/

        // Print - KStream Stream Processor
        greetingsTransformedKStream.print(
                Printed.<String, Greeting>toSysOut().withLabel(GREETINGS_TRANSFORMED_STREAM_LABEL)
        );

        // KStream - Sink Processor
        greetingsTransformedKStream.to(
                TOPIC_GREETINGS_PRODUCER,
                Produced.with(Serdes.String(), SerdesFactory.greetingSerde())
        );

        return streamsBuilder.build();
    }

    private static KStream<String, Greeting> getStringGreetingKStream(KStream<String, Greeting> mergedGreetingsKStream) {
        return mergedGreetingsKStream
                // Operator - filter
                .filter((key, value) -> value.getMessage().length() > 5)
                // Operator - filterNot
                .filterNot((key, value) -> value.getMessage().equalsIgnoreCase("Saturday"))
                // Operator - map
                .map((key, value) -> KeyValue.pair(
                        key != null ? key.toUpperCase() : null,
                        Greeting
                                .builder()
                                .message(value.getMessage().concat("2024"))
                                .timeStamp(value.getTimeStamp())
                                .build()
                ));
    }

    private static KStream<String, Greeting> callGreetingKStreamForErrors(KStream<String, Greeting> mergedGreetingsKStream) {
        return mergedGreetingsKStream
                .mapValues((readOnlyKey, value) -> {
                    if ("transient error".equalsIgnoreCase(value.getMessage())) {
                        throw new IllegalStateException("transient error");
                    }
                    return Greeting
                            .builder()
                            .message(value.getMessage().concat("2024"))
                            .timeStamp(value.getTimeStamp())
                            .build();
                });
    }
}
