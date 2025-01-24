plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
}

android {
    namespace = "app.unbound.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.unbound.android"
        minSdk = 24
        targetSdk = 35
        versionCode = 6
        versionName = "1.0.0"
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
        }
        release {
            isDebuggable = false
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += "-Xcontext-receivers"
    }

    namespace = "app.unbound.android"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation("com.github.ajalt.colormath:colormath:latest.release")
    implementation("com.google.code.gson:gson:latest.release")
    implementation("androidx.core:core-ktx:+")
    compileOnly("de.robv.android.xposed:api:82")
}