package com.learnkafkastreams.topology;

import com.learnkafkastreams.domain.Order;
import com.learnkafkastreams.domain.OrderLineItem;
import com.learnkafkastreams.domain.OrderType;
import com.learnkafkastreams.domain.TotalRevenue;
import com.learnkafkastreams.serdes.SerdeFactory;
import com.learnkafkastreams.util.Constants;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrdersTopologyTest {

    TopologyTestDriver topologyTestDriver;
    private TestInputTopic<String, Order> inputTopic;

    @BeforeEach
    void setUp() {
        topologyTestDriver = new TopologyTestDriver(OrdersTopology.buildTopology());
        inputTopic = topologyTestDriver.createInputTopic(
                Constants.ORDERS_TOPIC,
                Serdes.String().serializer(),
                SerdeFactory.generateOrderSerde().serializer()
        );
    }

    @AfterEach
    void tearDown() {
        if (null != topologyTestDriver) topologyTestDriver.close();
    }

    @Test
    void testTopology_readOnlyKeyValueStore() {
        inputTopic.pipeKeyValueList(
                orders("store_1234", "2024-05-01T08:51:52")
        );
        inputTopic.pipeKeyValueList(
                orders("store_1234", "2022-05-01T08:51:53")
        );
        inputTopic.pipeKeyValueList(
                orders("store_5678", "2022-05-01T08:51:54")
        );

        // assert orders count
        ReadOnlyKeyValueStore<String, Long> generalOrdersCountStore =
                topologyTestDriver.getKeyValueStore(Constants.GENERAL_ORDERS_COUNT);
        ReadOnlyKeyValueStore<String, Long> restaurantOrdersCountStore =
                topologyTestDriver.getKeyValueStore(Constants.RESTAURANT_ORDERS_COUNT);

        assertEquals(2, generalOrdersCountStore.get("store_1234"));
        assertEquals(1, generalOrdersCountStore.get("store_5678"));
        assertEquals(2, restaurantOrdersCountStore.get("store_1234"));
        assertEquals(1, restaurantOrdersCountStore.get("store_5678"));

        // assert orders revenue
        ReadOnlyKeyValueStore<String, TotalRevenue> generalOrdersRevenueStore =
                topologyTestDriver.getKeyValueStore(Constants.GENERAL_ORDERS_REVENUE);
        ReadOnlyKeyValueStore<String, TotalRevenue> restaurantOrdersRevenueStore =
                topologyTestDriver.getKeyValueStore(Constants.RESTAURANT_ORDERS_REVENUE);

        assertEquals(0, generalOrdersRevenueStore
                .get("store_1234")
                .runningRevenue()
                .compareTo(new BigDecimal("54.00"))
        );

        assertEquals(0, generalOrdersRevenueStore
                .get("store_5678")
                .runningRevenue()
                .compareTo(new BigDecimal("27.00"))
        );

        assertEquals(0, restaurantOrdersRevenueStore
                .get("store_1234")
                .runningRevenue()
                .compareTo(new BigDecimal("30.00"))
        );

        assertEquals(0, restaurantOrdersRevenueStore
                .get("store_5678")
                .runningRevenue()
                .compareTo(new BigDecimal("15.00"))
        );
    }

    @Test
    void testTopology_windowedKeyValueStore() {
        inputTopic.pipeKeyValueList(
                orders("store_1234", "2022-05-01T08:51:52")
        );
        inputTopic.pipeKeyValueList(
                orders("store_1234", "2022-05-01T08:51:53")
        );
        inputTopic.pipeKeyValueList(
                orders("store_5678", "2022-05-01T08:51:55")
        );
        inputTopic.pipeKeyValueList(
                orders("store_5678", "2022-05-01T08:51:56")
        );

        // assert orders count
        ReadOnlyWindowStore<String, Long> generalWindowedOrdersCountStore =
                topologyTestDriver.getWindowStore(Constants.GENERAL_WINDOWED_ORDERS_COUNT);
        ReadOnlyWindowStore<String, Long> restaurantWindowedOrdersCountStore =
                topologyTestDriver.getWindowStore(Constants.RESTAURANT_WINDOWED_ORDERS_COUNT);

        assertWindowedOrderCount(generalWindowedOrdersCountStore, OrderType.GENERAL);
        assertWindowedOrderCount(restaurantWindowedOrdersCountStore, OrderType.RESTAURANT);

        // assert orders revenue
        ReadOnlyWindowStore<String, TotalRevenue> generalWindowedOrdersRevenueStore =
                topologyTestDriver.getWindowStore(Constants.GENERAL_WINDOWED_ORDERS_REVENUE);
        ReadOnlyWindowStore<String, TotalRevenue> restaurantWindowedOrdersRevenueStore =
                topologyTestDriver.getWindowStore(Constants.RESTAURANT_WIDOWED_ORDERS_REVENUE);

        assertWindowedOrderRevenue(generalWindowedOrdersRevenueStore, OrderType.GENERAL);
        assertWindowedOrderRevenue(restaurantWindowedOrdersRevenueStore, OrderType.RESTAURANT);
    }

    private void assertWindowedOrderCount(
            ReadOnlyWindowStore<String, Long> readOnlyWindowStore, OrderType orderType
    ) {
        KeyValueIterator<Windowed<String>, Long> keyValueIterator = readOnlyWindowStore.all();
        List<KeyValue<Windowed<String>, Long>> keyValueList = StreamSupport
                .stream(
                        Spliterators
                                .spliteratorUnknownSize(keyValueIterator, 0),
                        false
                )
                .toList();

        assertEquals(2, keyValueList.size());
        assertWindowedOrderCount(keyValueList.get(0), orderType);
        assertWindowedOrderCount(keyValueList.get(1), orderType);
    }

    private void assertWindowedOrderRevenue(
            ReadOnlyWindowStore<String, TotalRevenue> readOnlyWindowStore, OrderType orderType
    ) {
        KeyValueIterator<Windowed<String>, TotalRevenue> keyValueIterator = readOnlyWindowStore.all();
        List<KeyValue<Windowed<String>, TotalRevenue>> keyValueList = StreamSupport
                .stream(
                        Spliterators
                                .spliteratorUnknownSize(keyValueIterator, 0),
                        false
                )
                .toList();

        assertEquals(2, keyValueList.size());
        assertWindowedOrderRevenue(keyValueList.get(0), orderType);
        assertWindowedOrderRevenue(keyValueList.get(1), orderType);
    }

    private static void assertWindowedOrderCount(KeyValue<Windowed<String>, Long> keyValue, OrderType orderType) {
        String key = keyValue.key.key();
        Long count = keyValue.value;
        Instant startTime = keyValue.key.window().startTime();
        Instant endTime = keyValue.key.window().endTime();

        System.out.println("Order Type: " + orderType.name());
        System.out.println("Window Key: " + key);
        System.out.println("Window value: " + count);
        System.out.println("Window startTime: " + startTime);
        System.out.println("Window endTime: " + endTime);

        assertEquals(2, count);

        if ("store_1234".equals(key)) {
            assertEquals(
                    LocalDateTime.parse("2022-05-01T08:51:51"),
                    LocalDateTime.ofInstant(startTime, ZoneId.of("Europe/London"))
            );
            assertEquals(
                    LocalDateTime.parse("2022-05-01T08:51:54"),
                    LocalDateTime.ofInstant(endTime, ZoneId.of("Europe/London"))
            );
        } else {
            assertEquals(
                    LocalDateTime.parse("2022-05-01T08:51:54"),
                    LocalDateTime.ofInstant(startTime, ZoneId.of("Europe/London"))
            );
            assertEquals(
                    LocalDateTime.parse("2022-05-01T08:51:57"),
                    LocalDateTime.ofInstant(endTime, ZoneId.of("Europe/London"))
            );
        }
    }

    private static void assertWindowedOrderRevenue(KeyValue<Windowed<String>, TotalRevenue> keyValue, OrderType orderType) {
        String key = keyValue.key.key();
        TotalRevenue totalRevenue = keyValue.value;
        Instant startTime = keyValue.key.window().startTime();
        Instant endTime = keyValue.key.window().endTime();

        System.out.println("Order Type: " + orderType.name());
        System.out.println("Window Key: " + key);
        System.out.println("Window value: " + totalRevenue);
        System.out.println("Window startTime: " + startTime);
        System.out.println("Window endTime: " + endTime);

        assertEquals(2, totalRevenue.runningOrderCount());

        if ("store_1234".equals(key)) {
            assertEquals(
                    LocalDateTime.parse("2022-05-01T08:51:51"),
                    LocalDateTime.ofInstant(startTime, ZoneId.of("Europe/London"))
            );
            assertEquals(
                    LocalDateTime.parse("2022-05-01T08:51:54"),
                    LocalDateTime.ofInstant(endTime, ZoneId.of("Europe/London"))
            );
        } else {
            assertEquals(
                    LocalDateTime.parse("2022-05-01T08:51:54"),
                    LocalDateTime.ofInstant(startTime, ZoneId.of("Europe/London"))
            );
            assertEquals(
                    LocalDateTime.parse("2022-05-01T08:51:57"),
                    LocalDateTime.ofInstant(endTime, ZoneId.of("Europe/London"))
            );
        }
    }

    private static List<KeyValue<String, Order>> orders(
            String locationId,
            String timestamp
    ) {

        var orderItems = List.of(
                new OrderLineItem("Bananas", 2, new BigDecimal("2.00")),
                new OrderLineItem("Iphone Charger", 1, new BigDecimal("25.00"))
        );

        var orderItemsRestaurant = List.of(
                new OrderLineItem("Pizza", 2, new BigDecimal("12.00")),
                new OrderLineItem("Coffee", 1, new BigDecimal("3.00"))
        );

        var order1 = new Order(12345, locationId,
                new BigDecimal("27.00"),
                OrderType.GENERAL,
                orderItems,
                LocalDateTime.parse(timestamp)
                //LocalDateTime.now(ZoneId.of("UTC"))
        );

        var order2 = new Order(54321, locationId,
                new BigDecimal("15.00"),
                OrderType.RESTAURANT,
                orderItemsRestaurant,
                LocalDateTime.parse(timestamp)
                //LocalDateTime.now(ZoneId.of("UTC"))
        );
        var keyValue1 = KeyValue.pair(order1.locationId()
                , order1);

        var keyValue2 = KeyValue.pair(order2.locationId()
                , order2);

        return List.of(keyValue1, keyValue2);
    }
}