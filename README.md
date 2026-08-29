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