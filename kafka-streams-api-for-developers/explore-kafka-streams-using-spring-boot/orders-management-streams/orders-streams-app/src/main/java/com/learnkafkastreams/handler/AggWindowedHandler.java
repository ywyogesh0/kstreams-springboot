package com.learnkafkastreams.handler;

import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;

public interface AggWindowedHandler<C, L> {

    void aggregate(KStream<String, C> consumerKStream,
                   String stateStore,
                   KTable<String, L> lookupTable);
}
