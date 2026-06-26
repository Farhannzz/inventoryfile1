// ===================================================================
// FILE: app/build.gradle.kts (Module :app)
// ===================================================================

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Wajib untuk Room (annotation processing menggunakan KSP, lebih cepat dari kapt)
    id("com.google.devtools.ksp")
    // Wajib jika ingin pakai Kotlin Parcelize (opsional, untuk passing object antar Activity)
    id("kotlin-parcelize")
}

android {
    namespace = "com.id.farhaninventory"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.id.farhaninventory"
        minSdk = 26 // Minimal SDK 26 (Android 8.0)
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true // Mengaktifkan ViewBinding untuk akses View tanpa findViewById
    }
}

dependencies {

    // ---------- CORE & UI (Material Design 3) ----------
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0") // Material Design 3
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // ---------- LIFECYCLE & VIEWMODEL (Arsitektur MVVM) ----------
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-ktx:1.9.1")

    // ---------- ROOM DATABASE (SQLite Lokal) ----------
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion") // Mendukung Coroutines & Flow
    ksp("androidx.room:room-compiler:$roomVersion")

    // ---------- COROUTINES (Operasi Database Async) ----------
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    // ---------- RECYCLERVIEW ----------
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // ---------- WORKMANAGER (opsional, untuk notifikasi terjadwal/reliable) ----------
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // ---------- TESTING ----------
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
