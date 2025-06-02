# CI/CD를 사용하지 않으므로 Docker Image 빌드 시 Gradle Build
FROM gradle:8.8-jdk17-alpine AS builder
WORKDIR /build

# Gradle 파일 변경 시 의존성 다운로드
COPY build.gradle settings.gradle /build/
RUN gradle build -x test --parallel --continue > /dev/null 2>&1 || true

# Image 빌드 - 빌드 속도를 빠르게 하기 위해 Test Pass, 향후 Test 포함할지 결정
COPY . /build
RUN gradle build -x test --parallel

# Jdk Build - JRE로 Build해도 문제 없지만 기존 익숙한 방법으로 Build
FROM openjdk:17-alpine

# alpine 이미지는 경량화로 인해 내부 패키지가 거의 없음 - timezone 설정을 위해 아래 명령어 작성
# docker log로 타임존 변경 확인 및 rds timezone 확인 완료
RUN apk --no-cache add tzdata && \
        cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
        echo "Asia/Seoul" > /etc/timezone

WORKDIR /work

# gradle = builder 위에서 build한 jar file 복사 -> /work/groupMeet.jar
COPY --from=builder /build/build/libs/*.jar groupMeet.jar

# 내부 포트는 고정할 것. Docker Port Binding에서 변경해도 무관
EXPOSE 8284

# Option으로 Profile 지정
ENTRYPOINT ["java", "-Dspring.profiles.active=${PROFILE}", "-jar", "groupMeet.jar"]