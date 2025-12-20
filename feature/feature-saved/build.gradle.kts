plugins {
    id("graceon.android.feature")
    id("graceon.android.compose")
}

android {
    namespace = "com.graceon.feature.saved"
}

dependencies {
    implementation(project(":core:core-ui"))
    implementation(project(":core:core-common"))
    implementation(project(":domain"))
}
