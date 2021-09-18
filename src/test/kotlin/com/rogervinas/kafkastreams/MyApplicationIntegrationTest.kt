package com.rogervinas.kafkastreams

import com.rogervinas.kafkastreams.helper.DockerComposeContainerHelper
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@Testcontainers
class MyApplicationIntegrationTest {

  @Container
  val container = DockerComposeContainerHelper().createContainer()

  @Test
  fun `should load application`() {
  }
}
