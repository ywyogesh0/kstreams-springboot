package com.learnkafkastreams.producer;

import com.learnkafkastreams.utils.Constant;
import lombok.extern.slf4j.Slf4j;

import static com.learnkafkastreams.producer.ProducerUtil.publishMessageSync;

@Slf4j
public class WordsProducer {

    public static void main(String[] args) throws InterruptedException {

        var key = "A";

        var word = "Apple";
        var word1 = "Alligator";
        var word2 = "Ambulance";

        var recordMetaData = publishMessageSync(Constant.TOPIC_WORDS_CONSUMER, key,word);
        log.info("Published the alphabet message : {} ", recordMetaData);

        var recordMetaData1 = publishMessageSync(Constant.TOPIC_WORDS_CONSUMER, key,word1);
        log.info("Published the alphabet message : {} ", recordMetaData1);

        var recordMetaData2 = publishMessageSync(Constant.TOPIC_WORDS_CONSUMER, key,word2);
        log.info("Published the alphabet message : {} ", recordMetaData2);

        var bKey = "B";

        var bWord1 = "Bus";
        var bWord2 = "Baby";
        var recordMetaData3 = publishMessageSync(Constant.TOPIC_WORDS_CONSUMER, bKey,bWord1);
        log.info("Published the alphabet message : {} ", recordMetaData2);

        var recordMetaData4 = publishMessageSync(Constant.TOPIC_WORDS_CONSUMER, bKey,bWord2);
        log.info("Published the alphabet message : {} ", recordMetaData2);

    }
}
