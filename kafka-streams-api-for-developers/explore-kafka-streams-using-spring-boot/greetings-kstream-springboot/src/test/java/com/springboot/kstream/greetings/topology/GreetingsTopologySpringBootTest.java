package com.springboot.kstream.greetings.topology;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.kstream.greetings.domain.Greeting;
import com.springboot.kstream.greetings.utils.Constants;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext
class GreetingsTopologySpringBootTest {

    @Autowired
    private ObjectMapper objectMapper;

    private TopologyTestDriver topologyTestDriver;
    private TestInputTopic<String, Greeting> inputTopic;
    private TestOutputTopic<String, Greeting> outputTopic;

    @BeforeEach
    void setUp() {
        GreetingTopology greetingTopology = new GreetingTopology(objectMapper);
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        greetingTopology.process(streamsBuilder);

        topologyTestDriver = new TopologyTestDriver(streamsBuilder.build());
        inputTopic = topologyTestDriver.createInputTopic(
                Constants.TOPIC_GREETINGS_CONSUMER,
                Serdes.String().serializer(),
                new JsonSerde<>(Greeting.class, objectMapper).serializer()
        );
        outputTopic = topologyTestDriver.createOutputTopic(
                Constants.TOPIC_GREETINGS_PRODUCER,
                Serdes.String().deserializer(),
                new JsonSerde<>(Greeting.class, objectMapper).deserializer()
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
                        createKeyValue("K1", "a1b1c1")
                )
        );

        assertEquals(1, outputTopic.getQueueSize());

        KeyValue<String, Greeting> outputKeyValue = outputTopic.readKeyValue();
        assertEquals("K1", outputKeyValue.key);
        assertEquals("A1B1C1", outputKeyValue.value.getMessage());
        assertNotNull(outputKeyValue.value.getTimeStamp());
    }

    @Test
    void testTopology_multipleInputMessage() {
        inputTopic.pipeKeyValueList(
                List.of(
                        createKeyValue("K1", "a1b1c1"),
                        createKeyValue("K2", "d1e1f1")
                )
        );

        assertEquals(2, outputTopic.getQueueSize());

        List<KeyValue<String, Greeting>> keyValueList = outputTopic.readKeyValuesToList();
        assertEquals("K1", keyValueList.get(0).key);
        assertEquals("A1B1C1", keyValueList.get(0).value.getMessage());
        assertNotNull(keyValueList.get(0).value.getTimeStamp());

        assertEquals("K2", keyValueList.get(1).key);
        assertEquals("D1E1F1", keyValueList.get(1).value.getMessage());
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