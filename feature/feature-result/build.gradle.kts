plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("graceon.android.library")
    id("graceon.compose.multiplatform")
    id("graceon.android.compose")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:core-ui"))
            implementation(project(":core:core-common"))
            implementation(project(":domain"))
            implementation(project(":data"))

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

android {
    namespace = "com.graceon.feature.result"
}
