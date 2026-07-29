# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk-jammy AS package
WORKDIR /build

COPY --chmod=0755 gradlew gradlew
COPY gradle gradle
COPY settings.gradle build.gradle ./
RUN --mount=type=cache,target=/root/.gradle ./gradlew dependencies --no-daemon

COPY src ./src
RUN --mount=type=cache,target=/root/.gradle ./gradlew clean bootJar --no-daemon -x test

FROM package AS extract
WORKDIR /build
RUN java -Djarmode=tools -jar build/libs/app.jar extract --layers --destination extracted

FROM eclipse-temurin:21-jre-jammy AS final
WORKDIR /application
ARG UID=10001
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser
USER appuser

COPY --from=extract build/extracted/dependencies/ ./
COPY --from=extract build/extracted/spring-boot-loader/ ./
COPY --from=extract build/extracted/snapshot-dependencies/ ./
COPY --from=extract build/extracted/application/ ./

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
