package com.learnkafkastreams.producer;

import static com.learnkafkastreams.utils.Constants.TOPIC_AGGREGATE;

import lombok.extern.slf4j.Slf4j;

import static com.learnkafkastreams.producer.ProducerUtil.publishMessageSync;

@Slf4j
public class AggregateProducer {

    public static void main(String[] args) throws InterruptedException {

        var key = "A";

        var word = "Apple";
        var word1 = "Alligator";
        var word2 = "Ambulance";

        var recordMetaData = publishMessageSync(TOPIC_AGGREGATE, key, word);
        log.info("Published the alphabet message : {} ", recordMetaData);

        var recordMetaData1 = publishMessageSync(TOPIC_AGGREGATE, key, word1);
        log.info("Published the alphabet message : {} ", recordMetaData1);

        var recordMetaData2 = publishMessageSync(TOPIC_AGGREGATE, key, word2);
        log.info("Published the alphabet message : {} ", recordMetaData2);

        var recordMetaData3 = publishMessageSync(TOPIC_AGGREGATE, key, null);
        log.info("Published the alphabet message : {} ", recordMetaData3);

        var bKey = "B";

        var bWord1 = "Bus";
        var bWord2 = "Baby";

        var recordMetaData4 = publishMessageSync(TOPIC_AGGREGATE, bKey, bWord1);
        log.info("Published the alphabet message : {} ", recordMetaData4);

        var recordMetaData5 = publishMessageSync(TOPIC_AGGREGATE, bKey, bWord2);
        log.info("Published the alphabet message : {} ", recordMetaData5);

        var cWord1 = "cBus";
        var cWord2 = "cBaby";

        var recordMetaData6 = publishMessageSync(TOPIC_AGGREGATE, null, cWord1);
        log.info("Published the alphabet message : {} ", recordMetaData6);

        var recordMetaData7 = publishMessageSync(TOPIC_AGGREGATE, null, cWord2);
        log.info("Published the alphabet message : {} ", recordMetaData7);
    }
}
