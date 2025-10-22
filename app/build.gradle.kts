plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.proyectotitulo"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.proyectotitulo"
        minSdk = 26 // Changed to 26 to support adaptive icons
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
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

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

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}