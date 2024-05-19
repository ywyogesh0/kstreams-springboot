package com.learnkafkastreams.topology;

import com.learnkafkastreams.domain.Order;
import com.learnkafkastreams.domain.OrderType;
import com.learnkafkastreams.domain.Revenue;
import com.learnkafkastreams.domain.TotalRevenue;
import com.learnkafkastreams.serdes.SerdeFactory;
import com.learnkafkastreams.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;

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
                        Consumed.with(Serdes.String(), SerdeFactory.generateOrderSerde())
                );
                /*.selectKey(
                        (key, order) -> order.locationId()
                );*/

        // print - orders stream
        ordersStream.print(
                Printed.<String, Order>toSysOut().withLabel("orders-stream")
        );

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

                            //aggregateOrdersCountByStore(generalOrderKStream, Constants.GENERAL_ORDERS_COUNT);
                            aggregateOrdersRevenueByStore(generalOrderKStream, Constants.GENERAL_ORDERS_REVENUE);
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

                            //aggregateOrdersCountByStore(restaurantOrderKStream, Constants.RESTAURANT_ORDERS_COUNT);
                            aggregateOrdersRevenueByStore(restaurantOrderKStream, Constants.RESTAURANT_ORDERS_REVENUE);
                        })
                );

        return streamsBuilder.build();
    }

    private static void aggregateOrdersCountByStore(KStream<String, Order> orderKStream, String stateStore) {
        orderKStream
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
                )
                .toStream()
                .print(
                        Printed.<String, Long>toSysOut().withLabel(stateStore)
                );
    }

    private static void aggregateOrdersRevenueByStore(KStream<String, Order> orderKStream, String stateStore) {
        // initializer for TotalRevenue java bean
        Initializer<TotalRevenue> totalRevenueInitializer = TotalRevenue::new;

        // aggregator for TotalRevenue java bean
        Aggregator<String, Order, TotalRevenue> totalRevenueAggregator =
                (key, order, aggregate) -> aggregate.updateTotalRevenue(key, order);

        orderKStream
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
                )
                .toStream()
                .print(
                        Printed.<String, TotalRevenue>toSysOut().withLabel(stateStore)
                );
    }
}
