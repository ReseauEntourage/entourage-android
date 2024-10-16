// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    dependencies {
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.6")
    }
}



plugins {
    id("com.android.application") version "8.5.1" apply false
    id("com.google.gms.google-services") version "4.3.15" apply false
    id("com.google.firebase.crashlytics") version "2.9.5" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    //id("com.google.firebase.firebase-perf") version "1.4.2" apply false // Performance Monitoring plugin
    id("androidx.navigation.safeargs") version "2.5.3" apply false
}
