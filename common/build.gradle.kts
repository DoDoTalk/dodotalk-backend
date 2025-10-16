plugins {
    id("java-library")
    id("dodotalk.kotlin-common")
}

group = "com.dothebestmayb"
version = "unspecified"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}