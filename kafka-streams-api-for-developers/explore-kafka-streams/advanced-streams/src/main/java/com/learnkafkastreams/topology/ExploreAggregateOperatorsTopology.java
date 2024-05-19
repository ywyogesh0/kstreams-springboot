package com.learnkafkastreams.topology;

import com.learnkafkastreams.domain.AlphabetWordAggregate;
import com.learnkafkastreams.serdes.SerdesFactory;
import com.learnkafkastreams.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.ArrayList;

@Slf4j
public class ExploreAggregateOperatorsTopology {

    public static Topology build() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KStream<String, String> aggregateStream = streamsBuilder
                .stream(
                        Constants.TOPIC_AGGREGATE,
                        Consumed.with(Serdes.String(), Serdes.String())
                );

        // explore count operator
        //exploreCountOperator(aggregateStream);

        // explore reduce operator
        //exploreReduceOperator(aggregateStream);

        // explore count operator
        exploreAggregateOperator(aggregateStream);

        return streamsBuilder.build();
    }

    private static void exploreCountOperator(KStream<String, String> aggregateStream) {
        KGroupedStream<String, String> kGroupedStream = aggregateStream
                /*.groupByKey(
                        Grouped.with(Serdes.String(), Serdes.String())
                );*/
                .groupBy(
                        (key, value) -> {
                            log.info("Before re-partition: key is {}, value is {}", key, value);
                            return value;
                        },
                        Grouped.with(Serdes.String(), Serdes.String())
                );

        KTable<String, Long> kGroupedTable = kGroupedStream
                .count(
                        Named.as("explore-count-processor"),
                        Materialized
                                .<String, Long, KeyValueStore<Bytes, byte[]>>as("explore-count")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Long())
                );

        kGroupedTable
                .toStream()
                .print(Printed.<String, Long>toSysOut().withLabel("explore-count"));
    }

    private static void exploreReduceOperator(KStream<String, String> aggregateStream) {
        KGroupedStream<String, String> kGroupedStream = aggregateStream
                .groupByKey(
                        Grouped.with(Serdes.String(), Serdes.String())
                );

        KTable<String, String> kGroupedTable = kGroupedStream
                .reduce(
                        (value1, value2) -> {
                            log.info("value1: {}, value2: {}", value1, value2);
                            return value1.toUpperCase() + "-" + value2.toUpperCase();
                        },
                        Named.as("explore-reduce-processor"),
                        Materialized.as("explore-reduce")
                );

        kGroupedTable
                .toStream()
                .print(Printed.<String, String>toSysOut().withLabel("explore-reduce"));
    }

    private static void exploreAggregateOperator(KStream<String, String> aggregateStream) {
        KGroupedStream<String, String> kGroupedStream = aggregateStream
                .groupByKey(
                        Grouped.with(Serdes.String(), Serdes.String())
                );

        KTable<String, AlphabetWordAggregate> kGroupedTable = kGroupedStream
                .aggregate(
                        () -> new AlphabetWordAggregate("", new ArrayList<>(), 0),
                        (key, value, aggregate) -> aggregate.updateNewEvents(key, value),
                        Named.as("explore-aggregate-processor"),
                        Materialized
                                .<String, AlphabetWordAggregate, KeyValueStore<Bytes, byte[]>>as("explore-aggregate")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(SerdesFactory.alphabetWordAggregate())
                );

        kGroupedTable
                .toStream()
                .print(Printed.<String, AlphabetWordAggregate>toSysOut().withLabel("explore-aggregate"));
    }
}
