plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
}

android {
    namespace = "app.enmity.xposed"
    compileSdk = 33

    defaultConfig {
        applicationId = "app.enmity.xposed"
        minSdk = 24
        targetSdk = 33
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

    namespace = "app.enmity.xposed"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation("com.google.code.gson:gson:latest.release")
    compileOnly("de.robv.android.xposed:api:82")
}