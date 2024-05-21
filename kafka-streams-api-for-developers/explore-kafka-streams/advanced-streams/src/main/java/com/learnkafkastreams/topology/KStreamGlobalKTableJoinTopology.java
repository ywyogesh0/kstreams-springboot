package com.learnkafkastreams.topology;

import com.learnkafkastreams.domain.Alphabet;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;

import static com.learnkafkastreams.utils.Constants.TOPIC_ALPHABETS;
import static com.learnkafkastreams.utils.Constants.TOPIC_ALPHABETS_ABBREVIATIONS;

/**
 * This Topology Only Supports - INNER & LEFT OUTER JOIN (Co-Partitioning Is NOT Required)
 */
@Slf4j
public class KStreamGlobalKTableJoinTopology {

    /**
     * Explore "inner" join between kStream-globalKTable
     *
     * @param streamsBuilder Provides the high-level Kafka Streams DSL to specify a Kafka Streams topology
     */
    public static void exploreJoinKStreamGlobalKTable(StreamsBuilder streamsBuilder) {
        // primary kStream - TOPIC_ALPHABETS_ABBREVIATIONS
        KStream<String, String> alphabetAbbreviationStream = streamsBuilder
                .stream(
                        TOPIC_ALPHABETS_ABBREVIATIONS,
                        Consumed.with(Serdes.String(), Serdes.String())
                );

        alphabetAbbreviationStream
                .print(Printed.<String, String>toSysOut().withLabel("alphabet-abbreviation-stream"));

        // secondary globalKTable - TOPIC_ALPHABETS
        GlobalKTable<String, String> alphabetGlobalKTable = streamsBuilder
                .globalTable(
                        TOPIC_ALPHABETS,
                        Consumed.with(Serdes.String(), Serdes.String()),
                        Materialized
                                .<String, String, KeyValueStore<Bytes, byte[]>>as("alphabet-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.String())
                );

        KeyValueMapper<String, String, String> alphabetKeyValueMapper = (key, value) -> key;

        ValueJoiner<String, String, Alphabet> alphabetValueJoiner = Alphabet::new;

        KStream<String, Alphabet> joinedKStream = alphabetAbbreviationStream
                .join(
                        alphabetGlobalKTable,
                        alphabetKeyValueMapper,
                        alphabetValueJoiner
                );

        joinedKStream
                .print(Printed.<String, Alphabet>toSysOut().withLabel("alphabet-abbreviation-inner-joined-stream"));
    }

    /**
     * Explore "left outer" join between kStream-globalKTable
     *
     * @param streamsBuilder Provides the high-level Kafka Streams DSL to specify a Kafka Streams topology
     */
    public static void exploreLeftJoinKStreamGlobalKTable(StreamsBuilder streamsBuilder) {
        // primary kStream - TOPIC_ALPHABETS_ABBREVIATIONS
        KStream<String, String> alphabetAbbreviationStream = streamsBuilder
                .stream(
                        TOPIC_ALPHABETS_ABBREVIATIONS,
                        Consumed.with(Serdes.String(), Serdes.String())
                );

        alphabetAbbreviationStream
                .print(Printed.<String, String>toSysOut().withLabel("alphabet-abbreviation-stream"));

        // secondary globalKTable - TOPIC_ALPHABETS
        GlobalKTable<String, String> alphabetGlobalKTable = streamsBuilder
                .globalTable(
                        TOPIC_ALPHABETS,
                        Consumed.with(Serdes.String(), Serdes.String()),
                        Materialized
                                .<String, String, KeyValueStore<Bytes, byte[]>>as("alphabet-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.String())
                );

        KeyValueMapper<String, String, String> alphabetKeyValueMapper = (key, value) -> key;

        ValueJoiner<String, String, Alphabet> alphabetValueJoiner = Alphabet::new;

        KStream<String, Alphabet> joinedKStream = alphabetAbbreviationStream
                .leftJoin(
                        alphabetGlobalKTable,
                        alphabetKeyValueMapper,
                        alphabetValueJoiner
                );

        joinedKStream
                .print(Printed.<String, Alphabet>toSysOut().withLabel("alphabet-abbreviation-left-joined-stream"));
    }
}
