plugins {
    alias(libs.plugins.resilia.android.application)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.goncalogoncalves.resilia"

    defaultConfig {
        applicationId = "com.goncalogoncalves.resilia"
        versionCode = 1
        versionName = "0.1.0"
    }

    androidResources {
        // Exposes the app languages (en-GB, pt) in the Android 13+ per-app language settings.
        generateLocaleConfig = true
    }

    // Release signing (Marco 9b). The upload key NEVER goes into the repository: it is read from
    // environment variables. Without them the release build is simply unsigned (CI, contributors).
    val keystorePath = System.getenv("RESILIA_KEYSTORE")
    signingConfigs {
        if (keystorePath != null) {
            create("upload") {
                storeFile = file(keystorePath)
                storePassword = System.getenv("RESILIA_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("RESILIA_KEY_ALIAS")
                keyPassword = System.getenv("RESILIA_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (keystorePath != null) signingConfig = signingConfigs.getByName("upload")
        }
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
    implementation(project(":feature:home"))
    implementation(project(":feature:guides"))
    implementation(project(":feature:emergency"))
    implementation(project(":feature:prepare"))
    implementation(project(":feature:morale"))
    implementation(project(":feature:tools"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:alerts"))
    implementation(project(":feature:places"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)

    testImplementation(libs.junit)
}
