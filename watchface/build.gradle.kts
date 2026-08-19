plugins {
    id("com.android.application")
}

val watch5ProMinSdk = 33
val watchFaceFormatVersion = "1"

android {
    namespace = "com.jglenn9k.aviator.watchface"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.jglenn9k.aviator.watchface"
        // WFF v1 starts at API 33 (Wear OS 4), the lowest WFF version supported
        // by an updated Galaxy Watch5 Pro (SM-R920).
        minSdk = watch5ProMinSdk
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

val verifyWatch5ProCompatibility by tasks.registering {
    group = "verification"
    description = "Guards WFF v1 / Galaxy Watch5 Pro (SM-R920) compatibility."

    val manifestFile = layout.projectDirectory.file("src/main/AndroidManifest.xml")
    val watchFaceFile = layout.projectDirectory.file("src/main/res/raw/watchface.xml")
    inputs.files(manifestFile, watchFaceFile)

    doLast {
        check(watch5ProMinSdk == 33) {
            "Galaxy Watch5 Pro compatibility requires minSdk 33 for WFF v1; found $watch5ProMinSdk."
        }

        val documentBuilder = javax.xml.parsers.DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
        }.newDocumentBuilder()
        val androidNamespace = "http://schemas.android.com/apk/res/android"
        val manifest = documentBuilder.parse(manifestFile.asFile)
        val application = manifest.getElementsByTagName("application").item(0)
            as? org.w3c.dom.Element ?: error("Watch-face manifest has no <application> element.")

        check(application.getAttributeNS(androidNamespace, "hasCode") == "false") {
            "A Watch Face Format package must set android:hasCode=\"false\"."
        }

        val hasRequiredWatchFeature = manifest.getElementsByTagName("uses-feature").let { features ->
            (0 until features.length).any { index ->
                val feature = features.item(index) as org.w3c.dom.Element
                feature.getAttributeNS(androidNamespace, "name") == "android.hardware.type.watch" &&
                    feature.getAttributeNS(androidNamespace, "required") != "false"
            }
        }
        check(hasRequiredWatchFeature) {
            "The package must require android.hardware.type.watch for Play's Wear OS catalog."
        }

        val declaredWffVersion = manifest.getElementsByTagName("property").let { properties ->
            (0 until properties.length).firstNotNullOfOrNull { index ->
                val property = properties.item(index) as org.w3c.dom.Element
                property.getAttributeNS(androidNamespace, "value").takeIf {
                    property.getAttributeNS(androidNamespace, "name") ==
                        "com.google.wear.watchface.format.version"
                }
            }
        }
        check(declaredWffVersion == watchFaceFormatVersion) {
            "Galaxy Watch5 Pro support requires WFF v1; found ${declaredWffVersion ?: "no declaration"}."
        }

        val watchFace = documentBuilder.parse(watchFaceFile.asFile).documentElement
        check(watchFace.tagName == "WatchFace") {
            "The WFF resource must have <WatchFace> as its root element."
        }
    }
}

tasks.named("preBuild") {
    dependsOn(verifyWatch5ProCompatibility)
}
