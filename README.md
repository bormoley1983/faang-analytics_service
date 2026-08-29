# Analytics Service
Service responsible for managing user analytics, analytics progress tracking, and related business logic.

## Quick start

Prerequisites:
- Java 25+ (JDK)
- Docker (for container runs)
- [faang-infra services](https://github.com/bormoley1983/faang-infra) running locally or accessible

Run locally:
```sh
./gradlew bootRun
```

Run tests:
```sh
./gradlew test --info
```

Build and run in Docker:
```sh
./gradlew build
docker build -t analytics-service .
docker run -p 8086:8086 analytics-service
```

## Configuration

Main config: [src/main/resources/application.yaml](src/main/resources/application.yaml)  
Test config: [src/test/resources/application-test.yaml](src/test/resources/application-test.yaml)

## External Integrations

Feign clients:
- [ProjectServiceClient](src/main/java/faang/school/analytics/client/ProjectServiceClient.java) — integration with project service
- Feign configuration: [FeignConfig](src/main/java/faang/school/analytics/client/FeignConfig.java), [FeignUserInterceptor](src/main/java/faang/school/analytics/client/FeignUserInterceptor.java)

**Note:** Base code structure and architecture patterns are based on [FAANG School](https://github.com/faang-school) educational project.

## Analytics query API

Both query forms return a bounded page (`content`, `page`, `size`, `totalElements`, `totalPages`):

- `GET /analytics?receiverId=1&eventType=POST_LIKE&page=0&size=20`
- `POST /analytics?page=0&size=20` for a JSON search body

`from` and `to` are UTC instants. Null bounds default to the last three days, `from` must not be after `to`, and page size is limited to 100.

## Event reliability

Analytics events use schema version `1`, carry a stable `eventId`, and store separate `occurredAt` and server-owned `receivedAt` timestamps. Duplicate event IDs are ignored atomically. Invalid timestamps, unsupported contracts, and malformed payloads are sent to `<source-topic>.DLT` after bounded retry.

The canonical like topic is configured in Post and Analytics services by `KAFKA_ANALYTICS_LIKE_TOPIC` and defaults to `analytics_like_topic`. Retry and timestamp limits can be configured with `KAFKA_ANALYTICS_RETRY_MAX_ATTEMPTS`, `KAFKA_ANALYTICS_RETRY_BACKOFF`, `ANALYTICS_MAX_FUTURE_SKEW`, and `ANALYTICS_MAX_EVENT_AGE`.

To replay a reviewed dead-letter topic through the local Kafka container:

```sh
docker compose exec kafka bash /replay-analytics-dlt.sh analytics_comment_topic
```
