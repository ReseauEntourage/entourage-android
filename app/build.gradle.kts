plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.navigation.safeargs)
    alias(libs.plugins.google.services)
}

fun String.runCommand(currentWorkingDir: File = file("./")): String {
    return providers.exec {
        workingDir = currentWorkingDir
        commandLine = this@runCommand.split("\\s".toRegex())
    }.standardOutput.asText.get().trim()
}

android {
// Java versions
    val sourceCompatibilityVersion = JavaVersion.VERSION_17
    val targetCompatibilityVersion = JavaVersion.VERSION_17

    // App versions
    val isRelease = project.gradle.startParameter.taskNames.any { it.contains("release", ignoreCase = true) }

    val versionMajor = 14
    val versionMinor = 5

    // Use a fixed version for debug builds to speed up configuration and enable caching
    val versionPatch = if (isRelease) {
        "git rev-list HEAD --count".runCommand().toIntOrNull() ?: 0
    } else {
        1000
    }

    val versionBranchName = if (isRelease) {
        "git rev-parse --abbrev-ref HEAD".runCommand()
    } else {
        "debug"
    }
    val versionCodeInt = (versionMajor * 100 + versionMinor) * 10000 + versionPatch % 10000
    val versionNameProd = "${versionMajor}.${versionMinor}.${versionPatch}"
    val appBundleName = System.getenv("APPBUNDLE_NAME") ?: "app"

    val entourageURLProd = "https://api.entourage.social/api/v1/"
    val entourageURLStaging = "https://api-preprod.entourage.social/api/v1/"
    val deepLinksSchemeProd = "entourage"
    val deepLinksSchemeStaging = "entourage-staging"
    val deepLinksURLProd = "www.entourage.social"
    val deepLinksURLStaging = "preprod.entourage.social"

    buildFeatures {
        viewBinding = true
        dataBinding = true

    }
    bundle {
        language {
            enableSplit = false
        }
    }

    compileSdk = 37
    buildToolsVersion = "36.1.0"

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
    base {
        archivesName.set("$appBundleName-$versionNameProd")
    }

    defaultConfig {
        manifestPlaceholders += mapOf(
            "deepLinksHostName" to deepLinksURLProd,
            "deepLinksScheme" to deepLinksSchemeProd
        )
        applicationId = "social.entourage.android"

        minSdk = 23 /*November 2015: Android 6.0, MarshMallow*/
        targetSdk = 37

        // Making either of these two values dynamic in the defaultConfig will
        // require a full APK build and reinstallation because the AndroidManifest.xml
        // must be updated.
        versionCode =1000
        versionName ="10.0"

        buildConfigField("String", "VERSION_FULL_NAME", "\"" + versionNameProd + "\"")
        buildConfigField("String", "VERSION_DISPLAY_BRANCH_NAME", "\"" + versionBranchName + "\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        buildConfigField("String", "ENTOURAGE_URL", "\"${entourageURLProd}\"")
        buildConfigField("String", "TEST_ACCOUNT_LOGIN", localTestAccountLogin)
        buildConfigField("String", "TEST_ACCOUNT_PWD", localTestAccountPwd)
    }

    signingConfigs {
        create("googleplay") {
            val keystorePass= System.getenv("KEYSTORE_PASS") ?: findProperty("entourageKeystorePassword") as String? ?: ""
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
            buildConfigField("String", "PEDAGO_GUIDE_ID", "\"eOB7jU8NNODY\"")
        }
        create("staging") {
            isDefault = true
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
            buildConfigField("String", "PEDAGO_GUIDE_ID", "\"eyck8DuIn3cI\"")

        }
        create("entourage") {
            dimension = "app"
            buildConfigField("String", "API_KEY", "\"4a7373f3e7dd45fc391a2f19\"")
            val hmacSecret = (System.getenv("HMAC_SECRET_ANDROID")
                ?: findProperty("entourageHmacSecret") as String?
                ?: "")
            buildConfigField("String", "HMAC_SECRET", "\"$hmacSecret\"")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getAt("googleplay")
            isDebuggable = false
            ndk {
                debugSymbolLevel = "FULL"
            }
        }

        debug {
            isDefault = true
            signingConfig = signingConfigs.getAt("debug")
            applicationIdSuffix = ".debug"
            //firebaseCrashlytics.mappingFileUploadEnabled = false
            //optimizing build speed
            //aaptOptions.cruncherEnabled = false
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
        isCoreLibraryDesugaringEnabled = true
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
    aboutLibraries {
        offlineMode.set(true)
    }
    namespace = "social.entourage.android"
}

aboutLibraries {
    // keep it empty
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.okhttp.bom))

    implementation(libs.androidx.annotation)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.compose.ui.text.android)

    implementation(libs.tape)
    implementation(libs.timber)

    //https://firebase.google.com/support/release-notes/android
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.inappmessaging.display)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.config)
    //implementation("com.google.firebase:firebase-perf")

    //implementation gmsDependencies.values()
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location) //v19 needs refactoring
    //implementation("com.google.android.libraries.places:places-compat:2.6.0")
    implementation(libs.places)

    //implementation networkDependencies.values()
    implementation(libs.retrofit.converter.gson)
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    //implementation uiDependencies.values()
    implementation(libs.material.datetime.picker)
    implementation(libs.fab)
    implementation(libs.cropme)
    implementation(libs.ucrop)
    implementation(libs.maps.utils.ktx)
    implementation(libs.material)
    implementation(libs.glide)
    implementation(libs.androidsvg)
    implementation(libs.shortcut.badger)
    implementation(libs.keyboard.visibility.event)
    ksp(libs.glide.ksp)

    //entourageImplementation facebookDependencies.values()
    implementation(libs.facebook.android.sdk)
    implementation(libs.facebook.core)
    implementation(libs.shimmer)
    compileOnly(libs.javax.annotation)

    // Instrumentation tests
    androidTestImplementation(libs.bundles.androidx.test)
    androidTestImplementation(libs.androidx.espresso.contrib) {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    androidTestImplementation(libs.bundles.espresso.test)

    // Unit tests
    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.test.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.mockito.kotlin)

    implementation(libs.flexbox)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.sectioned.recyclerview)
    implementation(libs.lottie)
    implementation(libs.photoview)
    implementation(libs.transition)
    implementation(libs.play.app.update.ktx)
    implementation(libs.play.asset.delivery)
    implementation(libs.play.asset.delivery.ktx)
    implementation(libs.play.feature.delivery)
    implementation(libs.play.feature.delivery.ktx)
    implementation(libs.play.review)
    implementation(libs.play.review.ktx)
    implementation(libs.speed.dial)
    implementation(libs.firebase.database)
    implementation(libs.bundles.ktor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.play.services.auth.api.phone)
    //UNCOMMENT FOR VIDEO CALL FEATURE
    //implementation("com.dafruits:webrtc:123.0.0")
    implementation(libs.bundles.oss)

    // Ajout du dictionnaire de rétrocompatibilité (Desugaring)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}

tasks.register<Exec>("clearSnapshots") {
    group = "verification"
    description = "Vider les snapshots sur le device"
    commandLine("adb", "shell", "rm", "-rf", "/sdcard/Download/entourage_snapshots/*")
    isIgnoreExitValue = true
}

tasks.register<Exec>("pullSnapshots") {
    group = "verification"
    description = "Transférer les snapshots du device vers le répertoire local et vider le device"

    val localDir = File(project.layout.buildDirectory.asFile.get(), "reports/snapshots")
    doFirst {
        if (!localDir.exists()) localDir.mkdirs()
    }

    commandLine("adb", "pull", "/sdcard/Download/entourage_snapshots/.", localDir.absolutePath)

    isIgnoreExitValue = true
    finalizedBy("clearSnapshots")
}