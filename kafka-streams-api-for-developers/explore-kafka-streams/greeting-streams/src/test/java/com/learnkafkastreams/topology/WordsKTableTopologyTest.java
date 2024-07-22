package com.learnkafkastreams.topology;


import com.learnkafkastreams.utils.Constant;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WordsKTableTopologyTest {

    private TopologyTestDriver topologyTestDriver;
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, String> outputTopic;

    @BeforeEach
    void setUp() {
        topologyTestDriver = new TopologyTestDriver(WordsKTableTopology.buildTopology());
        inputTopic = topologyTestDriver.createInputTopic(
                Constant.TOPIC_WORDS_CONSUMER,
                Serdes.String().serializer(),
                Serdes.String().serializer()
        );
        outputTopic = topologyTestDriver.createOutputTopic(
                Constant.TOPIC_WORDS_PRODUCER,
                Serdes.String().deserializer(),
                Serdes.String().deserializer()
        );
    }

    @AfterEach
    void tearDown() {
        if (null != topologyTestDriver) topologyTestDriver.close();
    }

    @Test
    void testTopology_singleInputMessage() {
        inputTopic.pipeKeyValueList(List.of(KeyValue.pair("K1", "First Message")));

        assertEquals(1, outputTopic.getQueueSize());

        KeyValue<String, String> outputKeyValue = outputTopic.readKeyValue();
        assertEquals("K1", outputKeyValue.key);
        assertEquals("FIRST MESSAGE", outputKeyValue.value);
    }

    @Test
    void testTopology_multipleInputMessage() {
        inputTopic.pipeKeyValueList(List.of(
                KeyValue.pair("K1", "First Message"),
                KeyValue.pair("K2", "Second Message")
        ));

        assertEquals(2, outputTopic.getQueueSize());

        List<KeyValue<String, String>> keyValueList = outputTopic.readKeyValuesToList();
        assertEquals("K1", keyValueList.get(0).key);
        assertEquals("FIRST MESSAGE", keyValueList.get(0).value);

        assertEquals("K2", keyValueList.get(1).key);
        assertEquals("SECOND MESSAGE", keyValueList.get(1).value);
    }

    @Test
    void testTopology_kTableCachingLimitation() {
        inputTopic.pipeKeyValueList(List.of(
                KeyValue.pair("K1", "First Message"),
                KeyValue.pair("K2", "Second Message"),
                KeyValue.pair("K1", "First-2 Message")
        ));

        assertNotEquals(2, outputTopic.getQueueSize(), "Expected Single K1 & K2 -> Keys");

        List<KeyValue<String, String>> keyValueList = outputTopic.readKeyValuesToList();
        assertEquals("K1", keyValueList.get(0).key);
        assertEquals("FIRST MESSAGE", keyValueList.get(0).value);

        assertEquals("K2", keyValueList.get(1).key);
        assertEquals("SECOND MESSAGE", keyValueList.get(1).value);

        assertEquals("K1", keyValueList.get(2).key);
        assertEquals("FIRST-2 MESSAGE", keyValueList.get(2).value);
    }
}
