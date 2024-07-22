package com.learnkafkastreams.topology;

import com.learnkafkastreams.domain.Greeting;
import com.learnkafkastreams.serdes.SerdesFactory;
import com.learnkafkastreams.utils.Constant;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GreetingsTopologyTest {

    private TopologyTestDriver topologyTestDriver;
    private TestInputTopic<String, Greeting> inputTopic;
    private TestOutputTopic<String, Greeting> outputTopic;

    @BeforeEach
    void setUp() {
        topologyTestDriver = new TopologyTestDriver(GreetingsTopology.createTopology());
        inputTopic = topologyTestDriver.createInputTopic(
                Constant.TOPIC_GREETINGS_CONSUMER,
                Serdes.String().serializer(),
                SerdesFactory.greetingSerde().serializer()
        );
        outputTopic = topologyTestDriver.createOutputTopic(
                Constant.TOPIC_GREETINGS_PRODUCER,
                Serdes.String().deserializer(),
                SerdesFactory.greetingSerde().deserializer()
        );
    }

    @AfterEach
    void tearDown() {
        if (null != topologyTestDriver) topologyTestDriver.close();
    }

    @Test
    void testTopology_singleInputMessage() {
        inputTopic.pipeKeyValueList(
                List.of(
                        createKeyValue("K1", "A1B1C1")
                )
        );

        assertEquals(1, outputTopic.getQueueSize());

        KeyValue<String, Greeting> outputKeyValue = outputTopic.readKeyValue();
        assertEquals("K1", outputKeyValue.key);
        assertEquals("A1B1C12024", outputKeyValue.value.getMessage());
        assertNotNull(outputKeyValue.value.getTimeStamp());
    }

    @Test
    void testTopology_multipleInputMessage() {
        inputTopic.pipeKeyValueList(
                List.of(
                        createKeyValue("K1", "A1B1C1"),
                        createKeyValue("K2", "D1E1F1")
                )
        );

        assertEquals(2, outputTopic.getQueueSize());

        List<KeyValue<String, Greeting>> keyValueList = outputTopic.readKeyValuesToList();
        assertEquals("K1", keyValueList.get(0).key);
        assertEquals("A1B1C12024", keyValueList.get(0).value.getMessage());
        assertNotNull(keyValueList.get(0).value.getTimeStamp());

        assertEquals("K2", keyValueList.get(1).key);
        assertEquals("D1E1F12024", keyValueList.get(1).value.getMessage());
        assertNotNull(keyValueList.get(1).value.getTimeStamp());
    }

    @Test
    void testTopology_error() {
        inputTopic.pipeKeyValueList(
                List.of(
                        createKeyValue("K1", "transient error")
                )
        );

        assertEquals(0, outputTopic.getQueueSize());
    }

    private KeyValue<String, Greeting> createKeyValue(String key, String message) {
        return KeyValue.pair(
                key,
                Greeting.builder()
                        .message(message)
                        .timeStamp(LocalDateTime.now())
                        .build()
        );
    }
}