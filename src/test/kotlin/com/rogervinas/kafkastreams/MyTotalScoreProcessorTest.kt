package com.rogervinas.kafkastreams

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TestInputTopic
import org.apache.kafka.streams.TestOutputTopic
import org.apache.kafka.streams.TopologyTestDriver
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerde
import java.time.Duration
import java.util.Properties

private const val TOPIC_IN = "topic.in"
private const val TOPIC_OUT = "topic.out"

private const val USERNAME_1 = "user1"
private const val USERNAME_2 = "user2"
private val TOTAL_SCORE_WINDOW = Duration.ofSeconds(15)

internal class MyTotalScoreProcessorTest {

  private lateinit var topologyTestDriver: TopologyTestDriver
  private lateinit var topicIn: TestInputTopic<String, ScoreEvent>
  private lateinit var topicOut: TestOutputTopic<String, TotalScoreEvent>

  @BeforeEach
  fun beforeEach() {
    val stringSerde = Serdes.StringSerde()
    val streamsBuilder = StreamsBuilder()

    MyTotalScoreProcessor(TOTAL_SCORE_WINDOW)
      .apply(streamsBuilder.stream(TOPIC_IN))
      .to(TOPIC_OUT)

    val config = Properties().apply {
      setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, stringSerde.javaClass.name)
      setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde::class.java.name)
      setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "test")
      setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "test-server")
      setProperty(JsonDeserializer.TRUSTED_PACKAGES, "*")
    }
    val topology = streamsBuilder.build()
    topologyTestDriver = TopologyTestDriver(topology, config)
    topicIn = topologyTestDriver.createInputTopic(TOPIC_IN, stringSerde.serializer(), JsonSerde(ScoreEvent::class.java).serializer())
    topicOut = topologyTestDriver.createOutputTopic(TOPIC_OUT, stringSerde.deserializer(), JsonSerde(TotalScoreEvent::class.java).deserializer())
  }

  @AfterEach
  fun afterEach() {
    topologyTestDriver.close()
  }

  @Test
  fun `should publish total score of one username when window expires`() {
    topicIn.pipeInput(USERNAME_1, ScoreEvent(25))
    topicIn.pipeInput(USERNAME_1, ScoreEvent(37))
    topicIn.pipeInput(USERNAME_1, ScoreEvent(13))

    topicIn.advanceTime(TOTAL_SCORE_WINDOW.plusMillis(100))

    // Send at least one more message so the previous window is closed
    topicIn.pipeInput(USERNAME_1, ScoreEvent(1))

    assertThat(topicOut.readKeyValuesToList()).singleElement().satisfies { topicOutMessage ->
      assertThat(topicOutMessage.key).isEqualTo(USERNAME_1)
      assertThat(topicOutMessage.value).isEqualTo(TotalScoreEvent(75))
    }
  }

  @Test
  fun `should publish total score grouped by username when window expires`() {
    topicIn.pipeInput(USERNAME_1, ScoreEvent(25))
    topicIn.pipeInput(USERNAME_2, ScoreEvent(18))
    topicIn.pipeInput(USERNAME_1, ScoreEvent(37))
    topicIn.pipeInput(USERNAME_2, ScoreEvent(23))
    topicIn.pipeInput(USERNAME_1, ScoreEvent(13))
    topicIn.pipeInput(USERNAME_2, ScoreEvent(45))

    topicIn.advanceTime(TOTAL_SCORE_WINDOW.plusMillis(100))

    // Send at least one more message so the previous window is closed
    topicIn.pipeInput(USERNAME_1, ScoreEvent(1))
    topicIn.pipeInput(USERNAME_2, ScoreEvent(1))

    assertThat(topicOut.queueSize).isEqualTo(2)
    assertThat(topicOut.readKeyValuesToMap()).satisfies { topicOutMessages ->
      assertThat(topicOutMessages[USERNAME_1]).isEqualTo(TotalScoreEvent(75))
      assertThat(topicOutMessages[USERNAME_2]).isEqualTo(TotalScoreEvent(86))
    }
  }

  @Test
  fun `should publish total score grouped by username every time window expires`() {
    topicIn.pipeInput(USERNAME_1, ScoreEvent(25))
    topicIn.pipeInput(USERNAME_2, ScoreEvent(18))
    topicIn.pipeInput(USERNAME_1, ScoreEvent(37))
    topicIn.pipeInput(USERNAME_2, ScoreEvent(23))
    topicIn.pipeInput(USERNAME_1, ScoreEvent(13))
    topicIn.pipeInput(USERNAME_2, ScoreEvent(45))

    topicIn.advanceTime(TOTAL_SCORE_WINDOW.plusMillis(100))

    topicIn.pipeInput(USERNAME_1, ScoreEvent(28))
    topicIn.pipeInput(USERNAME_2, ScoreEvent(21))
    topicIn.pipeInput(USERNAME_1, ScoreEvent(40))
    topicIn.pipeInput(USERNAME_2, ScoreEvent(26))
    topicIn.pipeInput(USERNAME_1, ScoreEvent(16))
    topicIn.pipeInput(USERNAME_2, ScoreEvent(48))

    assertThat(topicOut.queueSize).isEqualTo(2)
    assertThat(topicOut.readKeyValuesToMap()).satisfies { topicOutMessages ->
      assertThat(topicOutMessages[USERNAME_1]).isEqualTo(TotalScoreEvent(75))
      assertThat(topicOutMessages[USERNAME_2]).isEqualTo(TotalScoreEvent(86))
    }

    topicIn.advanceTime(TOTAL_SCORE_WINDOW.plusMillis(100))

    // Send at least one more message so the previous window is closed
    topicIn.pipeInput(USERNAME_1, ScoreEvent(1))
    topicIn.pipeInput(USERNAME_2, ScoreEvent(1))

    assertThat(topicOut.queueSize).isEqualTo(2)
    assertThat(topicOut.readKeyValuesToMap()).satisfies { topicOutMessages ->
      assertThat(topicOutMessages[USERNAME_1]).isEqualTo(TotalScoreEvent(84))
      assertThat(topicOutMessages[USERNAME_2]).isEqualTo(TotalScoreEvent(95))
    }
  }
}
