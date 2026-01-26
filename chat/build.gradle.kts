plugins {
    /**
     * "java-library"는 gradle에게 다른 모듈에게 기능을 제공하는 library 모듈임을 명시
     * main function의 entryPoint 혹은 실행할 수 있는(executable) 모듈이 아님
     */
    id("java-library")
    id("dodotalk.spring-boot-service")
    kotlin("plugin.jpa")
}

group = "com.dothebestmayb"
version = "unspecified"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
    implementation(projects.common)

    implementation(libs.spring.boot.starter.validation)

    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.postgresql)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}