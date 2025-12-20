plugins {
    id("graceon.android.feature")
    id("graceon.android.compose")
}

android {
    namespace = "com.graceon.feature.onboarding"
}

dependencies {
    implementation(project(":core:core-ui"))
    implementation(project(":core:core-common"))
}
