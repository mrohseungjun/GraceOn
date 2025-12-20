plugins {
    id("graceon.android.library")
}

android {
    namespace = "com.graceon.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.core.ktx)
}
