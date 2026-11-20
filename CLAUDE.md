# CLAUDE.md

A small Spring Boot service where members propose dates to each other. See README.md for the domain and the API.

## Stack

Kotlin 2.4, Spring Boot 4.1 (Web MVC, Data JPA, Validation), Java 25, Gradle 9 (Kotlin DSL). Postgres 18 with
Flyway migrations. OpenAPI Generator for the REST interfaces. JUnit 5, AssertJ, Mockito-Kotlin, MockMvc,
Testcontainers, ArchUnit. ktlint for formatting.

## Commands

- `./gradlew build`: generate, compile, ktlint check and every test. Needs Docker running.
- `./gradlew test --tests '*ProposalFlowTest'`: one test class.
- `./gradlew ktlintFormat`: fix formatting. Run it before committing.
- `./gradlew bootRun`: run locally on port 8080; Postgres starts from `compose.yaml`.

## Layout

Root package `com.example.dating`. Each subdomain is a top-level package with three layers:

```
{subdomain}/
  domain/                 entities, value objects, repository interfaces
    exception/            one file per domain exception
  application/
    command/{usecase}/    Command data class + CommandHandler
    query/{usecase}/      Query data class + QueryHandler
  infrastructure/
    controller/           REST controllers implementing the generated *Api interfaces, plus mappers
```

- `date/`: the `DateProposal` aggregate and its history.
- `common/`: code more than one subdomain needs: `Member`, `DomainException`, the `Clock` bean.
- Generated code lands in `com.example.dating.api` and `com.example.dating.schema` under `build/`; never edit it.

## Conventions

- **Layers.** `infrastructure` → `application` → `domain`, never the reverse. `ArchitectureTest` enforces it.
  JPA annotations on domain entities and `JpaRepository` interfaces in `domain` are fine.
- **Aggregates own the rules.** A command handler loads, calls one method on the aggregate, and saves. It makes
  no decision itself. Each validation is its own private function (`validateIsInvitee(...)`).
- **Transactions.** Command handlers that write are annotated `@Transactional`; query handlers use
  `@Transactional(readOnly = true)`.
- **Errors.** Each business rule has its own exception in `domain/exception/`, extending
  `common.domain.exception.DomainException`, carrying the data that explains the failure, and annotated with its
  `@ResponseStatus`. Never `require`/`check` or `IllegalArgumentException`. The body is Spring Boot's
  `{timestamp, status, error, path}`.
- **OpenAPI first.** Edit `src/main/resources/openapi/dating.yaml`, build to regenerate, then implement the
  generated interface. Don't write controller signatures by hand.
- **Migrations.** Add a new `V{n}__description.sql` under `src/main/resources/db/migration`; never edit an
  existing one. Hibernate validates the schema on startup (`ddl-auto: validate`).
- **Time.** Inject `java.time.Clock`; never call `Instant.now()` in production code.
- **No speculative abstractions.** Add a helper, interface or dependency only when something needs it now.

## Testing

- Domain and handler tests are plain unit tests; mock repositories with Mockito-Kotlin. Arrange / Act / Assert.
- Repository tests use `@DataJpaTest` with `@AutoConfigureTestDatabase(replace = NONE)` and
  `@Import(TestcontainersConfiguration::class)`.
- End-to-end tests use `@SpringBootTest` + `@AutoConfigureMockMvc` + `MockMvcTester`, with the same import.
- Name tests by behaviour, in backticks: `` `the invitee declining a proposal marks it declined` ``.
- The seeded members are available as `SeedMembers.ALICE` etc. Tests share one database, so don't assume a
  member has no other proposals.
