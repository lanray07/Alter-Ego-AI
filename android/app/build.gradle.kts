plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.alteregoai.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.alteregoai.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
        val aiBackendUrl = project.findProperty("AI_BACKEND_URL")?.toString().orEmpty()
        buildConfigField("String", "AI_BACKEND_URL", "\"$aiBackendUrl\"")
    }

    val uploadStoreFile = providers.gradleProperty("ANDROID_UPLOAD_STORE_FILE").orNull ?: System.getenv("ANDROID_UPLOAD_STORE_FILE")
    val uploadStorePassword = providers.gradleProperty("ANDROID_UPLOAD_STORE_PASSWORD").orNull ?: System.getenv("ANDROID_UPLOAD_STORE_PASSWORD")
    val uploadKeyAlias = providers.gradleProperty("ANDROID_UPLOAD_KEY_ALIAS").orNull ?: System.getenv("ANDROID_UPLOAD_KEY_ALIAS")
    val uploadKeyPassword = providers.gradleProperty("ANDROID_UPLOAD_KEY_PASSWORD").orNull ?: System.getenv("ANDROID_UPLOAD_KEY_PASSWORD")

    signingConfigs {
        create("release") {
            if (!uploadStoreFile.isNullOrBlank() && !uploadStorePassword.isNullOrBlank() && !uploadKeyAlias.isNullOrBlank() && !uploadKeyPassword.isNullOrBlank()) {
                storeFile = file(uploadStoreFile)
                storePassword = uploadStorePassword
                keyAlias = uploadKeyAlias
                keyPassword = uploadKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (!uploadStoreFile.isNullOrBlank() && !uploadStorePassword.isNullOrBlank() && !uploadKeyAlias.isNullOrBlank() && !uploadKeyPassword.isNullOrBlank()) signingConfig = signingConfigs.getByName("release")
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
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:${libs.versions.composeBom.get()}"))
    androidTestImplementation(platform("androidx.compose:compose-bom:${libs.versions.composeBom.get()}"))
    implementation(libs.bundles.compose)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.runtime:runtime-saveable")
    implementation(libs.androidx.compose.material.icons)
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.google.play.billing)
    implementation(libs.androidx.work.runtime)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
