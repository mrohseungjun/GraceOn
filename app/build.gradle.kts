plugins {
    id("graceon.android.application")
    id("graceon.android.compose")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.graceon"
    
    defaultConfig {
        applicationId = "com.graceon"
        
        // Read GEMINI_API_KEY from local.properties
        val localProperties = java.util.Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(java.io.FileInputStream(localPropertiesFile))
        }
        
        val geminiApiKey = localProperties.getProperty("GEMINI_API_KEY") ?: ""
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
    }
    
    buildFeatures {
        buildConfig = true
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Feature modules
    implementation(project(":feature:feature-worry"))
    implementation(project(":feature:feature-gacha"))
    implementation(project(":feature:feature-result"))
    
    // Core modules
    implementation(project(":core:core-ui"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-network"))
    
    // Domain & Data
    implementation(project(":domain"))
    implementation(project(":data"))
    
    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    
    // Koin DI
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    
    // Serialization
    implementation(libs.kotlinx.serialization.json)
}