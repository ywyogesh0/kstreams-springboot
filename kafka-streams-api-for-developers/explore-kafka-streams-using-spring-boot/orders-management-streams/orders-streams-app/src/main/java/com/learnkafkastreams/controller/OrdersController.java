package com.learnkafkastreams.controller;

import com.learnkafkastreams.domain.OrderCountPerStoreDTO;
import com.learnkafkastreams.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/v1/orders")
public class OrdersController {

    private final OrderService orderService;

    public OrdersController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping(path = "/count/{orders_type}")
    public List<OrderCountPerStoreDTO> getOrdersCountPerStore(
            @PathVariable(value = "order_type") String orderType
    ) {
        return orderService.getOrdersCountPerStore(orderType);
    }
}
