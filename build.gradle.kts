import java.util.Properties

val buildProps = Properties().apply {
    file("gradle.properties")
        .takeIf { it.exists() }
        ?.inputStream()
        ?.use(::load)
}

val projectGroup: String = buildProps.getProperty("projectGroup")
val applicationVersion: String = buildProps.getProperty("applicationVersion")

val springCloudVersion: String = buildProps.getProperty("springCloudVersion")

val bouncyCastleVersion: String = buildProps.getProperty("bouncyCastleVersion")
val jwtVersion: String = buildProps.getProperty("jwtVersion")
val okHttpVersion: String = buildProps.getProperty("okHttpVersion")
val awsVersion: String = buildProps.getProperty("awsVersion")
val xmlBindVersion: String = buildProps.getProperty("xmlBindVersion")
val persistenceVersion: String = buildProps.getProperty("persistenceVersion")
val mapperVersion: String = buildProps.getProperty("mapperVersion")
val lombokMapperVersion: String = buildProps.getProperty("lombokMapperVersion")
val swaggerVersion: String = buildProps.getProperty("swaggerVersion")
val queryDslVersion: String = buildProps.getProperty("queryDslVersion")
val datasourceProxyVersion: String = buildProps.getProperty("datasourceProxyVersion")
val logbackEncoderVersion: String = buildProps.getProperty("logbackEncoderVersion")
val firebaseVersion: String = buildProps.getProperty("firebaseVersion")
val dataFakerVersion: String = buildProps.getProperty("dataFakerVersion")
val fixtureMonkeyVersion: String = buildProps.getProperty("fixtureMonkeyVersion")

val mockitoAgent: Configuration by configurations.creating

plugins {
    id("java")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = projectGroup
version = applicationVersion

java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}")
    }
}

dependencies {
    // spring
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // security
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.bouncycastle:bcpkix-jdk18on:${bouncyCastleVersion}")

    // jwt
    implementation("io.jsonwebtoken:jjwt-api:${jwtVersion}")
    implementation("io.jsonwebtoken:jjwt-impl:${jwtVersion}")
    implementation("io.jsonwebtoken:jjwt-jackson:${jwtVersion}")

    // ssr - link
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect")

    // client
    implementation("com.squareup.okhttp3:okhttp:${okHttpVersion}")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    // s3 - profile image
    implementation(platform("io.awspring.cloud:spring-cloud-aws-dependencies:${awsVersion}"))
    implementation("io.awspring.cloud:spring-cloud-aws-starter-s3")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:${xmlBindVersion}")

    // lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // db
    runtimeOnly("org.postgresql:postgresql")

    // json - db Mapping
    implementation("io.hypersistence:hypersistence-utils-hibernate-63:${persistenceVersion}")

    // object mapping
    // binding library를 통해 lombok 순서 상관없이 사용가능
    implementation("org.mapstruct:mapstruct:${mapperVersion}")
    annotationProcessor("org.mapstruct:mapstruct-processor:${mapperVersion}")
    implementation("org.projectlombok:lombok-mapstruct-binding:${lombokMapperVersion}")

    // swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${swaggerVersion}")

    // jpa
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // querydsl/queryDsl -> openfeign/queryDsl
    // query performence 고려 -> (mybatis, jooq)
    implementation("io.github.openfeign.querydsl:querydsl-jpa:${queryDslVersion}")
    // openfeign qclass 생성 문제 (:jpa 추가)
    annotationProcessor("io.github.openfeign.querydsl:querydsl-apt:${queryDslVersion}:jpa")

    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    // property assist tool
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Datasource Proxy (슬로우 쿼리 측정하기 위해)
    implementation("net.ttddyy:datasource-proxy:${datasourceProxyVersion}")

    // Logstash Logback Encoder (로그를 JSON 형태로 변환)
    implementation("net.logstash.logback:logstash-logback-encoder:${logbackEncoderVersion}")

    // firebase
    // io.netty 4.1.115 미만 windows dos 공격 취약점 spring netty 4.1.115 이상 지원할 때 변경 - spring 3.4.0에서 해결
    implementation("com.google.firebase:firebase-admin:${firebaseVersion}")

    // dummy data
    implementation("net.datafaker:datafaker:${dataFakerVersion}")

    // test
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter:${fixtureMonkeyVersion}")

    // Mockito
    mockitoAgent(libs.mockito) { isTransitive = false }
    testImplementation(libs.mockito)
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-javaagent:${mockitoAgent.asPath}", "-Xshare:off")
}