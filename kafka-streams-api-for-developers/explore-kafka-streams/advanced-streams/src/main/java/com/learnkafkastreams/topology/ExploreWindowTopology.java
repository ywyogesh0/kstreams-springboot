package com.learnkafkastreams.topology;


import com.learnkafkastreams.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;

import java.time.*;

@Slf4j
public class ExploreWindowTopology {

    public static Topology build() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        /*tumbling window :
          - fixed interval
          - no overlapping
          - window start/end timestamp is based on clock timestamp of app running machine
          - start second included
          - end second not included*/
        //triggerTumblingWindow(streamsBuilder);

        /*hopping window :
          - fixed interval
          - overlapping
          - window start/end timestamp is based on clock timestamp of app running machine
          - start second included
          - end second not included*/
        //triggerHoppingWindow(streamsBuilder);

        /*sliding window :
          - fixed interval
          - overlapping
          - window start/end timestamp is based on consumer record timestamp via timestamp extractor
          - start second included
          - end second included*/
        triggerSlidingWindow(streamsBuilder);

        return streamsBuilder.build();
    }

    private static void triggerSlidingWindow(StreamsBuilder streamsBuilder) {
        Duration windowDuration = Duration.ofSeconds(3);
        SlidingWindows slidingWindow = SlidingWindows
                .ofTimeDifferenceWithNoGrace(windowDuration);

        KTable<Windowed<String>, Long> slidingWindowedKTable = streamsBuilder
                .stream(
                        Constants.TOPIC_WINDOW_WORDS,
                        Consumed.with(
                                Serdes.String(),
                                Serdes.String()
                        )
                )
                .groupByKey()
                .windowedBy(slidingWindow)
                .count()
                .suppress(
                        Suppressed
                                .untilWindowCloses(
                                        Suppressed
                                                .BufferConfig
                                                .unbounded()
                                                .shutDownWhenFull()
                                )
                );

        // display windowed KTable as KStream
        slidingWindowedKTable
                .toStream()
                .peek(ExploreWindowTopology::displayWindowedTableTimestamp);
    }

    private static void triggerHoppingWindow(StreamsBuilder streamsBuilder) {
        Duration windowDuration = Duration.ofSeconds(3);
        Duration advanceWindowDuration = Duration.ofSeconds(1);
        TimeWindows hoppingWindow = TimeWindows
                .ofSizeWithNoGrace(windowDuration)
                .advanceBy(advanceWindowDuration);

        KTable<Windowed<String>, Long> hoppingWindowedKTable = streamsBuilder
                .stream(
                        Constants.TOPIC_WINDOW_WORDS,
                        Consumed.with(
                                Serdes.String(),
                                Serdes.String()
                        )
                )
                .groupByKey()
                .windowedBy(hoppingWindow)
                .count()
                .suppress(
                        Suppressed
                                .untilWindowCloses(
                                        Suppressed
                                                .BufferConfig
                                                .unbounded()
                                                .shutDownWhenFull()
                                )
                );

        // display windowed KTable as KStream
        hoppingWindowedKTable
                .toStream()
                .peek(ExploreWindowTopology::displayWindowedTableTimestamp);
    }

    private static void triggerTumblingWindow(StreamsBuilder streamsBuilder) {
        Duration windowDuration = Duration.ofSeconds(3);
        TimeWindows tumblingWindow = TimeWindows.ofSizeWithNoGrace(windowDuration);

        KTable<Windowed<String>, Long> tumblingWindowedKTable = streamsBuilder
                .stream(
                        Constants.TOPIC_WINDOW_WORDS,
                        Consumed.with(
                                Serdes.String(),
                                Serdes.String()
                        )
                )
                .groupByKey()
                .windowedBy(tumblingWindow)
                .count()
                .suppress(
                        Suppressed
                                .untilWindowCloses(
                                        Suppressed
                                                .BufferConfig
                                                .unbounded()
                                                .shutDownWhenFull()
                                )
                );

        // display windowed KTable as KStream
        tumblingWindowedKTable
                .toStream()
                .peek(ExploreWindowTopology::displayWindowedTableTimestamp);
    }

    private static void displayWindowedTableTimestamp(Windowed<String> key, Long value) {
        log.info("key: {}, value: {}", key.key(), value);

        Instant windowedKeyStartTime = key.window().startTime();
        Instant windowedKeyEndTime = key.window().endTime();

        log.info("windowed key start time in GMT: {}", windowedKeyStartTime);
        log.info("windowed key end time in GMT: {}", windowedKeyEndTime);

        LocalDateTime windowedKeyLocalStartTime =
                LocalDateTime.ofInstant(windowedKeyStartTime, ZoneId.of("Europe/London"));

        LocalDateTime windowedKeyLocalEndTime =
                LocalDateTime.ofInstant(windowedKeyEndTime, ZoneId.of("Europe/London"));

        log.info("windowed key start time in BST: {}", windowedKeyLocalStartTime);
        log.info("windowed key end time in BST: {}", windowedKeyLocalEndTime);
    }
}
