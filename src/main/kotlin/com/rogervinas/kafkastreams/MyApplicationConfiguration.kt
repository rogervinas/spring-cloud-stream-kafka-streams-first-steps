package com.rogervinas.kafkastreams

import org.apache.kafka.streams.kstream.KStream
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import java.util.function.Function

@Configuration
class MyApplicationConfiguration {

  @Bean
  fun totalScoreProcessor(@Value("\${total-score.window}") totalScoreWindow: Duration):
        Function<KStream<String, ScoreEvent>, KStream<String, TotalScoreEvent>> = MyTotalScoreProcessor(totalScoreWindow)
}
