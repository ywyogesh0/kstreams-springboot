package com.learnkafkastreams.handler;

import com.learnkafkastreams.domain.*;
import com.learnkafkastreams.serdes.SerdeFactory;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;

public class AggOrdersRevenueByStore implements AggWindowedHandler<Order, Store> {
    @Override
    public void aggregate(
            KStream<String, Order> consumerKStream,
            String stateStore,
            KTable<String, Store> lookupTable
    ) {
        // initializer for TotalRevenue java bean
        Initializer<TotalRevenue> totalRevenueInitializer = TotalRevenue::new;

        // aggregator for TotalRevenue java bean
        Aggregator<String, Order, TotalRevenue> totalRevenueAggregator =
                (key, order, aggregate) -> aggregate.updateTotalRevenue(key, order);

        KTable<String, TotalRevenue> totalRevenueKTable = consumerKStream
                /*.groupBy(
                        (key, order) -> order.locationId(),
                        Grouped.with(Serdes.String(), SerdeFactory.generateOrderSerde())
                )*/
                .groupByKey(
                        Grouped.with(Serdes.String(), SerdeFactory.generateOrderSerde())
                )
                .aggregate(
                        totalRevenueInitializer,
                        totalRevenueAggregator,
                        Named.as(stateStore),
                        Materialized
                                .<String, TotalRevenue, KeyValueStore<Bytes, byte[]>>as(stateStore)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(SerdeFactory.generateTotalRevenueSerde())
                );

        totalRevenueKTable
                .toStream()
                .print(
                        Printed.<String, TotalRevenue>toSysOut().withLabel(stateStore)
                );

        // join between kTable-kTable
        ValueJoiner<TotalRevenue, Store, TotalRevenueWithAddress> valueJoiner = TotalRevenueWithAddress::new;

        KTable<String, TotalRevenueWithAddress> totalRevenueWithAddressKTable = totalRevenueKTable
                .join(
                        lookupTable,
                        valueJoiner,
                        Materialized
                                .<String, TotalRevenueWithAddress, KeyValueStore<Bytes, byte[]>>
                                        as("total-" + stateStore + "-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(SerdeFactory.generateTotalRevenueWithAddressSerde())
                );

        totalRevenueWithAddressKTable
                .toStream()
                .print(Printed.<String, TotalRevenueWithAddress>toSysOut().withLabel("total-" + stateStore + "-stream"));
    }
}
