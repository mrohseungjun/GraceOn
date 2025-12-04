plugins {
    id("graceon.android.feature")
}

android {
    namespace = "com.graceon.feature.worry"
}

dependencies {
    implementation(project(":data"))
}
