[![CI](https://github.com/rogervinas/spring-cloud-stream-kafka-streams-first-steps/actions/workflows/gradle.yml/badge.svg)](https://github.com/rogervinas/spring-cloud-stream-kafka-streams-first-steps/actions/workflows/gradle.yml)

# Spring Cloud Stream & Kafka Stream Binder first steps

[Spring Cloud Stream](https://spring.io/projects/spring-cloud-stream) is the solution provided by Spring to build applications connected to shared messaging systems.

It offers an abstraction (the **binding**) that works the same whatever underneath implementation we use (the **binder**):
* **Apache Kafka**
* **Rabbit MQ**
* **Kafka Streams**
* **Amazon Kinesis**
* ...

You can check out [Spring Cloud Stream step by step](https://github.com/rogervinas/spring-cloud-stream-step-by-step) where I got working a simple example using the **Kafka binder**.

Let's try this time a simple example using the **Kafka Streams binder** 🤩!
