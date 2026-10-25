plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.ateliertime.android"
    // CoGo device currently provides Android SDK 35 and 36 (no sdkmanager CLI).
    // Compose 1.11.x is the latest stable core line before 1.12 required API 37.
    // Keep targetSdk/minSdk independent from this compile-only toolchain setting.
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ateliertime.android"
        minSdk = 26
        targetSdk = 35
        // Android versionCode is the monotonic package-upgrade integer.
        // versionName is the real development iteration shown verbatim in About.
        // Keep this D-identifier synchronized with each future APK build.
        versionCode = 5
        versionName = "D-110"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.06.01")
    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.12.4")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
