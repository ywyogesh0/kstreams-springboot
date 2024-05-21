package com.learnkafkastreams.topology;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;

@Slf4j
public class ExploreJoinsOperatorsTopology {

    public static Topology build() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        // explore "inner" & "left outer" join between kStream-kTable
        // KStreamKTableJoinTopology.exploreJoinKStreamKTable(streamsBuilder);
        // KStreamKTableJoinTopology.exploreLeftJoinKStreamKTable(streamsBuilder);

        // explore "inner" & "left outer" join between kStream-globalKTable
        // KStreamGlobalKTableJoinTopology.exploreJoinKStreamGlobalKTable(streamsBuilder);
        // KStreamGlobalKTableJoinTopology.exploreLeftJoinKStreamGlobalKTable(streamsBuilder);

        // explore "inner", "left outer" & "full outer" join between kTable-kTable
        // KTableKTableJoinTopology.exploreJoinKTableKTable(streamsBuilder);
        // KTableKTableJoinTopology.exploreLeftJoinKTableKTable(streamsBuilder);
        // KTableKTableJoinTopology.exploreOuterJoinKTableKTable(streamsBuilder);

        // explore "inner", "left outer" & "full outer" join between kStream-kStream
        // KStreamKStreamJoinTopology.exploreJoinKStreamKStream(streamsBuilder);
        // KStreamKStreamJoinTopology.exploreLeftJoinKStreamKStream(streamsBuilder);
        // KStreamKStreamJoinTopology.exploreOuterJoinKStreamKStream(streamsBuilder);

        return streamsBuilder.build();
    }
}
