pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven( url="https://jitpack.io" )
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven( url="https://jitpack.io" )
        maven(url = "https://github.com/jitsi/jitsi-maven-repository/raw/master/releases")
    }
}

include(":app")
