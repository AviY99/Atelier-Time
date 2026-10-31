// Versions match the CoGo 26.38+ on-device toolchain documented by its maintainers.
// AGP 9 provides Kotlin compilation; do NOT apply org.jetbrains.kotlin.android.
buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.21")
    }
}
plugins {
    id("com.android.application") version "9.3.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.21" apply false
}
