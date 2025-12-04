plugins {
    id("graceon.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.graceon.data"
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(project(":core:core-network"))
    implementation(project(":domain"))
    
    implementation(libs.ktor.client.core)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
}
