package com.rogervinas.kafkastreams.helper

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties

class KafkaProducerHelper(bootstrapServers: String) {
  private val producer: Producer<String, String>

  init {
    val config = Properties()
    config[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
    config[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
    config[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
    producer = KafkaProducer(config)
  }

  @Throws(Exception::class)
  fun send(
    topic: String?,
    key: String,
    body: String,
  ) {
    producer.send(ProducerRecord(topic, key, body)).get()
    producer.flush()
  }
}
