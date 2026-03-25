plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("graceon.android.library")
    id("graceon.compose.multiplatform")
    id("graceon.android.compose")
}

kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:core-common"))
        }
        androidMain.dependencies {
            implementation(libs.androidx.lifecycle.runtime.ktx)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
        }
    }
}

android {
    namespace = "com.graceon.core.ui"
}
