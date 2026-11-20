package com.example.dating

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import javax.sql.DataSource

@SpringBootTest
@Import(TestcontainersConfiguration::class)
class DatingApplicationTests {
    @Autowired
    private lateinit var dataSource: DataSource

    @Test
    fun contextLoads() {
    }

    @Test
    fun `tests run against a throwaway container, never a local database`() {
        dataSource.connection.use { connection ->
            assertThat(connection.metaData.url)
                .describedAs(
                    "A @SpringBootTest that forgets @Import(TestcontainersConfiguration::class) falls back to " +
                        "application.yaml's local database, which compose.yaml also starts.",
                ).doesNotContain(":5432/")
        }
    }
}
