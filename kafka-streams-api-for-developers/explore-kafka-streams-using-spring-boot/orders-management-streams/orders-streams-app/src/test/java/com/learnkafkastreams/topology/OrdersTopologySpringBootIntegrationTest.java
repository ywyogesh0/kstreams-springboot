package com.learnkafkastreams.topology;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafkastreams.domain.Order;
import com.learnkafkastreams.domain.OrderLineItem;
import com.learnkafkastreams.domain.OrderType;
import com.learnkafkastreams.service.OrderService;
import com.learnkafkastreams.service.WindowedOrderService;
import com.learnkafkastreams.util.Constants;
import org.apache.kafka.streams.KeyValue;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(topics = {Constants.ORDERS_TOPIC, Constants.STORES_TOPIC})
@TestPropertySource(properties = {
        "spring.kafka.streams.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.streams.auto-startup=false"
})
@ActiveProfiles(value = {"test" })
public class OrdersTopologySpringBootIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private WindowedOrderService windowedOrderService;

    @BeforeEach
    public void setUp() {
        streamsBuilderFactoryBean.start();
    }

    @AfterEach
    public void destroy() {
        if (null != streamsBuilderFactoryBean && null != streamsBuilderFactoryBean.getKafkaStreams()) {
            streamsBuilderFactoryBean.getKafkaStreams().close();
            streamsBuilderFactoryBean.getKafkaStreams().cleanUp();
        }
    }

    @Test
    void test_ordersCount() {
        publishOrders("store_1234", "2022-05-01T08:51:52");
        publishOrders("store_1234", "2022-05-01T08:51:53");
        publishOrders("store_5678", "2022-05-01T08:51:55");
        publishOrders("store_5678", "2022-05-01T08:51:56");

        // general orders count
        Awaitility
                .await()
                .atMost(10, TimeUnit.SECONDS)
                .pollDelay(Duration.ofSeconds(1))
                .ignoreExceptions()
                .until(() -> orderService.getOrdersCountPerStore(Constants.GENERAL_ORDERS_PATH_VARIABLE).size(),
                        equalTo(2));

        var generalOrdersCount = orderService
                .getOrdersCountPerStore(Constants.GENERAL_ORDERS_PATH_VARIABLE);

        assertEquals(2, generalOrdersCount.get(0).orderCount());
        assertEquals(2, generalOrdersCount.get(1).orderCount());

        // restaurant orders count
        Awaitility
                .await()
                .atMost(10, TimeUnit.SECONDS)
                .pollDelay(Duration.ofSeconds(1))
                .ignoreExceptions()
                .until(() -> orderService.getOrdersCountPerStore(Constants.RESTAURANT_ORDERS_PATH_VARIABLE).size(),
                        equalTo(2));

        var restaurantOrdersCount = orderService
                .getOrdersCountPerStore(Constants.RESTAURANT_ORDERS_PATH_VARIABLE);

        assertEquals(2, restaurantOrdersCount.get(0).orderCount());
        assertEquals(2, restaurantOrdersCount.get(1).orderCount());
    }

    @Test
    void test_ordersRevenue() {
        publishOrders("store_1234", "2022-05-01T08:51:52");
        publishOrders("store_1234", "2022-05-01T08:51:53");
        publishOrders("store_5678", "2022-05-01T08:51:55");
        publishOrders("store_5678", "2022-05-01T08:51:56");

        // general orders revenue
        Awaitility
                .await()
                .atMost(10, TimeUnit.SECONDS)
                .pollDelay(Duration.ofSeconds(1))
                .ignoreExceptions()
                .until(() -> orderService.getOrdersRevenuePerStore(Constants.GENERAL_ORDERS_PATH_VARIABLE).size(),
                        equalTo(2));

        var generalOrdersRevenue = orderService
                .getOrdersRevenuePerStore(Constants.GENERAL_ORDERS_PATH_VARIABLE);

        assertEquals(2, generalOrdersRevenue.get(0).totalRevenue().runningOrderCount());
        assertEquals(2, generalOrdersRevenue.get(1).totalRevenue().runningOrderCount());

        assertEquals(0, new BigDecimal("54.00")
                .compareTo(generalOrdersRevenue.get(0).totalRevenue().runningRevenue()));
        assertEquals(0, new BigDecimal("54.00")
                .compareTo(generalOrdersRevenue.get(1).totalRevenue().runningRevenue()));

        // restaurant orders revenue
        Awaitility
                .await()
                .atMost(10, TimeUnit.SECONDS)
                .pollDelay(Duration.ofSeconds(1))
                .ignoreExceptions()
                .until(() -> orderService.getOrdersRevenuePerStore(Constants.RESTAURANT_ORDERS_PATH_VARIABLE).size(),
                        equalTo(2));

        var restaurantOrdersRevenue = orderService
                .getOrdersRevenuePerStore(Constants.RESTAURANT_ORDERS_PATH_VARIABLE);

        assertEquals(2, restaurantOrdersRevenue.get(0).totalRevenue().runningOrderCount());
        assertEquals(2, restaurantOrdersRevenue.get(1).totalRevenue().runningOrderCount());

        assertEquals(0, new BigDecimal("30.00")
                .compareTo(restaurantOrdersRevenue.get(0).totalRevenue().runningRevenue()));
        assertEquals(0, new BigDecimal("30.00")
                .compareTo(restaurantOrdersRevenue.get(1).totalRevenue().runningRevenue()));
    }

    @Test
    void test_windowedOrdersCount() {
        publishOrders("store_1234", "2022-05-01T08:51:52");
        publishOrders("store_1234", "2022-05-01T08:51:53");
        publishOrders("store_5678", "2022-05-01T08:51:55");
        publishOrders("store_5678", "2022-05-01T08:51:56");

        // general orders count
        Awaitility
                .await()
                .atMost(10, TimeUnit.SECONDS)
                .pollDelay(Duration.ofSeconds(1))
                .ignoreExceptions()
                .until(() -> windowedOrderService.getWindowedOrdersCountPerStore(Constants.GENERAL_ORDERS_PATH_VARIABLE).size(),
                        equalTo(2));

        var generalWindowedOrdersCount = windowedOrderService
                .getWindowedOrdersCountPerStore(Constants.GENERAL_ORDERS_PATH_VARIABLE);

        assertEquals(2, generalWindowedOrdersCount.get(0).orderCount());
        assertEquals(2, generalWindowedOrdersCount.get(1).orderCount());

        assertEquals(
                LocalDateTime.parse("2022-05-01T08:51:51").toInstant(ZoneOffset.ofHours(1)),
                generalWindowedOrdersCount.get(0).startWindow().toInstant(ZoneOffset.UTC)
        );
        assertEquals(
                LocalDateTime.parse("2022-05-01T08:51:54").toInstant(ZoneOffset.ofHours(1)),
                generalWindowedOrdersCount.get(0).endWindow().toInstant(ZoneOffset.UTC)
        );

        assertEquals(
                LocalDateTime.parse("2022-05-01T08:51:54").toInstant(ZoneOffset.ofHours(1)),
                generalWindowedOrdersCount.get(1).startWindow().toInstant(ZoneOffset.UTC)
        );
        assertEquals(
                LocalDateTime.parse("2022-05-01T08:51:57").toInstant(ZoneOffset.ofHours(1)),
                generalWindowedOrdersCount.get(1).endWindow().toInstant(ZoneOffset.UTC)
        );

        // restaurant orders count
        Awaitility
                .await()
                .atMost(10, TimeUnit.SECONDS)
                .pollDelay(Duration.ofSeconds(1))
                .ignoreExceptions()
                .until(() -> windowedOrderService.getWindowedOrdersCountPerStore(Constants.RESTAURANT_ORDERS_PATH_VARIABLE).size(),
                        equalTo(2));

        var restaurantWindowedOrdersCount = windowedOrderService
                .getWindowedOrdersCountPerStore(Constants.RESTAURANT_ORDERS_PATH_VARIABLE);

        assertEquals(2, restaurantWindowedOrdersCount.get(0).orderCount());
        assertEquals(2, restaurantWindowedOrdersCount.get(1).orderCount());

        assertEquals(
                LocalDateTime.parse("2022-05-01T08:51:51").toInstant(ZoneOffset.ofHours(1)),
                restaurantWindowedOrdersCount.get(0).startWindow().toInstant(ZoneOffset.UTC)
        );
        assertEquals(
                LocalDateTime.parse("2022-05-01T08:51:54").toInstant(ZoneOffset.ofHours(1)),
                restaurantWindowedOrdersCount.get(0).endWindow().toInstant(ZoneOffset.UTC)
        );

        assertEquals(
                LocalDateTime.parse("2022-05-01T08:51:54").toInstant(ZoneOffset.ofHours(1)),
                restaurantWindowedOrdersCount.get(1).startWindow().toInstant(ZoneOffset.UTC)
        );
        assertEquals(
                LocalDateTime.parse("2022-05-01T08:51:57").toInstant(ZoneOffset.ofHours(1)),
                restaurantWindowedOrdersCount.get(1).endWindow().toInstant(ZoneOffset.UTC)
        );
    }

    @Test
    void test_windowedOrdersRevenue() {
        publishOrders("store_1234", "2022-05-01T08:51:52");
        publishOrders("store_1234", "2022-05-01T08:51:53");
        publishOrders("store_5678", "2022-05-01T08:51:55");
        publishOrders("store_5678", "2022-05-01T08:51:56");

        // general orders revenue
        Awaitility
                .await()
                .atMost(10, TimeUnit.SECONDS)
                .pollDelay(Duration.ofSeconds(1))
                .ignoreExceptions()
                .until(() -> windowedOrderService.getWindowedOrdersRevenuePerStore(Constants.GENERAL_ORDERS_PATH_VARIABLE).size(),
                        equalTo(2));

        var generalWindowedOrdersRevenue = windowedOrderService
                .getWindowedOrdersRevenuePerStore(Constants.GENERAL_ORDERS_PATH_VARIABLE);

        assertEquals(2, generalWindowedOrdersRevenue.get(0).totalRevenue().runningOrderCount());
        assertEquals(2, generalWindowedOrdersRevenue.get(1).totalRevenue().runningOrderCount());

        assertEquals(0, new BigDecimal("54.00")
                .compareTo(generalWindowedOrdersRevenue.get(0).totalRevenue().runningRevenue()));
        assertEquals(0, new BigDecimal("54.00")
                .compareTo(generalWindowedOrdersRevenue.get(1).totalRevenue().runningRevenue()));

        assertEquals(
                LocalDateTime.parse("2022-05-01T08:51:51").toInstant(ZoneOffset.ofHours(1)),
                generalWindowedOrdersRevenue.get(0).startWindow().toInstant(ZoneOffset.UTC)
        );
        assertEquals(
                LocalDateTime.parse("2022-05-01T08:51:54").toInstant(ZoneOffset.ofHours(1)),
                generalWindowedOrdersRevenue.get(0).endWindow().toInstant(ZoneOffset.UTC)
        );

        assertEquals(
                LocalDateTime.parse("2022-05-01T08:51:54").toInstant(ZoneOffset.ofHours(1)),
                generalWindowedOrdersRevenue.get(1).startWindow().toInstant(ZoneOffset.UTC)
        );
        assertEquals(
                LocalDateTime.parse("2022-05-01T08:51:57").toInstant(ZoneOffset.ofHours(1)),
                generalWindowedOrdersRevenue.get(1).endWindow().toInstant(ZoneOffset.UTC)
        );

        // restaurant orders revenue
        Awaitility
                .await()
                .atMost(10, TimeUnit.SECONDS)
                .pollDelay(Duration.ofSeconds(1))
                .ignoreExceptions()
                .until(() -> windowedOrderService.getWindowedOrdersRevenuePerStore(Constants.RESTAURANT_ORDERS_PATH_VARIABLE).size(),
                        equalTo(2));

        var restaurantWindowedOrdersRevenue = windowedOrderService
                .getWindowedOrdersRevenuePerStore(Constants.RESTAURANT_ORDERS_PATH_VARIABLE);

        assertEquals(2, restaurantWindowedOrdersRevenue.get(0).totalRevenue().runningOrderCount());
        assertEquals(2, restaurantWindowedOrdersRevenue.get(1).totalRevenue().runningOrderCount());

        assertEquals(0, new BigDecimal("30.00")
                .compareTo(restaurantWindowedOrdersRevenue.get(0).totalRevenue().runningRevenue()));
        assertEquals(0, new BigDecimal("30.00")
                .compareTo(restaurantWindowedOrdersRevenue.get(1).totalRevenue().runningRevenue()));

        assertEquals(
                LocalDateTime.parse("2022-05-01T08:51:51").toInstant(ZoneOffset.ofHours(1)),
                restaurantWindowedOrdersRevenue.get(0).startWindow().toInstant(ZoneOffset.UTC)
        );
        assertEquals(
                LocalDateTime.parse("2022-05-01T08:51:54").toInstant(ZoneOffset.ofHours(1)),
                restaurantWindowedOrdersRevenue.get(0).endWindow().toInstant(ZoneOffset.UTC)
        );

        assertEquals(
                LocalDateTime.parse("2022-05-01T08:51:54").toInstant(ZoneOffset.ofHours(1)),
                restaurantWindowedOrdersRevenue.get(1).startWindow().toInstant(ZoneOffset.UTC)
        );
        assertEquals(
                LocalDateTime.parse("2022-05-01T08:51:57").toInstant(ZoneOffset.ofHours(1)),
                restaurantWindowedOrdersRevenue.get(1).endWindow().toInstant(ZoneOffset.UTC)
        );
    }

    private void publishOrders(String locationId, String timestamp) {
        orders(locationId, timestamp)
                .forEach(order -> {
                    String orderJSON;
                    try {
                        orderJSON = objectMapper.writeValueAsString(order.value);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    kafkaTemplate.send(Constants.ORDERS_TOPIC, order.key, orderJSON);
                });
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
