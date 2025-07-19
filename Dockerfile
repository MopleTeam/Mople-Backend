# CI/CD를 사용하지 않으므로 Docker Image 빌드 시 Gradle Build
FROM gradle:8.11-jdk21 AS builder
WORKDIR /build

# Gradle 파일 변경 시 의존성 다운로드
COPY build.gradle settings.gradle /build/
RUN gradle build -x test --parallel --continue > /dev/null 2>&1 || true

# Image 빌드 - 빌드 속도를 빠르게 하기 위해 Test Pass, 향후 Test 포함할지 결정
COPY . /build
RUN gradle build -x test --parallel && \
    gradle --stop

# Jdk Build - JRE로 Build해도 문제 없지만 기존 익숙한 방법으로 Build
FROM openjdk:21-oraclelinux8

ENV TZ=Asia/Seoul

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /work

# gradle = builder 위에서 build한 jar file 복사 -> /work/groupMeet.jar
COPY --from=builder /build/build/libs/GroupMeeting-0.0.1-SNAPSHOT.jar groupMeet.jar

# 내부 포트는 고정할 것. Docker Port Binding에서 변경해도 무관
EXPOSE 8284

# Option으로 Profile 지정
ENTRYPOINT ["java", "-Dspring.profiles.active=${PROFILE}", "-jar", "groupMeet.jar"]