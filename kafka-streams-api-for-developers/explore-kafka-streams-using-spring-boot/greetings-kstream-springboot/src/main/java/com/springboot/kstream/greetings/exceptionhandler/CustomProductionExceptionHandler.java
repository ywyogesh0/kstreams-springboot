package com.springboot.kstream.greetings.exceptionhandler;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.streams.errors.ProductionExceptionHandler;

import java.util.Map;

@Slf4j
public class CustomProductionExceptionHandler implements ProductionExceptionHandler {
    @Override
    public ProductionExceptionHandlerResponse handle(ProducerRecord<byte[], byte[]> record, Exception exception) {
        log.error("Custom Production Exception: Record: {} , Exception Message: {}",
                record, exception.getMessage(), exception);
        return ProductionExceptionHandlerResponse.CONTINUE;
    }

    @Override
    public void configure(Map<String, ?> configs) {
    }
}
