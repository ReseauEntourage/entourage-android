# android (entourage-android) — overview

> Added by the `entourage_specs` meta-repo. The submodule's own canonical README lives at `README.md`.

The native Android client of the LOCAL product. Kotlin, Gradle (Kotlin DSL), Android SDK 37, minSdk 23 (Android 6.0), Java 17 source/target. Distributed on Google Play under `social.entourage.android`. Eight languages supported (en, fr, de, pl, es, uk, ro, ar). Two signing configs (`googleplay` for release, `debug` for development), two product flavours / build variants (`Entourage` and `EntourageStaging`).

## Interactions

- **Backend**: HTTPS REST against the Rails API at `https://api.entourage.social/api/v1/` (production) and `https://api-preprod.entourage.social/api/v1/` (staging). The base URL, deep-link scheme and deep-link host are injected via `BuildConfig` (`ENTOURAGE_URL`, `DEEP_LINKS_SCHEME`, `DEEP_LINKS_URL`).
- **Firebase**: `app/google-services.json` for Crashlytics, push and analytics, wired via the `firebase.crashlytics` and `google.services` Gradle plugins.
- **Deep linking**: `entourage://` scheme in production, `entourage-staging://` in staging.
- **Bitrise**: CI workflows defined in `bitrise.yml` — `pr_check`, `dev_entourage` (staging build on `develop` push) and Play Store release flows.

## Installing / scripts

```bash
# Build via Gradle wrapper (uses -Xmx6g -XX:+HeapDumpOnOutOfMemoryError)
./gradlew assembleEntourageStagingRelease
./gradlew assembleRelease
./gradlew assembleDebug

# Bitrise local invocation
bitrise run dev_entourage     # build staging APK locally with Bitrise CLI
```

Helper scripts in the repo root for managing localized strings:

```bash
./add_strings.sh              # add a new French string, propagate stubs
./add_strings_en.sh           # add a new English string
./update_activity.sh          # boilerplate for a new Activity
python copy_strings.py        # bulk copy of strings between locales
python update_strings.py      # bulk update of strings
```

`gradle.properties` and `build.gradle.kts` apply the version code/name from the git commit count (major.minor.patch). `keystore/googleplay-keystore.jks` and `keystore/debug.keystore` hold the signing material.

## External libraries

Managed via Gradle KTS + version catalog. Top-level plugins / dependencies seen:

- `com.android.application`, `org.jetbrains.kotlin.android`
- `com.google.firebase.crashlytics`, `com.google.gms.google-services`
- `androidx.navigation.safeargs`
- AboutLibraries, Kotlin serialization, view binding, data binding.

Build tools: AGP 36.1.0, compileSdk 37, minSdk 23, targetSdk 37, Java 17.

## Used technologies

- **Language**: Kotlin (+ a small amount of Java where needed).
- **Build**: Gradle 8.x with Kotlin DSL (KTS).
- **Android SDK**: 37 (compile/target), 23 minimum.
- **CI/CD**: Bitrise (`pr_check`, `dev_entourage`, release).
- **Distribution**: Google Play Store (`social.entourage.android`).

## Secrets

Gradle properties, all referenced via `findProperty()` in `build.gradle.kts`:

- `KEYSTORE_PASS` — keystore password for the Play Store signing config.
- `TEST_ACCOUNT_LOGIN`, `TEST_ACCOUNT_PWD` — instrumented test credentials.
- `APPBUNDLE_NAME` — custom app bundle name (defaults to `app`).

Files:

- `app/google-services.json` — Firebase configuration (committed).
- `keystore/googleplay-keystore.jks` — Play Store signing key.
- `keystore/debug.keystore` — debug signing key.

CI: Bitrise env vars (Bitrise Secrets) hold the runtime values for the gradle properties above and any Crashlytics / Play Console upload tokens.
