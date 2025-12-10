pluginManagement {
    includeBuild("build-logic")
    repositories {
        maven { url = uri("https://repo.spring.io/snapshot") }
        gradlePluginPortal()
    }
}

/**
 * dependencies에서 `project("chat")`와 같이 문자열 방식으로 추가하는 것이 아니라
 * `projects.chat`와 같이 typesafe하게 추가할 수 있도록 설정해주는 코드이다.
 */
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "dodotalk"

include("app")
include("user")
include("chat")
include("notification")
include("common")