plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.proyectotitulo"
    compileSdk = 36

    defaultConfig {
        // CHANGED: Giving the app a new identity to bypass system's cached permissions
        applicationId = "com.example.proyectotitulo.healthapp"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Jetpack Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    debugImplementation(libs.compose.ui.tooling)

    // Health Connect - API oficial de Google para datos de salud
    implementation("androidx.health.connect:connect-client:1.1.0-alpha10")
    
    // Activity para Health Connect permissions
    implementation("androidx.activity:activity-ktx:1.9.0")

    // WorkManager para notificaciones en background
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))
    // Firebase Auth for authentication
    implementation("com.google.firebase:firebase-auth")
    // Firebase Firestore for database
    implementation("com.google.firebase:firebase-firestore")

    // Firebase App Check
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    // App Check debug provider
    implementation("com.google.firebase:firebase-appcheck-debug")

    // MPAndroidChart para gráficos (legacy - se migrará a Compose charts)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    
    // Vico Charts para Compose
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}