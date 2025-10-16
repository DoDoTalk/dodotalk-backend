plugins {
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

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}