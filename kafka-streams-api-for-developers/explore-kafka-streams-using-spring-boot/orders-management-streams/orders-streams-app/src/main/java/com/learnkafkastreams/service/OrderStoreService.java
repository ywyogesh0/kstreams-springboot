package com.learnkafkastreams.service;

import com.learnkafkastreams.domain.TotalRevenue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class OrderStoreService {

    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    public OrderStoreService(StreamsBuilderFactoryBean streamsBuilderFactoryBean) {
        this.streamsBuilderFactoryBean = streamsBuilderFactoryBean;
    }

    public ReadOnlyKeyValueStore<String, Long> getOrdersCountPerStore(String storeName) {
        return Objects.requireNonNull(streamsBuilderFactoryBean.getKafkaStreams())
                .store(
                        StoreQueryParameters.fromNameAndType(
                                storeName, QueryableStoreTypes.keyValueStore()
                        )
                );
    }

    public ReadOnlyKeyValueStore<String, TotalRevenue> getOrdersRevenuePerStore(String storeName) {
        return Objects.requireNonNull(streamsBuilderFactoryBean.getKafkaStreams())
                .store(
                        StoreQueryParameters.fromNameAndType(
                                storeName, QueryableStoreTypes.keyValueStore()
                        )
                );
    }

    public ReadOnlyWindowStore<String, Long> getWindowedOrdersCountPerStore(String storeName) {
        return Objects.requireNonNull(streamsBuilderFactoryBean.getKafkaStreams())
                .store(
                        StoreQueryParameters.fromNameAndType(
                                storeName, QueryableStoreTypes.windowStore()
                        )
                );
    }

    public ReadOnlyWindowStore<String, TotalRevenue> getWindowedOrdersRevenuePerStore(String storeName) {
        return Objects.requireNonNull(streamsBuilderFactoryBean.getKafkaStreams())
                .store(
                        StoreQueryParameters.fromNameAndType(
                                storeName, QueryableStoreTypes.windowStore()
                        )
                );
    }
}
