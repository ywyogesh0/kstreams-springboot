package com.learnkafkastreams.domain;

public record TotalCountWithAddress(
        Long runningOrderCount,
        Store store
) {
}
