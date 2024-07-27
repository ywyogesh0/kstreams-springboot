package com.learnkafkastreams.topology;

import com.learnkafkastreams.utils.Constant;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;

public class WordsKTableTopology {

    public static Topology buildTopology() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        // kTable
        streamsBuilder.table(
                        Constant.TOPIC_WORDS_CONSUMER,
                        Consumed.with(Serdes.String(), Serdes.String()),
                        Materialized.as("words-store")
                ).mapValues(
                        (readOnlyKey, value) -> value.toUpperCase()
                )
                .toStream()
                .to(
                        Constant.TOPIC_WORDS_PRODUCER,
                        Produced.with(Serdes.String(), Serdes.String())
                );
                //.print(Printed.<String, String>toSysOut().withLabel("words-ktable"));

        // globalKTable
/*        streamsBuilder.globalTable(
                        Constant.TOPIC_WORDS_CONSUMER,
                        Consumed.with(Serdes.String(), Serdes.String()),
                        Materialized.as("words-global-store")
                );*/

        return streamsBuilder.build();
    }
}
