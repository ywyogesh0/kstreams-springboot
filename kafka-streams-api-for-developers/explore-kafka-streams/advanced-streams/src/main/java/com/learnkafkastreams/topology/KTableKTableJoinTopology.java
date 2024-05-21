package com.learnkafkastreams.topology;

import com.learnkafkastreams.domain.Alphabet;
import com.learnkafkastreams.serdes.SerdesFactory;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;

import static com.learnkafkastreams.utils.Constants.TOPIC_ALPHABETS;
import static com.learnkafkastreams.utils.Constants.TOPIC_ALPHABETS_ABBREVIATIONS;

/**
 * This Topology Supports - INNER, LEFT OUTER & FULL OUTER JOIN (Co-Partitioning Is Required)
 */
public class KTableKTableJoinTopology {

    /**
     * Explore "inner" join between kTable-kTable
     *
     * @param streamsBuilder Provides the high-level Kafka Streams DSL to specify a Kafka Streams topology
     */
    public static void exploreJoinKTableKTable(StreamsBuilder streamsBuilder) {
        // primary kTable - TOPIC_ALPHABETS_ABBREVIATIONS
        KTable<String, String> alphabetAbbreviationKTable = streamsBuilder
                .table(
                        TOPIC_ALPHABETS_ABBREVIATIONS,
                        Consumed.with(Serdes.String(), Serdes.String()),
                        Materialized
                                .<String, String, KeyValueStore<Bytes, byte[]>>as("alphabet-abbreviation-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.String())
                );

        alphabetAbbreviationKTable
                .toStream()
                .print(Printed.<String, String>toSysOut().withLabel("alphabet-abbreviation-ktable"));

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

        KTable<String, Alphabet> joinedKTable = alphabetAbbreviationKTable
                .join(
                        alphabetKTable,
                        alphabetValueJoiner,
                        Materialized
                                .<String, Alphabet, KeyValueStore<Bytes, byte[]>>as("joined-alphabet-abbreviation-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(SerdesFactory.alphabet())
                );

        joinedKTable
                .toStream()
                .print(Printed.<String, Alphabet>toSysOut().withLabel("alphabet-abbreviation-inner-joined-ktable"));
    }

    /**
     * Explore "left outer" join between kTable-kTable
     *
     * @param streamsBuilder Provides the high-level Kafka Streams DSL to specify a Kafka Streams topology
     */
    public static void exploreLeftJoinKTableKTable(StreamsBuilder streamsBuilder) {
        // primary kTable - TOPIC_ALPHABETS_ABBREVIATIONS
        KTable<String, String> alphabetAbbreviationKTable = streamsBuilder
                .table(
                        TOPIC_ALPHABETS_ABBREVIATIONS,
                        Consumed.with(Serdes.String(), Serdes.String()),
                        Materialized
                                .<String, String, KeyValueStore<Bytes, byte[]>>as("alphabet-abbreviation-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.String())
                );

        alphabetAbbreviationKTable
                .toStream()
                .print(Printed.<String, String>toSysOut().withLabel("alphabet-abbreviation-ktable"));

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

        KTable<String, Alphabet> joinedKTable = alphabetAbbreviationKTable
                .leftJoin(
                        alphabetKTable,
                        alphabetValueJoiner,
                        Materialized
                                .<String, Alphabet, KeyValueStore<Bytes, byte[]>>as("joined-alphabet-abbreviation-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(SerdesFactory.alphabet())
                );

        joinedKTable
                .toStream()
                .print(Printed.<String, Alphabet>toSysOut().withLabel("alphabet-abbreviation-left-joined-ktable"));
    }

    /**
     * Explore "full outer" join between kTable-kTable
     *
     * @param streamsBuilder Provides the high-level Kafka Streams DSL to specify a Kafka Streams topology
     */
    public static void exploreOuterJoinKTableKTable(StreamsBuilder streamsBuilder) {
        // primary kTable - TOPIC_ALPHABETS_ABBREVIATIONS
        KTable<String, String> alphabetAbbreviationKTable = streamsBuilder
                .table(
                        TOPIC_ALPHABETS_ABBREVIATIONS,
                        Consumed.with(Serdes.String(), Serdes.String()),
                        Materialized
                                .<String, String, KeyValueStore<Bytes, byte[]>>as("alphabet-abbreviation-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.String())
                );

        alphabetAbbreviationKTable
                .toStream()
                .print(Printed.<String, String>toSysOut().withLabel("alphabet-abbreviation-ktable"));

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

        KTable<String, Alphabet> joinedKTable = alphabetAbbreviationKTable
                .outerJoin(
                        alphabetKTable,
                        alphabetValueJoiner,
                        Materialized
                                .<String, Alphabet, KeyValueStore<Bytes, byte[]>>as("joined-alphabet-abbreviation-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(SerdesFactory.alphabet())
                );

        joinedKTable
                .toStream()
                .print(Printed.<String, Alphabet>toSysOut().withLabel("alphabet-abbreviation-outer-joined-ktable"));
    }
}
