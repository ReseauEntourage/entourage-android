import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.ByteArrayOutputStream

plugins {
    id("com.android.application")
    kotlin("android")
    //id("kotlin-android-extensions")
    id("com.google.firebase.crashlytics")
    //id("com.google.firebase.firebase-perf")
    kotlin("kapt")
    id("androidx.navigation.safeargs")
    id("com.google.gms.google-services")

}

fun String.runCommand(currentWorkingDir: File = file("./")): String {
    val byteOut = ByteArrayOutputStream()
    project.exec {
        workingDir = currentWorkingDir
        commandLine = this@runCommand.split("\\s".toRegex())
        standardOutput = byteOut
    }
    return String(byteOut.toByteArray()).trim()
}

android {
// Java versions
    val sourceCompatibilityVersion = JavaVersion.VERSION_11
    val targetCompatibilityVersion = JavaVersion.VERSION_11

    // App versions
    val versionMajor = 12
    val versionMinor = 4
    val versionPatch = "git rev-list HEAD --count".runCommand().toInt()
    val versionBranchName = "git rev-parse --abbrev-ref HEAD".runCommand()
    val versionCodeInt = (versionMajor * 100 + versionMinor) * 10000 + versionPatch % 10000
    val versionNameProd = "${versionMajor}.${versionMinor}.${versionPatch}"
    val appBundleName = System.getenv("APPBUNDLE_NAME") ?: "app"

    val entourageURLProd = "https://api.entourage.social/api/v1/"
    val entourageURLStaging = "https://api-preprod.entourage.social/api/v1/"
    val deepLinksSchemeProd = "entourage"
    val deepLinksSchemeStaging = "entourage-staging"
    val deepLinksURLProd = "www.entourage.social"
    val deepLinksURLStaging = "preprod.entourage.social"

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true

    }
    bundle {
        language {
            enableSplit = false
        }
    }

    compileSdk = 36
    buildToolsVersion = "36.0.0"

    val localTestAccountLogin = System.getenv("TEST_ACCOUNT_LOGIN")?.let { login -> "\""+ login+ "\"" }
        ?: findProperty("entourageTestLogin") as String?
        ?: "\"\""
    val localTestAccountPwd = System.getenv("TEST_ACCOUNT_PWD")?.let { login -> "\""+ login+ "\"" }
        ?: findProperty("entourageTestPwd") as String?
        ?: "\"\""

    buildFeatures.buildConfig = true

    androidResources{
        localeFilters.addAll(listOf("en", "fr", "de", "pl", "es","uk", "ro", "ar"))
    }

    defaultConfig {
        manifestPlaceholders += mapOf(
            "deepLinksHostName" to deepLinksURLProd,
            "deepLinksScheme" to deepLinksSchemeProd
        )
        applicationId = "social.entourage.android"

        minSdk = 23 /*November 2015: Android 6.0, MarshMallow*/
        targetSdk = 36

        // Making either of these two values dynamic in the defaultConfig will
        // require a full APK build and reinstallation because the AndroidManifest.xml
        // must be updated.
        versionCode =1000
        versionName ="10.0"

        buildConfigField("String", "VERSION_FULL_NAME", "\"" + versionNameProd + "\"")
        buildConfigField("String", "VERSION_DISPLAY_BRANCH_NAME", "\"" + versionBranchName + "\"")
        setProperty("archivesBaseName", "$appBundleName-$versionNameProd")
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        buildConfigField("String", "ENTOURAGE_URL", "\"${entourageURLProd}\"")
        buildConfigField("String", "TEST_ACCOUNT_LOGIN", localTestAccountLogin)
        buildConfigField("String", "TEST_ACCOUNT_PWD", localTestAccountPwd)
    }

    signingConfigs {
        create("googleplay") {
            val keystorePass= System.getenv("KEYSTORE_PASS") ?: findProperty("entourageKeystorePassword") as String
            keyAlias = "googleplay"

            keyPassword = keystorePass
            storeFile = file("../keystore/googleplay-keystore.jks")
            storePassword = keystorePass
        }

        getByName("debug") {
            storeFile = file("../keystore/debug.keystore")
        }
    }

    flavorDimensions += listOf("app", "env")

    productFlavors {
        create("prod") {
            dimension = "env"
            buildConfigField("String", "ENTOURAGE_URL", "\"${entourageURLProd}\"")
            buildConfigField("String", "DEEP_LINKS_SCHEME", "\"${deepLinksSchemeProd}\"")
            buildConfigField("String", "DEEP_LINKS_URL", "\"${deepLinksURLProd}\"")
            buildConfigField("int", "PEDAGO_CREATE_EVENT_ID", "15")
            buildConfigField("int", "PEDAGO_CREATE_GROUP_ID", "37")
            buildConfigField("int", "PEDAGO_ACTION_SECTION_ID", "34")
        }
        create("staging") {
            manifestPlaceholders += mapOf(
                "deepLinksHostName" to deepLinksURLStaging,
                "deepLinksScheme" to deepLinksSchemeStaging
            )
            dimension = "env"
            applicationIdSuffix = ".preprod"
            buildConfigField("String", "ENTOURAGE_URL", "\"${entourageURLStaging}\"")
            buildConfigField("String", "DEEP_LINKS_SCHEME", "\"${deepLinksSchemeStaging}\"")
            buildConfigField("String", "DEEP_LINKS_URL", "\"${deepLinksURLStaging}\"")
            buildConfigField("int", "PEDAGO_CREATE_EVENT_ID", "32")
            buildConfigField("int", "PEDAGO_CREATE_GROUP_ID", "33")
            buildConfigField("int", "PEDAGO_ACTION_SECTION_ID", "33")

        }
         create("entourage") {
            dimension = "app"
            buildConfigField("String", "API_KEY", "\"4a7373f3e7dd45fc391a2f19\"")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getAt("googleplay")
            isDebuggable = false
        }

        debug {
            signingConfig = signingConfigs.getAt("debug")
            applicationIdSuffix = ".debug"
            //firebaseCrashlytics.mappingFileUploadEnabled = false
            //optimizing build speed
            aaptOptions.cruncherEnabled = false
            /*FirebasePerformance {
                // Set this flag to "false" to disable @AddTrace annotation processing and
                // automatic monitoring of HTTP/S network requests
                // for a specific build variant at compile time.
                instrumentationEnabled = false
            }*/
        }
    }

    compileOptions {
        sourceCompatibility = sourceCompatibilityVersion
        targetCompatibility = targetCompatibilityVersion
    }

    packaging {
        resources {
            excludes += listOf(
                "META-INF/notice.txt", "META-INF/NOTICE.txt", "META-INF/NOTICE", "META-INF/license.txt", "META-INF/LICENSE.txt", "META-INF/LICENSE", "META-INF/ASL2.0", "META-INF/DEPENDENCIES"
            )
        }
    }

    androidComponents.onVariants { variant ->
        variant.outputs.all { output ->
            output.versionCode.set(versionCodeInt)
            output.versionName.set(versionNameProd)
            return@all true
        }
    }

    lint {
        abortOnError = false
        disable += listOf("InvalidPackage")
    }
    namespace = "social.entourage.android"
}

