package com.learnkafkastreams.topology;

import com.learnkafkastreams.domain.*;
import com.learnkafkastreams.handler.AggOrdersCountByStore;
import com.learnkafkastreams.handler.AggWindowedOrdersCountByStore;
import com.learnkafkastreams.serdes.SerdeFactory;
import com.learnkafkastreams.util.Constants;
import com.learnkafkastreams.util.OrderTimestampExtractor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
public class OrdersTopology {

    public static Topology buildTopology() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        // AggWindowedOrdersCountByStore
        AggWindowedOrdersCountByStore aggWindowedOrdersCountByStore =
                new AggWindowedOrdersCountByStore();

        // AggOrdersCountByStore
        AggOrdersCountByStore aggOrdersCountByStore =
                new AggOrdersCountByStore();

        // general order - predicate
        Predicate<String, Order> generalPredicate = ((orderKey, orderValue) ->
                OrderType.GENERAL.equals(orderValue.orderType()));

        // restaurant order - predicate
        Predicate<String, Order> restaurantPredicate = ((orderKey, orderValue) ->
                OrderType.RESTAURANT.equals(orderValue.orderType()));

        // transform order to revenue domain stream
        ValueMapper<Order, Revenue> revenueValueMapper = (order) -> new Revenue(
                order.locationId(),
                order.finalAmount()
        );

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

                            /*aggOrdersCountByStore
                                    .aggregate(
                                            generalOrderKStream,
                                            Constants.GENERAL_ORDERS_COUNT,
                                            storeKTable
                                    );*/

                            aggWindowedOrdersCountByStore
                                    .aggregate(
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

                            /*aggOrdersCountByStore
                                    .aggregate(
                                            restaurantOrderKStream,
                                            Constants.RESTAURANT_ORDERS_COUNT,
                                            storeKTable
                                    );*/

                            aggWindowedOrdersCountByStore
                                    .aggregate(
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
}
