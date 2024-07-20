package com.learnkafkastreams.service;

import com.learnkafkastreams.domain.*;
import com.learnkafkastreams.util.OrderHelperUtility;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.learnkafkastreams.util.Constants.*;

@Service
public class WindowedOrderService {

    private final OrderStoreService orderStoreService;

    public WindowedOrderService(OrderStoreService orderStoreService) {
        this.orderStoreService = orderStoreService;
    }

    public List<OrdersCountPerStoreByWindowsDTO> getWindowedOrdersCountPerStore(String orderType) {
        ReadOnlyWindowStore<String, Long> readOnlyWindowStore =
                orderStoreService.getWindowedOrdersCountPerStore(
                        getWindowedStoreNameForOrdersCount(orderType)
                );

        return getWindowedOrdersCount(getOrderTypeEnum(orderType), readOnlyWindowStore.all());
    }

    public List<OrdersCountPerStoreByWindowsDTO> getAllWindowedOrdersCount() {
        // general windowed orders count
        List<OrdersCountPerStoreByWindowsDTO> generalWindowedOrdersCount =
                getWindowedOrdersCountPerStore(GENERAL_ORDERS_PATH_VARIABLE);
        // restaurant windowed orders count
        List<OrdersCountPerStoreByWindowsDTO> restaurantWindowedOrdersCount =
                getWindowedOrdersCountPerStore(RESTAURANT_ORDERS_PATH_VARIABLE);

        // merge both list
        return Stream
                .of(generalWindowedOrdersCount, restaurantWindowedOrdersCount)
                .flatMap(Collection::stream)
                //.toList();
                .collect(Collectors.toList());
    }

    public List<OrdersCountPerStoreByWindowsDTO> getAllWindowedOrdersCount(
            LocalDateTime fromTime,
            LocalDateTime toTime
    ) {
        // general windowed orders count
        ReadOnlyWindowStore<String, Long> readOnlyGeneralWindowStore =
                orderStoreService.getWindowedOrdersCountPerStore(
                        getWindowedStoreNameForOrdersCount(GENERAL_ORDERS_PATH_VARIABLE)
                );

        List<OrdersCountPerStoreByWindowsDTO> generalWindowedOrdersCount = getWindowedOrdersCount(
                getOrderTypeEnum(GENERAL_ORDERS_PATH_VARIABLE),
                readOnlyGeneralWindowStore.fetchAll(
                        OrderHelperUtility.convertLocalToInstantTime(fromTime),
                        OrderHelperUtility.convertLocalToInstantTime(toTime)
                )
        );

        // restaurant windowed orders count
        ReadOnlyWindowStore<String, Long> readOnlyRestaurantWindowStore =
                orderStoreService.getWindowedOrdersCountPerStore(
                        getWindowedStoreNameForOrdersCount(RESTAURANT_ORDERS_PATH_VARIABLE)
                );

        List<OrdersCountPerStoreByWindowsDTO> restaurantWindowedOrdersCount = getWindowedOrdersCount(
                getOrderTypeEnum(RESTAURANT_ORDERS_PATH_VARIABLE),
                readOnlyRestaurantWindowStore.fetchAll(
                        OrderHelperUtility.convertLocalToInstantTime(fromTime),
                        OrderHelperUtility.convertLocalToInstantTime(toTime)
                )
        );

        // merge both list
        return Stream
                .of(generalWindowedOrdersCount, restaurantWindowedOrdersCount)
                .flatMap(Collection::stream)
                //.toList();
                .collect(Collectors.toList());
    }

    public List<OrdersRevenuePerStoreByWindowsDTO> getWindowedOrdersRevenuePerStore(String orderType) {
        ReadOnlyWindowStore<String, TotalRevenue> readOnlyWindowStore =
                orderStoreService.getWindowedOrdersRevenuePerStore(
                        getWindowedStoreNameForOrdersRevenue(orderType)
                );

        return StreamSupport
                .stream(Spliterators
                                .spliteratorUnknownSize(
                                        readOnlyWindowStore.all(), 0
                                ),
                        false
                )
                .map(
                        windowedTotalRevenueKeyValue -> new OrdersRevenuePerStoreByWindowsDTO(
                                windowedTotalRevenueKeyValue.key.key(),
                                windowedTotalRevenueKeyValue.value,
                                getOrderTypeEnum(orderType),
                                OrderHelperUtility
                                        .convertInstantToLocalTime(windowedTotalRevenueKeyValue.key.window().startTime()),
                                OrderHelperUtility
                                        .convertInstantToLocalTime(windowedTotalRevenueKeyValue.key.window().endTime())
                        )
                )
                //.toList();
                .collect(Collectors.toList());
    }

    private String getWindowedStoreNameForOrdersCount(String orderType) {
        return switch (orderType) {
            case GENERAL_ORDERS_PATH_VARIABLE -> GENERAL_WINDOWED_ORDERS_COUNT;
            case RESTAURANT_ORDERS_PATH_VARIABLE -> RESTAURANT_WINDOWED_ORDERS_COUNT;
            default -> throw new IllegalArgumentException(
                    "Error: Accepted Order Types -> general_orders OR restaurant_orders"
            );
        };
    }

    private String getWindowedStoreNameForOrdersRevenue(String orderType) {
        return switch (orderType) {
            case GENERAL_ORDERS_PATH_VARIABLE -> GENERAL_WINDOWED_ORDERS_REVENUE;
            case RESTAURANT_ORDERS_PATH_VARIABLE -> RESTAURANT_WIDOWED_ORDERS_REVENUE;
            default -> throw new IllegalArgumentException(
                    "Error: Accepted Order Types -> general_orders OR restaurant_orders"
            );
        };
    }

    private OrderType getOrderTypeEnum(String orderType) {
        return switch (orderType) {
            case GENERAL_ORDERS_PATH_VARIABLE -> OrderType.GENERAL;
            case RESTAURANT_ORDERS_PATH_VARIABLE -> OrderType.RESTAURANT;
            default -> throw new IllegalArgumentException(
                    "Error: Accepted Order Types -> general_orders OR restaurant_orders"
            );
        };
    }

    private List<OrdersCountPerStoreByWindowsDTO> getWindowedOrdersCount(
            OrderType orderType,
            KeyValueIterator<Windowed<String>, Long> windowedLongKeyValueIterator
    ) {
        return StreamSupport
                .stream(Spliterators
                                .spliteratorUnknownSize(windowedLongKeyValueIterator, 0),
                        false
                )
                .map(
                        windowedLongKeyValue -> new OrdersCountPerStoreByWindowsDTO(
                                windowedLongKeyValue.key.key(),
                                windowedLongKeyValue.value,
                                orderType,
                                OrderHelperUtility
                                        .convertInstantToLocalTime(windowedLongKeyValue.key.window().startTime()),
                                OrderHelperUtility
                                        .convertInstantToLocalTime(windowedLongKeyValue.key.window().endTime())
                        )
                )
                //.toList();
                .collect(Collectors.toList());
    }
}
