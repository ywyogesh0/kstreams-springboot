package com.learnkafkastreams.exceptionhandler;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.errors.DeserializationExceptionHandler;
import org.apache.kafka.streams.processor.ProcessorContext;

import java.util.Map;

@Slf4j
public class CustomDeserializerExceptionHandler implements DeserializationExceptionHandler {

    // Increment counter at TASK [] level within the STREAM THREAD []
    int count = 1;

    @Override
    public DeserializationHandlerResponse handle(ProcessorContext context, ConsumerRecord<byte[], byte[]> record, Exception exception) {
        if (count > 2) {
            log.error("FAIL -> Custom Deserializer Exception: Count: {} , Record: {} , Exception Message: {}",
                    count, record, exception.getMessage(), exception);
            return DeserializationHandlerResponse.FAIL;
        }

        log.error("CONTINUE -> Custom Deserializer Exception: Count: {} , Record: {} , Exception Message: {}",
                count, record, exception.getMessage(), exception);
        count++;
        return DeserializationHandlerResponse.CONTINUE;
    }

    @Override
    public void configure(Map<String, ?> configs) {
    }
}
