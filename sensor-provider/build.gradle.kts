plugins {
    id("com.android.application")
}

android {
    namespace = "com.jglenn9k.aviator.sensors"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.jglenn9k.aviator.sensors"
        minSdk = 30
        targetSdk = 36
        versionCode = providers.environmentVariable("RELEASE_VERSION_CODE").orNull?.toIntOrNull() ?: 1
        versionName = providers.environmentVariable("RELEASE_VERSION_NAME").orNull ?: "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures { buildConfig = true }
    testOptions { unitTests.isIncludeAndroidResources = true }
    signingConfigs {
        val keystorePath = providers.environmentVariable("ANDROID_KEYSTORE_PATH").orNull
        if (keystorePath != null) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = providers.environmentVariable("ANDROID_KEYSTORE_PASSWORD").orNull
                keyAlias = providers.environmentVariable("ANDROID_KEY_ALIAS").orNull
                keyPassword = providers.environmentVariable("ANDROID_KEY_PASSWORD").orNull
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("release")
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.wear:wear:1.3.0")
    implementation("androidx.wear.watchface:watchface-complications-data-source-ktx:1.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
}
