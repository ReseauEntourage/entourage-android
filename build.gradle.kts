// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    id("com.android.application") version "8.9.3" apply false
    id("com.google.gms.google-services") version "4.4.3" apply false
    id("com.google.firebase.crashlytics") version "3.0.4" apply false
    kotlin("plugin.serialization") version "2.1.0" apply false   // <— AJOUTE ÇA
    kotlin("android") version "2.1.0" apply false
    //id("com.google.firebase.firebase-perf") version "1.4.2" apply false // Performance Monitoring plugin
    id("androidx.navigation.safeargs") version "2.9.1" apply false
}
