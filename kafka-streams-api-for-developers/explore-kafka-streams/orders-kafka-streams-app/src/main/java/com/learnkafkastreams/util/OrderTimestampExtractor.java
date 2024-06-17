package com.learnkafkastreams.util;

import com.learnkafkastreams.domain.Order;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
public class OrderTimestampExtractor implements TimestampExtractor {
    @Override
    public long extract(ConsumerRecord<Object, Object> consumerRecord, long l) {
        if(null != consumerRecord) {
            Order order = (Order) consumerRecord.value();
            LocalDateTime payloadTimestampInBST = order.orderedDateTime();
            return payloadTimestampInBST
                    .minusHours(1)
                    .toEpochSecond(ZoneOffset.UTC);
        }
        return l;
    }
}
