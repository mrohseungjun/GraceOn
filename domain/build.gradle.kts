plugins {
    id("graceon.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.graceon.domain"
}

dependencies {
    implementation(project(":core:core-common"))
    
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
}
