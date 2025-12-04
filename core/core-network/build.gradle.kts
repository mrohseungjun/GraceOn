plugins {
    id("graceon.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.graceon.core.network"
}

dependencies {
    implementation(project(":core:core-common"))
    
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
}
