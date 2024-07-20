package com.learnkafkastreams.controller;

import com.learnkafkastreams.domain.AllOrdersCountPerStoreDTO;
import com.learnkafkastreams.domain.OrderRevenueDTO;
import com.learnkafkastreams.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/v1/orders")
public class OrdersController {

    private final OrderService orderService;

    public OrdersController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping(path = "/count/{order_type}")
    public ResponseEntity<?> ordersCountPerStore(
            @PathVariable(value = "order_type") String orderType,
            @RequestParam(value = "location_id", required = false) String locationId
    ) {
        if (StringUtils.hasLength(locationId)) {
            return ResponseEntity.ok(orderService.getOrdersCountPerStoreByLocationId(orderType, locationId));
        }

        return ResponseEntity.ok(orderService.getOrdersCountPerStore(orderType));
    }

    @GetMapping(path = "/count")
    public List<AllOrdersCountPerStoreDTO> allOrdersCount() {
        return orderService.getAllOrdersCount();
    }

    @GetMapping(path = "/revenue/{order_type}")
    public ResponseEntity<?> ordersRevenuePerStore(
            @PathVariable(value = "order_type") String orderType,
            @RequestParam(value = "location_id", required = false) String locationId
    ) {
        if (StringUtils.hasLength(locationId)) {
            return ResponseEntity.ok(orderService.getOrdersRevenuePerStoreByLocationId(orderType, locationId));
        }

        return ResponseEntity.ok(orderService.getOrdersRevenuePerStore(orderType));
    }

    @GetMapping(path = "/revenue")
    public List<OrderRevenueDTO> allOrdersRevenue() {
        return orderService.getAllOrdersRevenue();
    }
}
