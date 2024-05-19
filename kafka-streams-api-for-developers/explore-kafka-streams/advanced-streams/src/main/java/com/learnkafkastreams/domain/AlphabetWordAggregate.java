package com.learnkafkastreams.domain;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public record AlphabetWordAggregate(String key, List<String> valueList, int runningCount) {

    public AlphabetWordAggregate updateNewEvents(String key, String newValue) {
        log.info("Before Update: {}", this);
        log.info("key: {}, new value: {}", key, newValue);
        valueList.add(newValue);
        var alphabetWordAggregate = new AlphabetWordAggregate(key, valueList, runningCount + 1);
        log.info("After Update: {}", alphabetWordAggregate);
        return alphabetWordAggregate;
    }
}


