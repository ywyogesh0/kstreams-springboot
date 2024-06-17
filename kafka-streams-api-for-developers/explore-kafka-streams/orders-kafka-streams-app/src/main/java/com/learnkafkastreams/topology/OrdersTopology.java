package com.learnkafkastreams.topology;

import com.learnkafkastreams.domain.*;
import com.learnkafkastreams.serdes.SerdeFactory;
import com.learnkafkastreams.util.Constants;
import com.learnkafkastreams.util.OrderTimestampExtractor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.WindowStore;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
public class OrdersTopology {

    public static Topology buildTopology() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        // general order - predicate
        Predicate<String, Order> generalPredicate = ((orderKey, orderValue) ->
                OrderType.GENERAL.equals(orderValue.orderType()));

        // restaurant order - predicate
        Predicate<String, Order> restaurantPredicate = ((orderKey, orderValue) ->
                OrderType.RESTAURANT.equals(orderValue.orderType()));

        // transform order to revenue domain stream
        /*ValueMapper<Order, Revenue> revenueValueMapper = (order) -> new Revenue(
                order.locationId(),
                order.finalAmount()
        );*/

        // source processor - stream from orders topic
        KStream<String, Order> ordersStream = streamsBuilder
                .stream(
                        Constants.ORDERS_TOPIC,
                        Consumed
                                .with(
                                        Serdes.String(),
                                        SerdeFactory.generateOrderSerde()
                                )
                                .withTimestampExtractor(new OrderTimestampExtractor())
                );
                /*.selectKey(
                        (key, order) -> order.locationId()
                );*/

        // print - orders stream
        /*ordersStream.print(
                Printed.<String, Order>toSysOut().withLabel("orders-stream")
        );*/

        // store details - kTable
        KTable<String, Store> storeKTable = streamsBuilder
                .table(
                        Constants.STORES_TOPIC,
                        Consumed.with(Serdes.String(), SerdeFactory.generateStoreSerde()),
                        Materialized
                                .<String, Store, KeyValueStore<Bytes, byte[]>>as("store-details-ktable")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(SerdeFactory.generateStoreSerde())
                );

        // print - store kTable
        /*storeKTable
                .toStream()
                .print(Printed.<String, Store>toSysOut().withLabel("store-details-stream"));*/

        // split orders stream into - general & restaurant stream branches
        ordersStream.split(Named.as("split-orders-stream-processor"))
                .branch(
                        generalPredicate,
                        Branched.withConsumer(generalOrderKStream -> {
                            /*KStream<String, Revenue> revenueGeneralKStream = generalOrderKStream
                                    .mapValues(
                                            (readOnlyOrderKey, orderValue) -> revenueValueMapper.apply(orderValue)
                                    );
                            revenueGeneralKStream.print(
                                    Printed.<String, Revenue>toSysOut().withLabel("general-branched-stream")
                            );

                            revenueGeneralKStream.to(
                                    Constants.GENERAL_ORDERS_TOPIC,
                                    Produced.with(Serdes.String(), SerdeFactory.generateRevenueSerde())
                            );*/

                            /*aggregateOrdersCountByStore(
                                    generalOrderKStream,
                                    Constants.GENERAL_ORDERS_COUNT,
                                    storeKTable
                            );*/

                            aggregateWindowedOrdersCountByStore(
                                    generalOrderKStream,
                                    Constants.GENERAL_WINDOWED_ORDERS_COUNT,
                                    storeKTable
                            );

                            /*aggregateOrdersRevenueByStore(
                            generalOrderKStream,
                                    Constants.GENERAL_ORDERS_REVENUE,
                                    storeKTable
                            );*/
                        })
                )
                .branch(
                        restaurantPredicate,
                        Branched.withConsumer(restaurantOrderKStream -> {
                            /*KStream<String, Revenue> revenueRestaurantKStream = restaurantOrderKStream
                                    .mapValues(
                                            (readOnlyOrderKey, orderValue) -> revenueValueMapper.apply(orderValue)
                                    );
                            revenueRestaurantKStream.print(
                                    Printed.<String, Revenue>toSysOut().withLabel("restaurant-branched-stream")
                            );
                            revenueRestaurantKStream.to(
                                    Constants.RESTAURANT_ORDERS_TOPIC,
                                    Produced.with(Serdes.String(), SerdeFactory.generateRevenueSerde())
                            );*/

                            /*aggregateOrdersCountByStore(
                                    restaurantOrderKStream,
                                    Constants.RESTAURANT_ORDERS_COUNT,
                                    storeKTable
                            );*/

                            aggregateWindowedOrdersCountByStore(
                                    restaurantOrderKStream,
                                    Constants.RESTAURANT_WINDOWED_ORDERS_COUNT,
                                    storeKTable
                            );

                            /*aggregateOrdersRevenueByStore(
                                    restaurantOrderKStream,
                                    Constants.RESTAURANT_ORDERS_REVENUE,
                                    storeKTable
                            );*/
                        })
                );

        return streamsBuilder.build();
    }

    private static void aggregateOrdersCountByStore(
            KStream<String, Order> orderKStream,
            String stateStore,
            KTable<String, Store> storeKTable
    ) {
        KTable<String, Long> countKTable = orderKStream
                .map((key, order) -> KeyValue.pair(order.locationId(), order))
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
                        storeKTable,
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

    private static void aggregateWindowedOrdersCountByStore(
            KStream<String, Order> orderKStream,
            String stateStore,
            KTable<String, Store> storeKTable
    ) {
        Duration windowedSize = Duration.ofSeconds(3);
        TimeWindows tumblingAggWindow = TimeWindows.ofSizeWithNoGrace(windowedSize);

        KTable<Windowed<String>, Long> countKTable = orderKStream
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
                .peek(OrdersTopology::displayWindowedTableTimestamp)
                .print(
                        Printed.<Windowed<String>, Long>toSysOut().withLabel(stateStore)
                );

        // join between kTable-kTable
        ValueJoiner<Long, Store, TotalCountWithAddress> valueJoiner = TotalCountWithAddress::new;

/*        KTable<String, TotalCountWithAddress> totalCountWithAddressKTable = countKTable
                .join(
                        storeKTable,
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
                );*/
    }

    private static void aggregateOrdersRevenueByStore(
            KStream<String, Order> orderKStream,
            String stateStore,
            KTable<String, Store> storeKTable
    ) {
        // initializer for TotalRevenue java bean
        Initializer<TotalRevenue> totalRevenueInitializer = TotalRevenue::new;

        // aggregator for TotalRevenue java bean
        Aggregator<String, Order, TotalRevenue> totalRevenueAggregator =
                (key, order, aggregate) -> aggregate.updateTotalRevenue(key, order);

        KTable<String, TotalRevenue> totalRevenueKTable = orderKStream
                .groupBy(
                        (key, order) -> order.locationId(),
                        Grouped.with(Serdes.String(), SerdeFactory.generateOrderSerde())
                )
                /*.groupByKey(
                        Grouped.with(Serdes.String(), SerdeFactory.generateOrderSerde())
                )*/
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
                        storeKTable,
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
