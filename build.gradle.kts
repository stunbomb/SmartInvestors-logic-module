import com.android.build.gradle.internal.utils.isKotlinPluginAppliedInTheSameClassloader
import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application") version "8.5.2"
    id("org.jetbrains.kotlin.multiplatform") version "2.1.10"
    id("org.jetbrains.compose") version "1.7.3"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.10"
    id("androidx.room") version "2.7.0-rc03"
    id("com.google.devtools.ksp") version "2.1.10-1.0.29"
    kotlin("plugin.serialization") version "2.1.0"
}

kotlin {
    androidTarget {
        @OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "logic"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.compose.runtime:runtime")
                implementation("org.jetbrains.compose.foundation:foundation")
                implementation("org.jetbrains.compose.material3:material3")
                implementation("org.jetbrains.compose.ui:ui")
                implementation("org.jetbrains.compose.components:components-resources")
                implementation("org.jetbrains.compose.components:components-uitooling-preview")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
                implementation("io.ktor:ktor-client-core:3.0.2")
                implementation("io.ktor:ktor-client-json:3.0.2")
                implementation("io.ktor:ktor-client-serialization:3.0.2")
                implementation("io.ktor:ktor-client-logging:3.0.2")
                implementation("io.ktor:ktor-client-content-negotiation:3.0.2")

                implementation("io.insert-koin:koin-core:4.0.0")
                implementation("io.insert-koin:koin-compose:4.0.0")
                implementation("io.insert-koin:koin-compose-viewmodel:4.0.0")

                implementation("androidx.room:room-runtime:2.7.0-rc03")
                implementation("androidx.sqlite:sqlite-bundled:2.5.0-rc03")

                implementation("io.github.jan-tennert.supabase:auth-kt:3.0.3")
                implementation("io.github.jan-tennert.supabase:postgrest-kt:3.0.3")
                implementation("io.github.jan-tennert.supabase:storage-kt:3.0.3")
                implementation("io.github.jan-tennert.supabase:realtime-kt:3.0.3")

                implementation("com.revenuecat.purchases:purchases-kmp-core:1.7.3+13.26.1")
                implementation("com.revenuecat.purchases:purchases-kmp-datetime:1.7.3+13.26.1")
                implementation("com.revenuecat.purchases:purchases-kmp-either:1.7.3+13.26.1")
                implementation("com.revenuecat.purchases:purchases-kmp-result:1.7.3+13.26.1")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("androidx.activity:activity-compose:1.10.1")
                implementation("io.ktor:ktor-client-cio:3.0.2")
                implementation("io.ktor:ktor-client-gson:3.0.2")
                implementation("io.ktor:ktor-client-okhttp:3.0.2")
                implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.4")
                implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
            }
        }

        val iosMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:3.0.2")
            }
        }
    }
}

android {
    namespace = "com.dblhargrove"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dblhargrove"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    debugImplementation("org.jetbrains.compose.ui:ui-tooling")
    implementation("androidx.work:work-runtime-ktx:2.10.0")
}

room {
    schemaDirectory("$projectDir/schemas")
}

    schemaDirectory("$projectDir/schemas")
}
