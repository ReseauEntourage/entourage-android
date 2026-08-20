# Agent Knowledge Base - Entourage Android

Ce fichier contient la synthèse des connaissances accumulées sur le projet Entourage Android pour assister les futurs agents IA.

## 1. Architecture & Patterns
- **Manual Dependency Injection :** Pas de Dagger/Hilt. Tout est centralisé dans `EntourageApplication`.
  - Accès : `EntourageApplication.get().apiModule.someRequest`
  - Utilisateur : `EntourageApplication.get().me()`
- **Composants "Presenter" :** Souvent des `ViewModel` ou des classes de logique UI liées à une Activity/Fragment. Vérifier la super-classe.
- **Réseau :** Retrofit 2 + OkHttp. 
  - `ApiModule` gère les intercepturs (`AuthenticationInterceptor`, `HmacInterceptor`).
  - Troncature des logs réseau à 4000 caractères pour éviter les crashs Logcat/Debugger (Pixel 7a optimization).
- **Navigation & Deep Links :** `UniversalLinkManager` centralise le routage. `MainActivity.handleUniversalLinkFromMain()` est le point d'entrée.
- **Migration Jetpack Compose :** Migration progressive en cours (ex: écrans de paramètres). Utiliser les composants partagés `SettingsHeader`, `SettingsItem`, `OrangeButton`.

## 2. Build & Environnement
- **Build System :** Gradle Kotlin DSL (`.gradle.kts`).
- **Kotlin Integration :** Migré vers le support natif d'AGP 9.0+ (plugin `kotlin-android` supprimé au profit de l'intégration intégrée).
- **Flavors :** un seul flavor `entourage`, décliné en 3 build types : `debug` (défaut, API staging, débuggable), `preprod` (API staging, signé release), `release` (API prod).
  - Cible de dev principale : `EntourageDebug`.
  - Commande de compilation recommandée : `./gradlew :app:compileEntourageDebugKotlin`
- **Variables d'environnement :** `ENTOURAGE_URL`, `PEDAGO_CREATE_EVENT_ID`, etc., varient selon le build type. Ne jamais les hardcoder.

## 3. UI & Ressources
- **ViewBinding :** Obligatoire. Pas de `findViewById`.
  - IDs en `snake_case`, propriétés de binding en `camelCase`.
- **Internationalisation :** Le français est la source (`values/strings.xml`). Utiliser `./add_strings.sh` pour ajouter des clés.
- **Performance UI :** Éviter les appels `requestLayout()` redondants. Toujours vérifier si une valeur (padding, visibility, layoutParams) a réellement changé avant de l'appliquer (cf. `Extensions.kt`).
- **Analytics :** Utiliser `AnalyticsEvents.logEvent(AnalyticsEvents.VIEW__...)`.
- **Vector Drawables :** `vectorDrawables.useSupportLibrary = true` est activé pour éviter les erreurs de génération PNG sur les anciens API.

## 4. Règles de Sécurité & Performance (Anti-Crash)
- **Indexation :** Ignorer `build/`, `.gradle/`, `.idea/`, `keystore/` et les fichiers binaires (`*.apk`, `*.jar`).
- **Shell :** Ne jamais exécuter `git` ou `adb` dans les scripts Gradle (risque de timeout/blocage environnement cloud).
