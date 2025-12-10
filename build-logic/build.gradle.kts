plugins {
    `kotlin-dsl` // build specific module임을 명시
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
    // version catalog를 사용할 수 없음
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.10")
    implementation("org.jetbrains.kotlin:kotlin-allopen:2.2.10")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:4.0.0-SNAPSHOT")
    implementation("io.spring.gradle:dependency-management-plugin:1.1.7")
}