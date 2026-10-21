plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// GitHub Actions numbers its runs 1, 2, 3, ... Using that number as versionCode makes every CI build an upgrade of
// the one before, so the phone installs it over the old one. Local builds are number 1.
val buildNumber = providers.environmentVariable("GITHUB_RUN_NUMBER").orNull?.toIntOrNull() ?: 1

android {
    namespace = "com.seyta.forge"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.seyta.forge"
        minSdk = 26
        targetSdk = 37
        versionCode = buildNumber
        versionName = "0.1.$buildNumber"
    }

    signingConfigs {
        // The debug key is part of the repo (app/debug.keystore), so every computer and every CI run signs with the
        // same key. Android only installs an update over an app that was signed with the same key.
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true // the home screen shows BuildConfig.VERSION_NAME
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
