package com.learnkafkastreams.controller;

import com.learnkafkastreams.domain.OrdersCountPerStoreByWindowsDTO;
import com.learnkafkastreams.domain.OrdersRevenuePerStoreByWindowsDTO;
import com.learnkafkastreams.service.WindowedOrderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/v1/orders")
public class WindowedOrdersController {

    private final WindowedOrderService windowedOrderService;

    public WindowedOrdersController(WindowedOrderService windowedOrderService) {
        this.windowedOrderService = windowedOrderService;
    }

    @GetMapping(path = "windows/count/{order_type}")
    public List<OrdersCountPerStoreByWindowsDTO> windowedOrdersCountPerStore(
            @PathVariable(value = "order_type") String orderType
    ) {
        return windowedOrderService.getWindowedOrdersCountPerStore(orderType);
    }

    @GetMapping(path = "windows/count")
    public List<OrdersCountPerStoreByWindowsDTO> allWindowedOrdersCount(
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @RequestParam(value = "from_time", required = false)
            LocalDateTime fromTime,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @RequestParam(value = "to_time", required = false)
            LocalDateTime toTime
    ) {
        if (null != fromTime && null != toTime) {
            return windowedOrderService.getAllWindowedOrdersCount(fromTime, toTime);
        }

        return windowedOrderService.getAllWindowedOrdersCount();
    }

    @GetMapping(path = "windows/revenue/{order_type}")
    public List<OrdersRevenuePerStoreByWindowsDTO> windowedOrdersRevenuePerStore(
            @PathVariable(value = "order_type") String orderType
    ) {
        return windowedOrderService.getWindowedOrdersRevenuePerStore(orderType);
    }
}
