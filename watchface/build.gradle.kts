plugins {
    id("com.android.application")
}

android {
    namespace = "com.jglenn.aviator.watchface"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.jglenn.aviator.watchface"
        minSdk = 33
        targetSdk = 36
        versionCode = providers.environmentVariable("RELEASE_VERSION_CODE").orNull?.toIntOrNull() ?: 1
        versionName = providers.environmentVariable("RELEASE_VERSION_NAME").orNull ?: "1.0.0"
    }

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
            isMinifyEnabled = true
            isShrinkResources = false
            signingConfig = signingConfigs.findByName("release")
        }
    }
}
