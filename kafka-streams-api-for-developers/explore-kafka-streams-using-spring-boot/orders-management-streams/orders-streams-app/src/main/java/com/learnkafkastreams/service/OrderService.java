package com.learnkafkastreams.service;

import com.learnkafkastreams.domain.*;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.learnkafkastreams.util.Constants.*;

@Service
public class OrderService {

    private final OrderStoreService orderStoreService;

    public OrderService(OrderStoreService orderStoreService) {
        this.orderStoreService = orderStoreService;
    }

    public List<OrderCountPerStoreDTO> getOrdersCountPerStore(String orderType) {
        ReadOnlyKeyValueStore<String, Long> readOnlyKeyValueStore =
                orderStoreService.getOrdersCountPerStore(getStoreNameForOrdersCount(orderType));

        return StreamSupport
                .stream(Spliterators
                                .spliteratorUnknownSize(
                                        readOnlyKeyValueStore.all(), 0
                                ),
                        false
                )
                .map(
                        stringLongKeyValue -> new OrderCountPerStoreDTO(
                                stringLongKeyValue.key,
                                stringLongKeyValue.value
                        )
                )
                //.toList();
                .collect(Collectors.toList());
    }

    public OrderCountPerStoreDTO getOrdersCountPerStoreByLocationId(String orderType, String locationId) {
        ReadOnlyKeyValueStore<String, Long> readOnlyKeyValueStore =
                orderStoreService.getOrdersCountPerStore(getStoreNameForOrdersCount(orderType));

        Long ordersCount = readOnlyKeyValueStore.get(locationId);
        if (null != ordersCount) {
            return new OrderCountPerStoreDTO(
                    locationId,
                    ordersCount
            );
        }

        return null;
    }

    public List<AllOrdersCountPerStoreDTO> getAllOrdersCount() {
        // general orders count
        List<OrderCountPerStoreDTO> generalOrdersCount = getOrdersCountPerStore(GENERAL_ORDERS_PATH_VARIABLE);
        // restaurant orders count
        List<OrderCountPerStoreDTO> restaurantOrdersCount = getOrdersCountPerStore(RESTAURANT_ORDERS_PATH_VARIABLE);

        // map general orders count DTO To all order count DTO
        List<AllOrdersCountPerStoreDTO> generalAllOrdersCountList = generalOrdersCount
                .stream()
                .map(orderCountPerStoreDTO -> ordersCountMapper.apply(
                        orderCountPerStoreDTO, OrderType.GENERAL
                ))
                .toList();

        // map restaurant orders count DTO To all order count DTO
        List<AllOrdersCountPerStoreDTO> restaurantAllOrdersCountList = restaurantOrdersCount
                .stream()
                .map(orderCountPerStoreDTO -> ordersCountMapper.apply(
                        orderCountPerStoreDTO, OrderType.RESTAURANT
                ))
                .toList();

        // merge both list
        return Stream
                .of(generalAllOrdersCountList, restaurantAllOrdersCountList)
                .flatMap(Collection::stream)
                //.toList();
                .collect(Collectors.toList());
    }

    public List<OrderRevenueDTO> getOrdersRevenuePerStore(String orderType) {
        ReadOnlyKeyValueStore<String, TotalRevenue> readOnlyKeyValueStore =
                orderStoreService.getOrdersRevenuePerStore(getStoreNameForOrdersRevenue(orderType));

        return StreamSupport
                .stream(Spliterators
                                .spliteratorUnknownSize(
                                        readOnlyKeyValueStore.all(), 0
                                ),
                        false
                )
                .map(
                        stringTotalRevenueKeyValue -> new OrderRevenueDTO(
                                stringTotalRevenueKeyValue.key,
                                getOrderTypeEnum(orderType),
                                stringTotalRevenueKeyValue.value
                        )
                )
                //.toList();
                .collect(Collectors.toList());
    }

    public Object getOrdersRevenuePerStoreByLocationId(String orderType, String locationId) {
        ReadOnlyKeyValueStore<String, TotalRevenue> readOnlyKeyValueStore =
                orderStoreService.getOrdersRevenuePerStore(getStoreNameForOrdersRevenue(orderType));

        TotalRevenue totalRevenue = readOnlyKeyValueStore.get(locationId);
        if (null != totalRevenue) {
            return new OrderRevenueDTO(
                    locationId,
                    getOrderTypeEnum(orderType),
                    totalRevenue
            );
        }

        return null;
    }

    public List<OrderRevenueDTO> getAllOrdersRevenue() {
        // general orders count
        List<OrderRevenueDTO> generalOrdersRevenue = getOrdersRevenuePerStore(GENERAL_ORDERS_PATH_VARIABLE);
        // restaurant orders count
        List<OrderRevenueDTO> restaurantOrdersRevenue = getOrdersRevenuePerStore(RESTAURANT_ORDERS_PATH_VARIABLE);

        // merge both list
        return Stream
                .of(generalOrdersRevenue, restaurantOrdersRevenue)
                .flatMap(Collection::stream)
                //.toList();
                .collect(Collectors.toList());
    }

    private final BiFunction<OrderCountPerStoreDTO, OrderType, AllOrdersCountPerStoreDTO> ordersCountMapper =
            (orderCountPerStoreDTO, orderType) ->
                    new AllOrdersCountPerStoreDTO(
                            orderCountPerStoreDTO.locationId(),
                            orderCountPerStoreDTO.orderCount(),
                            orderType
                    );

    private String getStoreNameForOrdersCount(String orderType) {
        return switch (orderType) {
            case GENERAL_ORDERS_PATH_VARIABLE -> GENERAL_ORDERS_COUNT;
            case RESTAURANT_ORDERS_PATH_VARIABLE -> RESTAURANT_ORDERS_COUNT;
            default -> throw new IllegalArgumentException(
                    "Error: Accepted Order Types -> general_orders OR restaurant_orders"
            );
        };
    }

    private String getStoreNameForOrdersRevenue(String orderType) {
        return switch (orderType) {
            case GENERAL_ORDERS_PATH_VARIABLE -> GENERAL_ORDERS_REVENUE;
            case RESTAURANT_ORDERS_PATH_VARIABLE -> RESTAURANT_ORDERS_REVENUE;
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
}
