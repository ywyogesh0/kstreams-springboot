package com.learnkafkastreams.handler;

import com.learnkafkastreams.domain.Order;
import com.learnkafkastreams.domain.Store;
import com.learnkafkastreams.domain.TotalCountWithAddress;
import com.learnkafkastreams.serdes.SerdeFactory;
import com.learnkafkastreams.util.OrderHelperUtility;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;

import java.time.Duration;

public class AggWindowedOrdersCountByStore implements AggWindowedHandler<Order, Store> {
    @Override
    public void aggregate(
            KStream<String, Order> consumerKStream,
            String stateStore,
            KTable<String, Store> lookupTable
    ) {
        Duration windowedSize = Duration.ofSeconds(3);
        TimeWindows tumblingAggWindow = TimeWindows.ofSizeWithNoGrace(windowedSize);

        KTable<Windowed<String>, Long> countKTable = consumerKStream
                //.map((key, order) -> KeyValue.pair(order.locationId(), order))
                .groupByKey(
                        Grouped.with(Serdes.String(), SerdeFactory.generateOrderSerde())
                )
                .windowedBy(tumblingAggWindow)
                .count(
                        Named.as(stateStore),
                        Materialized
                                .<String, Long, WindowStore<Bytes, byte[]>>as(stateStore)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Long())
                )
                .suppress(
                        Suppressed
                                .untilWindowCloses(
                                        Suppressed
                                                .BufferConfig
                                                .unbounded()
                                                .shutDownWhenFull()
                                )
                );

        countKTable
                .toStream()
                .peek(OrderHelperUtility::displayWindowedTableTimestamp)
                .print(
                        Printed.<Windowed<String>, Long>toSysOut().withLabel(stateStore)
                );

        // join between kStream-kTable
        ValueJoiner<Long, Store, TotalCountWithAddress> valueJoiner = TotalCountWithAddress::new;

        Joined<String, Long, Store> joinedParam = Joined
                .<String, Long, Store>as("orders-count-join")
                .withKeySerde(Serdes.String())
                .withValueSerde(Serdes.Long())
                .withOtherValueSerde(SerdeFactory.generateStoreSerde());

        KStream<String, TotalCountWithAddress> totalCountWithAddressKStream = countKTable
                .toStream()
                .map((stringWindowed, aLong) -> KeyValue.pair(stringWindowed.key(), aLong))
                .join(
                        lookupTable,
                        valueJoiner,
                        joinedParam
                );

        totalCountWithAddressKStream
                .print(
                        Printed.<String, TotalCountWithAddress>toSysOut().withLabel("total-" + stateStore + "-stream")
                );
    }
}
