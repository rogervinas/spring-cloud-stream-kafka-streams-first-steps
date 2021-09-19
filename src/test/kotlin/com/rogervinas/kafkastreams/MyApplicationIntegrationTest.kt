package com.rogervinas.kafkastreams

import com.rogervinas.kafkastreams.helper.DockerComposeContainerHelper
import com.rogervinas.kafkastreams.helper.KafkaConsumerHelper
import com.rogervinas.kafkastreams.helper.KafkaProducerHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration

private const val TOPIC_SCORES = "pub.scores"
private const val TOPIC_TOTALS = "pub.totals"

private const val USERNAME_1 = "user1"
private const val USERNAME_2 = "user2"

@SpringBootTest
@Testcontainers
internal class MyApplicationIntegrationTest {

  @Container
  val container = DockerComposeContainerHelper().createContainer()

  @Value("\${spring.cloud.stream.kafka.streams.binder.brokers}")
  lateinit var kafkaBroker: String
  lateinit var kafkaProducerHelper: KafkaProducerHelper
  lateinit var kafkaConsumerHelper: KafkaConsumerHelper

  @Value("\${total-score.window}")
  lateinit var totalScoreWindow: Duration

  @BeforeEach
  fun beforeEach() {
    kafkaProducerHelper = KafkaProducerHelper(kafkaBroker)
    kafkaConsumerHelper = KafkaConsumerHelper(kafkaBroker, TOPIC_TOTALS)
    kafkaConsumerHelper.consumeAll()
  }

  @Test
  fun `should publish total scores`() {
    kafkaProducerHelper.send(TOPIC_SCORES, USERNAME_1, "{\"score\": 10}")
    kafkaProducerHelper.send(TOPIC_SCORES, USERNAME_2, "{\"score\": 20}")
    kafkaProducerHelper.send(TOPIC_SCORES, USERNAME_1, "{\"score\": 30}")
    kafkaProducerHelper.send(TOPIC_SCORES, USERNAME_2, "{\"score\": 40}")
    kafkaProducerHelper.send(TOPIC_SCORES, USERNAME_1, "{\"score\": 50}")
    kafkaProducerHelper.send(TOPIC_SCORES, USERNAME_2, "{\"score\": 60}")

    Thread.sleep(totalScoreWindow.plusSeconds(1).toMillis())

    // Send at least one more message so the previous window is closed
    kafkaProducerHelper.send(TOPIC_SCORES, USERNAME_1, "{\"score\": 1}")
    kafkaProducerHelper.send(TOPIC_SCORES, USERNAME_2, "{\"score\": 1}")

    val records = kafkaConsumerHelper.consumeAtLeast(2, Duration.ofSeconds(30))

    assertThat(records).hasSize(2)
    assertThat(records.associate { record -> record.key() to record.value() }).satisfies { valuesByKey ->
      JSONAssert.assertEquals("{\"totalScore\": 90}", valuesByKey[USERNAME_1], true)
      JSONAssert.assertEquals("{\"totalScore\": 120}", valuesByKey[USERNAME_2], true)
    }
  }
}
