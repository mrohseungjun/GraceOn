plugins {
    id("graceon.android.feature")
}

android {
    namespace = "com.graceon.feature.gacha"
}

dependencies {
    implementation(project(":data"))
}
