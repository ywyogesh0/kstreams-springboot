package com.learnkafkastreams.topology;

import com.learnkafkastreams.domain.Alphabet;
import com.learnkafkastreams.serdes.SerdesFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;

import java.time.Duration;

import static com.learnkafkastreams.utils.Constants.TOPIC_ALPHABETS;
import static com.learnkafkastreams.utils.Constants.TOPIC_ALPHABETS_ABBREVIATIONS;

/**
 * This Topology Supports - INNER, LEFT OUTER & FULL OUTER JOIN (Co-Partitioning Is Required)
 */
@Slf4j
public class KStreamKStreamJoinTopology {

    /**
     * Explore "inner" join between kStream-kStream
     *
     * @param streamsBuilder Provides the high-level Kafka Streams DSL to specify a Kafka Streams topology
     */
    public static void exploreJoinKStreamKStream(StreamsBuilder streamsBuilder) {
        // primary kStream - TOPIC_ALPHABETS_ABBREVIATIONS
        KStream<String, String> alphabetAbbreviationKStream = streamsBuilder
                .stream(
                        TOPIC_ALPHABETS_ABBREVIATIONS,
                        Consumed.with(Serdes.String(), Serdes.String())
                );

        alphabetAbbreviationKStream
                .print(Printed.<String, String>toSysOut().withLabel("alphabet-abbreviation-stream"));

        // secondary kStream - TOPIC_ALPHABETS
        KStream<String, String> alphabetKStream = streamsBuilder
                .stream(
                        TOPIC_ALPHABETS,
                        Consumed.with(Serdes.String(), Serdes.String())
                );

        alphabetKStream
                .print(Printed.<String, String>toSysOut().withLabel("alphabet-stream"));

        ValueJoiner<String, String, Alphabet> alphabetValueJoiner = Alphabet::new;

        JoinWindows joinWindows = JoinWindows
                .ofTimeDifferenceWithNoGrace(Duration.ofSeconds(5));

        StreamJoined<String, String, String> streamJoined = StreamJoined
                .<String, String, String>as("alphabet-abbreviation-store")
                .withKeySerde(Serdes.String())
                .withValueSerde(Serdes.String());

        KStream<String, Alphabet> joinedKStream = alphabetAbbreviationKStream
                .join(
                        alphabetKStream,
                        alphabetValueJoiner,
                        joinWindows,
                        streamJoined
                );

        joinedKStream
                .print(Printed.<String, Alphabet>toSysOut().withLabel("alphabet-abbreviation-inner-joined-stream"));
    }

    /**
     * Explore "left outer" join between kStream-kStream
     *
     * @param streamsBuilder Provides the high-level Kafka Streams DSL to specify a Kafka Streams topology
     */
    public static void exploreLeftJoinKStreamKStream(StreamsBuilder streamsBuilder) {
        // primary kStream - TOPIC_ALPHABETS_ABBREVIATIONS
        KStream<String, String> alphabetAbbreviationKStream = streamsBuilder
                .stream(
                        TOPIC_ALPHABETS_ABBREVIATIONS,
                        Consumed.with(Serdes.String(), Serdes.String())
                );

        alphabetAbbreviationKStream
                .print(Printed.<String, String>toSysOut().withLabel("alphabet-abbreviation-stream"));

        // secondary kStream - TOPIC_ALPHABETS
        KStream<String, String> alphabetKStream = streamsBuilder
                .stream(
                        TOPIC_ALPHABETS,
                        Consumed.with(Serdes.String(), Serdes.String())
                );

        alphabetKStream
                .print(Printed.<String, String>toSysOut().withLabel("alphabet-stream"));

        ValueJoiner<String, String, Alphabet> alphabetValueJoiner = Alphabet::new;

        JoinWindows joinWindows = JoinWindows
                .ofTimeDifferenceWithNoGrace(Duration.ofSeconds(5));

        StreamJoined<String, String, String> streamJoined = StreamJoined
                .<String, String, String>as("alphabet-abbreviation-store")
                .withKeySerde(Serdes.String())
                .withValueSerde(Serdes.String());

        KStream<String, Alphabet> joinedKStream = alphabetAbbreviationKStream
                .leftJoin(
                        alphabetKStream,
                        alphabetValueJoiner,
                        joinWindows,
                        streamJoined
                );

        joinedKStream
                .print(Printed.<String, Alphabet>toSysOut().withLabel("alphabet-abbreviation-left-joined-stream"));
    }

    /**
     * Explore "full outer" join between kStream-kStream
     *
     * @param streamsBuilder Provides the high-level Kafka Streams DSL to specify a Kafka Streams topology
     */
    public static void exploreOuterJoinKStreamKStream(StreamsBuilder streamsBuilder) {
        // primary kStream - TOPIC_ALPHABETS_ABBREVIATIONS
        KStream<String, String> alphabetAbbreviationKStream = streamsBuilder
                .stream(
                        TOPIC_ALPHABETS_ABBREVIATIONS,
                        Consumed.with(Serdes.String(), Serdes.String())
                );

        alphabetAbbreviationKStream
                .print(Printed.<String, String>toSysOut().withLabel("alphabet-abbreviation-stream"));

        // secondary kStream - TOPIC_ALPHABETS
        KStream<String, String> alphabetKStream = streamsBuilder
                .stream(
                        TOPIC_ALPHABETS,
                        Consumed.with(Serdes.String(), Serdes.String())
                );

        alphabetKStream
                .print(Printed.<String, String>toSysOut().withLabel("alphabet-stream"));

        ValueJoiner<String, String, Alphabet> alphabetValueJoiner = Alphabet::new;

        JoinWindows joinWindows = JoinWindows
                .ofTimeDifferenceWithNoGrace(Duration.ofSeconds(5));

        StreamJoined<String, String, String> streamJoined = StreamJoined
                .<String, String, String>as("alphabet-abbreviation-store")
                .withKeySerde(Serdes.String())
                .withValueSerde(Serdes.String());

        KStream<String, Alphabet> joinedKStream = alphabetAbbreviationKStream
                .outerJoin(
                        alphabetKStream,
                        alphabetValueJoiner,
                        joinWindows,
                        streamJoined
                );

        joinedKStream
                .print(Printed.<String, Alphabet>toSysOut().withLabel("alphabet-abbreviation-outer-joined-stream"));
    }
}
