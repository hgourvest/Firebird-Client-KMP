plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("maven-publish")
    id("signing")
}

group = "com.progdigy"
version = "1.0-RC"

publishing {
    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }

    publications.withType<MavenPublication> {
        artifact(tasks.register("${name}JavadocJar", Jar::class) {
            archiveClassifier = "javadoc"
            archiveAppendix = this@withType.name
        })
        signing.sign(this)
        pom {
            name = "Firebird Client KMP"
            description = "A Kotlin Multiplatform library for working with a Firebird SQL database."
            url = "https://github.com/hgourvest/Firebird-Client-KMP"
            licenses {
                license {
                    name = "MIT License"
                    url = "https://opensource.org/licenses/MIT"
                }
            }
            developers {
                developer {
                    id = "hgourvest"
                    name = "Henri Gourvest"
                    email = "hgourvest@progdigy.com"
                }
            }
            scm {
                url = "https://github.com/hgourvest/Firebird-Client-KMP.git"
            }
        }
    }
}

kotlin {
    jvm()
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    val hostOs = System.getProperty("os.name")
    val isArm64 = System.getProperty("os.arch") == "aarch64"
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" && isArm64 -> macosArm64()
        hostOs == "Mac OS X" && !isArm64 -> macosX64()
        hostOs == "Linux" && isArm64 -> linuxArm64()
        hostOs == "Linux" && !isArm64 -> linuxX64()
        isMingwX64 -> mingwX64()
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.apply {
        compilations {
            val main by getting {
                cinterops {
                    create("fbclient") {
                        header(file("../native/include/ibase.h"))
                    }
                }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {

        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines)
        }
    }
}

android {
    namespace = "com.progdigy.fbclient"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    externalNativeBuild {
        cmake {
            path = file("../native/CMakeLists.txt")
            version = "3.22.1"
        }
    }


    // Local Android tests are not possible
    testOptions {
        unitTests.all {
            it.enabled = false
        }

    }
}
