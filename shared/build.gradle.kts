import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework { baseName = "shared"; isStatic = true }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel:2.8.4")
            implementation("io.ktor:ktor-client-core:${libs.versions.ktor.get()}")
            implementation("io.ktor:ktor-client-content-negotiation:${libs.versions.ktor.get()}")
            implementation("io.ktor:ktor-serialization-kotlinx-json:${libs.versions.ktor.get()}")
            implementation("io.ktor:ktor-client-logging:${libs.versions.ktor.get()}")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
            implementation("io.insert-koin:koin-core:4.0.2")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("io.ktor:ktor-client-mock:${libs.versions.ktor.get()}")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
        }
        androidMain.dependencies {
            implementation("androidx.activity:activity-compose:1.10.0")
            implementation("io.ktor:ktor-client-okhttp:${libs.versions.ktor.get()}")
        }
        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:${libs.versions.ktor.get()}")
        }
    }
}

android {
    namespace = "pe.edu.upeu.pharmamobil"
    compileSdk = 35

    defaultConfig {
        applicationId = "pe.edu.upeu.pharmamobil"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
