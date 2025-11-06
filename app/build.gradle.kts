plugins {
    id("dodotalk.spring-boot-app")
}

group = "com.dothebestmayb"
version = "0.0.1-SNAPSHOT"
description = "dodotalk-backend"

dependencies {
    implementation(projects.user)
    implementation(projects.chat)
    implementation(projects.notification)
    implementation(projects.common)

    implementation(libs.spring.boot.starter.security)

    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.postgresql)
}