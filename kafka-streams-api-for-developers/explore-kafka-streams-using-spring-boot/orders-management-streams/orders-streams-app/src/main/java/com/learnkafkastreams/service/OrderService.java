package com.learnkafkastreams.service;

import com.learnkafkastreams.domain.OrderCountPerStoreDTO;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Spliterators;
import java.util.stream.Collectors;
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
                orderStoreService.getOrdersCountByOrderType(getStoreName(orderType));

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
                .collect(Collectors.toList());
    }

    private String getStoreName(String orderType) {
        return switch (orderType) {
            case GENERAL_ORDERS_PATH_VARIABLE -> GENERAL_ORDERS_COUNT;
            case RESTAURANT_ORDERS_PATH_VARIABLE -> RESTAURANT_ORDERS_COUNT;
            default -> throw new IllegalArgumentException(
                    "Error: Accepted Order Types -> general_orders OR restaurant_orders"
            );
        };
    }
}
