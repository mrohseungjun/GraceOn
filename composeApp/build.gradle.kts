import java.io.FileInputStream
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    id("graceon.compose.multiplatform")
    id("graceon.android.compose")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(project(":core:core-common"))
            implementation(project(":core:core-network"))

            implementation(libs.androidx.core.ktx)
            implementation("androidx.core:core-splashscreen:1.0.1")
            implementation(libs.androidx.lifecycle.runtime.ktx)
            implementation(libs.androidx.activity.compose)
            implementation(libs.google.play.services.ads)

            implementation(libs.kotlinx.coroutines.android)
        }
        iosMain.dependencies {
            implementation(project(":core:core-common"))
            implementation(project(":core:core-network"))
            implementation(project(":domain"))
            implementation(project(":data"))
            implementation(libs.kotlinx.coroutines.core)
        }
        commonMain.dependencies {
            implementation(project(":feature:feature-onboarding"))
            implementation(project(":feature:feature-worry"))
            implementation(project(":feature:feature-gacha"))
            implementation(project(":feature:feature-result"))
            implementation(project(":feature:feature-saved"))
            implementation(project(":feature:feature-profile"))

            implementation(project(":domain"))
            implementation(project(":data"))
            implementation(project(":core:core-ui"))
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "com.graceon"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.graceon"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }

        val graceOnApiBaseUrl = localProperties.getProperty("GRACEON_API_BASE_URL") ?: ""
        val supabaseAnonKey = localProperties.getProperty("SUPABASE_ANON_KEY") ?: ""
        val admobAppId = localProperties.getProperty("ADMOB_APP_ID")
            ?: "ca-app-pub-3940256099942544~3347511713"
        val admobRewardedAdUnitId = localProperties.getProperty("ADMOB_REWARDED_AD_UNIT_ID")
            ?: "ca-app-pub-3940256099942544/5224354917"
        buildConfigField("String", "GRACEON_API_BASE_URL", "\"$graceOnApiBaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
        buildConfigField("String", "ADMOB_APP_ID", "\"$admobAppId\"")
        buildConfigField("String", "ADMOB_REWARDED_AD_UNIT_ID", "\"$admobRewardedAdUnitId\"")
        manifestPlaceholders["ADMOB_APP_ID"] = admobAppId
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    testImplementation(libs.junit)
}
