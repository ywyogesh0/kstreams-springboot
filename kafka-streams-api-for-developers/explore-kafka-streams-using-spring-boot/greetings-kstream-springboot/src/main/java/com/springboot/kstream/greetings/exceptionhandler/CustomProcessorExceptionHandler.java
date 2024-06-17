package com.springboot.kstream.greetings.exceptionhandler;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.errors.StreamsException;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;

@Slf4j
public class CustomProcessorExceptionHandler implements StreamsUncaughtExceptionHandler {
    @Override
    public StreamThreadExceptionResponse handle(Throwable exception) {
        log.error("Uncaught Stream Processor Exception: {}", exception.getMessage(), exception);

        if(exception instanceof StreamsException) {
            return StreamThreadExceptionResponse.SHUTDOWN_CLIENT;
        }

        //return StreamThreadExceptionResponse.REPLACE_THREAD;
        return StreamThreadExceptionResponse.SHUTDOWN_APPLICATION;
    }
}
