package com.example.dating

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

/**
 * Postgres for every test that starts an application context. Flyway builds the schema and seeds the members from
 * `db/migration`.
 *
 * One shared container for every context. `stop()` is suppressed because Spring stops container beans when a
 * context closes, which would pull the database out from under every other cached context; Ryuk removes the
 * container when the JVM exits.
 *
 * Keep the image tag in step with `compose.yaml`.
 */
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {
    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer = SHARED

    private companion object {
        private val SHARED =
            object : PostgreSQLContainer(DockerImageName.parse("postgres:18")) {
                override fun stop() = Unit
            }
    }
}
