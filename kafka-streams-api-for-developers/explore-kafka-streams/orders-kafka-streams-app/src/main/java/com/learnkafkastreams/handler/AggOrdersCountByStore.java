package com.learnkafkastreams.handler;

import com.learnkafkastreams.domain.Order;
import com.learnkafkastreams.domain.Store;
import com.learnkafkastreams.domain.TotalCountWithAddress;
import com.learnkafkastreams.serdes.SerdeFactory;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;

public class AggOrdersCountByStore implements AggWindowedHandler<Order, Store> {
    @Override
    public void aggregate(
            KStream<String, Order> consumerKStream,
            String stateStore,
            KTable<String, Store> lookupTable
    ) {
        KTable<String, Long> countKTable = consumerKStream
                //.map((key, order) -> KeyValue.pair(order.locationId(), order))
                .groupByKey(
                        Grouped.with(Serdes.String(), SerdeFactory.generateOrderSerde())
                )
                .count(
                        Named.as(stateStore),
                        Materialized
                                .<String, Long, KeyValueStore<Bytes, byte[]>>as(stateStore)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Long())
                );

        countKTable
                .toStream()
                .print(
                        Printed.<String, Long>toSysOut().withLabel(stateStore)
                );

        // join between kTable-kTable
        ValueJoiner<Long, Store, TotalCountWithAddress> valueJoiner = TotalCountWithAddress::new;

        KTable<String, TotalCountWithAddress> totalCountWithAddressKTable = countKTable
                .join(
                        lookupTable,
                        valueJoiner,
                        Materialized
                                .<String, TotalCountWithAddress, KeyValueStore<Bytes, byte[]>>
                                        as("total-" + stateStore + "-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(SerdeFactory.generateTotalCountWithAddressSerde())
                );

        totalCountWithAddressKTable
                .toStream()
                .print(
                        Printed.<String, TotalCountWithAddress>toSysOut().withLabel("total-" + stateStore + "-stream")
                );
    }
}
