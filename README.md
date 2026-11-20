# Dating

A small Kotlin / Spring Boot service where members propose dates to each other.

## Requirements

- Docker (running). Tests start Postgres with Testcontainers; `bootRun` starts it with Docker Compose.
- JDK 25. If it is not installed, Gradle downloads it on the first build.

## Build and test

```bash
./gradlew build
```

One command compiles, generates the API interfaces from the OpenAPI spec, checks formatting and runs every test.

Other useful commands:

```bash
./gradlew test            # tests only
./gradlew ktlintFormat    # fix formatting
./gradlew bootRun         # run on http://localhost:8080 (starts Postgres from compose.yaml)
```

## Domain

- **Member**: someone who can propose and receive dates. Five members are seeded by
  `V2__seed_members.sql`:

  | Name    | Id                                     |
  |---------|----------------------------------------|
  | Alice   | `00000000-0000-0000-0000-00000000000a` |
  | Bob     | `00000000-0000-0000-0000-00000000000b` |
  | Charlie | `00000000-0000-0000-0000-00000000000c` |
  | Dana    | `00000000-0000-0000-0000-00000000000d` |
  | Eve     | `00000000-0000-0000-0000-00000000000e` |

- **DateProposal**: a proposer invites an invitee to a date at a proposed time. It starts out `PROPOSED`.
  - The invitee can **accept** or **decline** it.
  - The proposer can **cancel** it, before or after it was accepted.
  - Every change is also written to `date_proposal_history`.

## API

The contract is `src/main/resources/openapi/dating.yaml`.

| Method | Path                       | Body                                       |
|--------|----------------------------|--------------------------------------------|
| POST   | `/proposals`               | `{proposerId, inviteeId, proposedTime}`    |
| POST   | `/proposals/{id}/accept`   | `{memberId}`, the invitee                  |
| POST   | `/proposals/{id}/decline`  | `{memberId}`, the invitee                  |
| POST   | `/proposals/{id}/cancel`   | `{memberId}`, the proposer                 |
| GET    | `/members/{id}/proposals`  |                                            |

```bash
curl -s -X POST localhost:8080/proposals -H 'Content-Type: application/json' \
  -d '{"proposerId":"00000000-0000-0000-0000-00000000000a","inviteeId":"00000000-0000-0000-0000-00000000000b","proposedTime":"2030-06-01T19:00:00Z"}'

curl -s -X POST localhost:8080/proposals/<id>/accept -H 'Content-Type: application/json' \
  -d '{"memberId":"00000000-0000-0000-0000-00000000000b"}' -o /dev/null -w '%{http_code}\n'

curl -s localhost:8080/members/00000000-0000-0000-0000-00000000000b/proposals
```

Errors answer with Spring Boot's standard body, `{timestamp, status, error, path}`.

## Conventions

See [CLAUDE.md](CLAUDE.md). In short:

- Three layers per subdomain, `domain` ← `application` ← `infrastructure`; dependencies point inwards.
- Business rules live on the aggregate. Handlers load, call one method, save.
- One exception class per business rule, extending `DomainException`, with its own `@ResponseStatus`.
- OpenAPI first: change the spec, let Gradle regenerate, implement the generated interface.
- Schema changes are new Flyway migrations; never edit one that already exists.
- Run `./gradlew ktlintFormat` before committing.
