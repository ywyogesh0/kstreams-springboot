package com.learnkafkastreams.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.Windowed;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Slf4j
public class OrderHelperUtility {

    public static void displayWindowedTableTimestamp(Windowed<String> key, Object value) {
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

    public static LocalDateTime convertInstantToLocalTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.of("Europe/London"));
    }

    public static Instant convertLocalToInstantTime(LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZoneOffset.UTC);
    }
}
