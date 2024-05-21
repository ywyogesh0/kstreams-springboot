package com.learnkafkastreams.topology;

import com.learnkafkastreams.domain.Alphabet;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;

import static com.learnkafkastreams.utils.Constants.TOPIC_ALPHABETS;
import static com.learnkafkastreams.utils.Constants.TOPIC_ALPHABETS_ABBREVIATIONS;

/**
 * This Topology Only Supports - INNER & LEFT OUTER JOIN (Co-Partitioning Is Required)
 */
public class KStreamKTableJoinTopology {

    /**
     * Explore "inner" join between kStream-kTable
     *
     * @param streamsBuilder Provides the high-level Kafka Streams DSL to specify a Kafka Streams topology
     */
    public static void exploreJoinKStreamKTable(StreamsBuilder streamsBuilder) {
        // primary kStream - TOPIC_ALPHABETS_ABBREVIATIONS
        KStream<String, String> alphabetAbbreviationStream = streamsBuilder
                .stream(
                        TOPIC_ALPHABETS_ABBREVIATIONS,
                        Consumed.with(Serdes.String(), Serdes.String())
                );

        alphabetAbbreviationStream
                .print(Printed.<String, String>toSysOut().withLabel("alphabet-abbreviation-stream"));

        // secondary kTable - TOPIC_ALPHABETS
        KTable<String, String> alphabetKTable = streamsBuilder
                .table(
                        TOPIC_ALPHABETS,
                        Consumed.with(Serdes.String(), Serdes.String()),
                        Materialized
                                .<String, String, KeyValueStore<Bytes, byte[]>>as("alphabet-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.String())
                );

        alphabetKTable
                .toStream()
                .print(Printed.<String, String>toSysOut().withLabel("alphabet-ktable"));

        ValueJoiner<String, String, Alphabet> alphabetValueJoiner = Alphabet::new;

        Joined<String, String, String> joinedParam = Joined
                .<String, String, String>as("alphabet-abbreviation-join")
                .withKeySerde(Serdes.String())
                .withValueSerde(Serdes.String());

        KStream<String, Alphabet> joinedKStream = alphabetAbbreviationStream
                .join(
                        alphabetKTable,
                        alphabetValueJoiner,
                        joinedParam
                );

        joinedKStream
                .print(Printed.<String, Alphabet>toSysOut().withLabel("alphabet-abbreviation-inner-joined-stream"));
    }

    /**
     * Explore "left outer" join between kStream-kTable
     *
     * @param streamsBuilder Provides the high-level Kafka Streams DSL to specify a Kafka Streams topology
     */
    public static void exploreLeftJoinKStreamKTable(StreamsBuilder streamsBuilder) {
        // primary kStream - TOPIC_ALPHABETS_ABBREVIATIONS
        KStream<String, String> alphabetAbbreviationStream = streamsBuilder
                .stream(
                        TOPIC_ALPHABETS_ABBREVIATIONS,
                        Consumed.with(Serdes.String(), Serdes.String())
                );

        alphabetAbbreviationStream
                .print(Printed.<String, String>toSysOut().withLabel("alphabet-abbreviation-stream"));

        // secondary kTable - TOPIC_ALPHABETS
        KTable<String, String> alphabetKTable = streamsBuilder
                .table(
                        TOPIC_ALPHABETS,
                        Consumed.with(Serdes.String(), Serdes.String()),
                        Materialized
                                .<String, String, KeyValueStore<Bytes, byte[]>>as("alphabet-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.String())
                );

        alphabetKTable
                .toStream()
                .print(Printed.<String, String>toSysOut().withLabel("alphabet-ktable"));

        ValueJoiner<String, String, Alphabet> alphabetValueJoiner = Alphabet::new;

        Joined<String, String, String> joinedParam = Joined
                .<String, String, String>as("alphabet-abbreviation-join")
                .withKeySerde(Serdes.String())
                .withValueSerde(Serdes.String());

        KStream<String, Alphabet> joinedKStream = alphabetAbbreviationStream
                .leftJoin(
                        alphabetKTable,
                        alphabetValueJoiner,
                        joinedParam
                );

        joinedKStream
                .print(Printed.<String, Alphabet>toSysOut().withLabel("alphabet-abbreviation-left-joined-stream"));
    }
}
