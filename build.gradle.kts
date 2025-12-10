
// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    id("com.android.application") version "8.13.1" apply false
    id("com.google.gms.google-services") version "4.4.3" apply false
    id("com.google.firebase.crashlytics") version "3.0.4" apply false
    kotlin("plugin.serialization") version "2.1.0" apply false
    kotlin("android") version "2.1.0" apply false
    kotlin("kapt") version "2.1.0" apply false
    id("androidx.navigation.safeargs") version "2.9.1" apply false
}
