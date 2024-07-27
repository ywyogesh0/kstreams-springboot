package com.learnkafkastreams.util;

import com.learnkafkastreams.domain.Order;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

import java.time.ZoneOffset;

@Slf4j
public class OrderTimestampExtractor implements TimestampExtractor {
    @Override
    public long extract(ConsumerRecord<Object, Object> consumerRecord, long partitionTime) {
        var orderRecord = (Order) consumerRecord.value();
        if (orderRecord != null && orderRecord.orderedDateTime() != null) {
            var timeStampInBST = orderRecord.orderedDateTime();
            log.info("timeStampInBST : {} ", timeStampInBST);
            var instant = timeStampInBST.toInstant(ZoneOffset.ofHours(1));
            log.info("timeStampInUTC : {} ", instant);
            return instant.toEpochMilli();
        }
        //fallback to stream time
        return partitionTime;
    }
}
