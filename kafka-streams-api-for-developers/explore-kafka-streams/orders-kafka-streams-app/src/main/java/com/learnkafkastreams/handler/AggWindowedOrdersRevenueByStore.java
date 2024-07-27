package com.learnkafkastreams.handler;

import com.learnkafkastreams.domain.Order;
import com.learnkafkastreams.domain.Store;
import com.learnkafkastreams.domain.TotalRevenue;
import com.learnkafkastreams.domain.TotalRevenueWithAddress;
import com.learnkafkastreams.serdes.SerdeFactory;
import com.learnkafkastreams.util.OrderHelperUtility;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;

import java.time.Duration;

public class AggWindowedOrdersRevenueByStore implements AggWindowedHandler<Order, Store> {
    @Override
    public void aggregate(
            KStream<String, Order> consumerKStream,
            String stateStore,
            KTable<String, Store> lookupTable
    ) {
        // tumbling windows
        Duration windowedSize = Duration.ofSeconds(3);
        TimeWindows timeWindows = TimeWindows.ofSizeWithNoGrace(windowedSize);

        // hopping windows
        /*Duration windowedSize = Duration.ofSeconds(3);
        TimeWindows timeWindows = TimeWindows
                .ofSizeWithNoGrace(windowedSize)
                .advanceBy(Duration.ofSeconds(1));*/

        /*SlidingWindows timeWindows = SlidingWindows
                .ofTimeDifferenceWithNoGrace(Duration.ofSeconds(3));*/

        // initializer for TotalRevenue java bean
        Initializer<TotalRevenue> totalRevenueInitializer = TotalRevenue::new;

        // aggregator for TotalRevenue java bean
        Aggregator<String, Order, TotalRevenue> totalRevenueAggregator =
                (key, order, aggregate) -> aggregate.updateTotalRevenue(key, order);

        KTable<Windowed<String>, TotalRevenue> totalRevenueKTable = consumerKStream
                /*.groupBy(
                        (key, order) -> order.locationId(),
                        Grouped.with(Serdes.String(), SerdeFactory.generateOrderSerde())
                )*/
                .groupByKey(
                        Grouped.with(Serdes.String(), SerdeFactory.generateOrderSerde())
                )
                .windowedBy(timeWindows)
                .aggregate(
                        totalRevenueInitializer,
                        totalRevenueAggregator,
                        Named.as(stateStore),
                        Materialized
                                .<String, TotalRevenue, WindowStore<Bytes, byte[]>>as(stateStore)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(SerdeFactory.generateTotalRevenueSerde())
                );

        totalRevenueKTable
                .toStream()
                .peek(OrderHelperUtility::displayWindowedTableTimestamp)
                .print(
                        Printed.<Windowed<String>, TotalRevenue>toSysOut().withLabel(stateStore)
                );

        // join between kStream-kTable
        ValueJoiner<TotalRevenue, Store, TotalRevenueWithAddress> valueJoiner = TotalRevenueWithAddress::new;

        Joined<String, TotalRevenue, Store> joinedParam = Joined
                .with(
                        Serdes.String(),
                        SerdeFactory.generateTotalRevenueSerde(),
                        SerdeFactory.generateStoreSerde()
                );

        KStream<String, TotalRevenueWithAddress> totalRevenueWithAddressKStream = totalRevenueKTable
                .toStream()
                .map((stringWindowed, TotalRevenue) -> KeyValue.pair(stringWindowed.key(), TotalRevenue))
                .join(
                        lookupTable,
                        valueJoiner,
                        joinedParam
                );

        totalRevenueWithAddressKStream
                .print(Printed.<String, TotalRevenueWithAddress>toSysOut().withLabel("total-" + stateStore + "-stream"));
    }
}
