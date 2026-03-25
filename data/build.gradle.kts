plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("graceon.android.library")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:core-common"))
            implementation(project(":core:core-network"))
            implementation(project(":domain"))

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
        androidMain.dependencies {
            implementation(libs.androidx.datastore.preferences)
        }
    }
}

android {
    namespace = "com.graceon.data"
}
