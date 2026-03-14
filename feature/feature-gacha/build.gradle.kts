import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    id("graceon.compose.multiplatform")
    id("graceon.android.compose")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:core-ui"))
            implementation(project(":core:core-common"))
            implementation(project(":core:core-network"))
            implementation(project(":domain"))
            implementation(project(":data"))

            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

android {
    namespace = "com.graceon.feature.gacha"
    compileSdk = 36
    defaultConfig {
        minSdk = 29
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
