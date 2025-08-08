FROM openjdk:21-slim

ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app
COPY build/libs/mople-*.jar app.jar

EXPOSE 8284

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "${JAVA_OPS}", "-Dspring.profiles.active=${PROFILE}", "-jar", "app.jar"]