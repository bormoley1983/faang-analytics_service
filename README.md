# Analytics Service
Service responsible for managing user achievements, achievement progress tracking, and related business logic.

## Quick start

Prerequisites:
- Java 21+ (JDK)
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
docker build -t achievement-service .
docker run -p 8085:8085 achievement-service
```

## Configuration

Main config: [src/main/resources/application.yaml](src/main/resources/application.yaml)  
Test config: [src/test/resources/application-test.yaml](src/test/resources/application-test.yaml)

## External Integrations

Feign clients:
- [ProjectServiceClient](src/main/java/faang/school/analytics/client/ProjectServiceClient.java) — integration with project service
- Feign configuration: [FeignConfig](src/main/java/faang/school/analytics/client/FeignConfig.java), [FeignUserInterceptor](src/main/java/faang/school/analytics/client/FeignUserInterceptor.java)