dependencies {
    implementation("androidx.core:core-ktx:1.16.0")
    implementation(platform("com.squareup.okhttp3:okhttp-bom:5.1.0"))

    //implementation androidSupportDependencies.values()

    implementation("androidx.annotation:annotation:1.9.1")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.browser:browser:1.8.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.3.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.fragment:fragment-ktx:1.8.9")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.compose.ui:ui-text-android:1.8.3")

    implementation("com.squareup:tape:1.2.3")
    implementation("com.jakewharton.timber:timber:5.0.1")


    //https://firebase.google.com/support/release-notes/android
    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))
    //implementation firebaseDependencies.values()
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    implementation("com.google.firebase:firebase-inappmessaging-display")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-config")
    //implementation("com.google.firebase:firebase-perf")

    //implementation gmsDependencies.values()
    implementation("com.google.android.gms:play-services-maps:19.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0") //v19 needs refactoring
    //implementation("com.google.android.libraries.places:places-compat:2.6.0")
    implementation("com.google.android.libraries.places:places:4.3.1")

    //https://developers.google.com/android/guides/opensource
    //implementation("com.google.android.gms:play-services-oss-licenses:17.1.0")

    //implementation networkDependencies.values()
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    //implementation uiDependencies.values()
    implementation("com.wdullaer:materialdatetimepicker:4.2.3")
    implementation("com.github.clans:fab:1.6.4")
    implementation("com.github.takusemba:cropme:2.0.8")
    implementation("com.google.maps.android:maps-utils-ktx:5.2.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("me.leolin:ShortcutBadger:1.1.22@aar")
    implementation("net.yslibrary.keyboardvisibilityevent:keyboardvisibilityevent:3.0.0-RC3")
    implementation("androidx.test.espresso:espresso-core:3.7.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    //entourageImplementation facebookDependencies.values()
    implementation("com.facebook.android:facebook-android-sdk:18.0.3")
    implementation("com.facebook.android:facebook-core:18.0.3")
    compileOnly("org.glassfish:javax.annotation:10.0-b28")

    // Instrumentation tests

    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test:rules:1.7.0")
    androidTestImplementation("androidx.test:core-ktx:1.7.0")

    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
    //androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    //androidTestImplementation("androidx.test.espresso.idling:idling-concurrent:3.5.1")
    //androidTestImplementation("androidx.test.espresso:espresso-idling-resource:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.7.0")
    androidTestImplementation("com.jakewharton.espresso:okhttp3-idling-resource:1.0.0")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")

    // Unit tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test.ext:junit:1.3.0")
    testImplementation("androidx.test:core:1.7.0")
    testImplementation("org.mockito:mockito-core:5.19.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.0.0")


    implementation("com.google.android.flexbox:flexbox:3.0.0")
    // Kotlin
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.3")
    implementation("com.github.IntruderShanky:Sectioned-RecyclerView:2.1.1")

    implementation("com.airbnb.android:lottie:6.6.7")

    //photoview to click and zoom
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    implementation("androidx.transition:transition:1.6.0") // Remplacez 'x.x.x' par la dernière version disponible.

    implementation("com.google.android.play:app-update-ktx:2.1.0")
    implementation("com.google.android.play:asset-delivery:2.3.0")
    implementation("com.google.android.play:asset-delivery-ktx:2.3.0")
    implementation("com.google.android.play:feature-delivery:2.1.0")
    implementation("com.google.android.play:feature-delivery-ktx:2.1.0")
    implementation("com.google.android.play:review:2.0.2")
    implementation("com.google.android.play:review-ktx:2.0.2")
    implementation("com.leinardi.android:speed-dial:3.3.0")
    implementation("com.google.firebase:firebase-database")

    implementation("io.ktor:ktor-client-core:3.2.3")
    implementation("io.ktor:ktor-client-okhttp:3.2.3")
    implementation("io.ktor:ktor-client-content-negotiation:3.2.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.2.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    implementation("io.ktor:ktor-client-core:3.2.3")
    implementation("io.ktor:ktor-client-android:3.2.3")
    implementation("io.ktor:ktor-client-serialization:3.2.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.2.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    //UNCOMMENT FOR VIDEO CALL FEATURE
    //implementation("com.dafruits:webrtc:123.0.0")
}