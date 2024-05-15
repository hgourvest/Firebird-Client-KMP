enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Firebird-Client-KMP"
include(":native")
include(":FirebirdClient")
include(":FirebirdClient-ext")

project(":FirebirdClient").projectDir = file("library")
project(":FirebirdClient-ext").projectDir = file("library-ext")
