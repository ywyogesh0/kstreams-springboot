package com.learnkafkastreams.exceptionhandler;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalRestControllerExceptionHandler {

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException illegalArgumentException) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatusCode.valueOf(404),
                illegalArgumentException.getMessage()
        );

        problemDetail.setProperty("additionalInfo", "order types applicable for both windowed/non-windowed state stores");
        return problemDetail;
    }
}
