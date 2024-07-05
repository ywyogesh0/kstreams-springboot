package com.learnkafkastreams.service;

import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class OrderStoreService {

    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    public OrderStoreService(StreamsBuilderFactoryBean streamsBuilderFactoryBean) {
        this.streamsBuilderFactoryBean = streamsBuilderFactoryBean;
    }

    public ReadOnlyKeyValueStore<String, Long> getOrdersCountByOrderType(String storeName) {
        return Objects.requireNonNull(streamsBuilderFactoryBean.getKafkaStreams())
                .store(
                        StoreQueryParameters.fromNameAndType(
                                storeName, QueryableStoreTypes.keyValueStore()
                        )
                );
    }
}
