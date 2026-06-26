// ===================================================================
// FILE: build.gradle.kts (ROOT PROJECT)
// Top-level build file: konfigurasi yang dipakai oleh semua module/sub-project.
// ===================================================================
plugins {
    id("com.android.application") version "8.7.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false
    // Plugin KSP -> wajib untuk Room Database annotation processing
    id("com.google.devtools.ksp") version "2.0.20-1.0.25" apply false
}
