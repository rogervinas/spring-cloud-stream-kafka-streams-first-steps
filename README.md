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

This demo has been created using this [spring initializr configuration](https://start.spring.io/#!type=gradle-project&language=kotlin&platformVersion=2.5.4&packaging=jar&jvmVersion=11&groupId=com.rogervinas&artifactId=springcloudstreamkafkastreamsbinder&name=springcloudstreamkafkastreamsbinder&description=Spring%20Cloud%20Streams%20%26%20Kafka%20Streams%20Binder&packageName=com.rogervinas.springcloudstreamkafkastreamsbinder&dependencies=cloud-stream) adding Kafka binder dependency `spring-cloud-stream-binder-kafka-streams`.

## Run

Run with docker-compose:
```shell
docker-compose up -d
./gradlew bootRun
docker-compose down
```

Then you can use [kcat](https://github.com/edenhill/kcat) (formerly know as kafkacat) to produce/consume to/from Kafka:
```shell
# consume
kcat -b localhost:9094 -C -t pub.scores -f '%k %s\n'
kcat -b localhost:9094 -C -t pub.totals -f '%k %s\n'

# produce
echo 'john:{"score":100}' | kcat -b localhost:9094 -P -t pub.scores -K:
```

That's it! Happy coding!