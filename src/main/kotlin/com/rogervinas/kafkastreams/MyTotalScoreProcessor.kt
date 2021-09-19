package com.rogervinas.kafkastreams

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Suppressed
import org.apache.kafka.streams.kstream.Suppressed.BufferConfig.unbounded
import org.apache.kafka.streams.kstream.TimeWindows
import org.apache.kafka.streams.state.WindowStore
import org.springframework.kafka.support.serializer.JsonSerde
import java.time.Duration
import java.util.function.Function

data class ScoreEvent(val score: Int)
data class TotalScoreEvent(val totalScore: Int)

class MyTotalScoreProcessor(private val totalScoreWindow: Duration) :
  Function<KStream<String, ScoreEvent>, KStream<String, TotalScoreEvent>> {

  override fun apply(input: KStream<String, ScoreEvent>): KStream<String, TotalScoreEvent> {
    return input
      .groupByKey()
      .windowedBy(TimeWindows.of(totalScoreWindow).grace(Duration.ofSeconds(0)))
      .aggregate(
        { TotalScoreEvent(0) },
        { _, scoreEvent, totalScoreEvent -> TotalScoreEvent(scoreEvent.score + totalScoreEvent.totalScore) },
        Materialized.`as`<String?, TotalScoreEvent?, WindowStore<Bytes, ByteArray>?>("total-score")
          .withKeySerde(Serdes.StringSerde())
          .withValueSerde(JsonSerde(TotalScoreEvent::class.java))
      )
      .suppress(Suppressed.untilWindowCloses(unbounded()))
      .toStream()
      .map { key, value -> KeyValue(key.key(), value) }
  }
}
