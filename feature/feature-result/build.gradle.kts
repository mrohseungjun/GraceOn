plugins {
    id("graceon.android.feature")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.graceon.feature.result"
}

dependencies {
    implementation(project(":data"))
    implementation(project(":core:core-common"))
    implementation(libs.kotlinx.serialization.json)
}
