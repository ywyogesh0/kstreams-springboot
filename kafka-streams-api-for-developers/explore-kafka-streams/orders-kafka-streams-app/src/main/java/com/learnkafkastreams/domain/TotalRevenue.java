package com.learnkafkastreams.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public record TotalRevenue(String locationId,
                           Integer runningOrderCount,
                           BigDecimal runningRevenue) {

    private static final Logger log = LoggerFactory.getLogger(TotalRevenue.class);

    public TotalRevenue() {
        this("", 0, BigDecimal.valueOf(0.0));
    }

    public TotalRevenue updateTotalRevenue(String key, Order newValue) {
        log.info("Before Update: {}", this);
        var newRunningOrderCount = this.runningOrderCount + 1;
        var newRunningRevenue = this.runningRevenue().add(newValue.finalAmount());

        TotalRevenue newTotalRevenue = new TotalRevenue(key, newRunningOrderCount, newRunningRevenue);
        log.info("After Update: {}", newTotalRevenue);

        return newTotalRevenue;
    }
}
