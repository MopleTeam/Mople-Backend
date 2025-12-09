val mockitoAgent: Configuration by configurations.creating

plugins {
    alias(libs.plugins.java)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

group = libs.versions.project.group.get()
version = libs.versions.application.version.get()

java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral() // mokito configuration 으로 중복, mockito docs 권장 방식이라 유지
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${libs.versions.spring.cloud.get()}")
        mavenBom("com.oracle.cloud.spring:spring-cloud-oci-dependencies:${libs.versions.oci.get()}")
    }
}

dependencies {
    implementation(libs.bundles.spring.boot.web) // spring

    compileOnly(libs.lombok) // lombok
    annotationProcessor(libs.lombok)

    implementation(libs.bundles.spring.security) // secure
    implementation(libs.bouncy.castle)
    implementation(libs.bundles.jwt)

    implementation(libs.bundles.template) // template

    implementation(libs.spring.cloud.starter.openfeign) // client
    implementation(libs.okhttp)

    implementation(libs.oci.storage) // oci storage

    runtimeOnly(libs.postgresql) // db, orm
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.hypersistence.utils)
    implementation(libs.querydsl.jpa)
    annotationProcessor("${libs.querydsl.apt.get()}:jpa") // openfeign qclass 생성 문제 (:jpa 추가)
    annotationProcessor(libs.jakarta.annotation.api)
    annotationProcessor(libs.jakarta.persistence.api)

    implementation(libs.mapstruct) // mapper
    annotationProcessor(libs.mapstruct.processor)
    implementation(libs.lombok.mapstruct.binding)

    implementation(libs.springdoc.openapi) // docs

    annotationProcessor(libs.spring.boot.configuration.processor) // property assist

    implementation(libs.datasource.proxy) // Datasource Proxy (슬로우 쿼리 측정하기 위해)

    implementation(libs.logstash.logback.encoder) // Logstash Logback Encoder (로그를 JSON 형태로 변환)

    implementation(libs.spring.boot.starter.cache) // 캐시 추상화
    implementation(libs.caffeine)  // Caffeine 로컬 캐시 구현체

    implementation(libs.spring.boot.starter.actuator)  // actuator
    implementation(libs.micrometer.registry.prometheus)  // prometheus

    implementation(libs.firebase.admin) // io.netty 4.1.115 미만 windows dos 공격 취약점 spring netty 4.1.115 이상 지원할 때 변경 - spring 3.4.0에서 해결

    implementation(libs.datafaker) // test dummy

    testRuntimeOnly(libs.junit.platform.launcher) // test
    testImplementation(libs.bundles.test)
    testImplementation(libs.mockito)
    mockitoAgent(libs.mockito) { isTransitive = false }
}

sourceSets {
    main {
        resources {
            srcDirs( "secret/yaml")
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-javaagent:${mockitoAgent.asPath}", "-Xshare:off")
}

tasks.named<Jar>("jar") {
    enabled = false
}

// build -> gradle 일 경우 사용 가능 intellij Idea면 X

//tasks.register<Exec>("updateSubmodules") {
//    commandLine("git", "submodule", "update", "--init", "--recursive")
//}
//
tasks.processResources {
//    dependsOn("updateSubmodules")

    // secret/templates → build/resources/main/templates
    from("secret/templates") {
        into("templates")
    }
}