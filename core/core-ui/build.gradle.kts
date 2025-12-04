plugins {
    id("graceon.android.library")
    id("graceon.android.compose")
}

android {
    namespace = "com.graceon.core.ui"
}

dependencies {
    implementation(project(":core:core-common"))
    
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
}
