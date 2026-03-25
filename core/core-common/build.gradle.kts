plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("graceon.android.library")
}

kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.core.ktx)
        }
    }
}

android {
    namespace = "com.graceon.core.common"
}
