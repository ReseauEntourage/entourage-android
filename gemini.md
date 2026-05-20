# Configuration IA pour le projet Entourage Android

## 1. Contexte du Projet
- **Projet :** Application Android Entourage (Réseau social de solidarité).
- **Langage principal :** Kotlin.
- **UI & Architecture :** XML Layouts, ViewBinding, DataBinding.
- **Build System :** Gradle (Kotlin DSL - `build.gradle.kts`).

## 2. 🛑 RÈGLES STRICTES D'INDEXATION (ANTI-CRASH)
Pour éviter les timeouts et la surcharge de la mémoire de la machine virtuelle, tu dois **IMPÉRATIVEMENT IGNORER** les dossiers et fichiers suivants lors de ton analyse et de tes recherches :
- Tous les dossiers générés : `build/`, `app/build/`, `.gradle/`, `.idea/`
- Tous les fichiers binaires et de build : `*.apk`, `*.aab`, `*.aar`, `*.jar`, `*.so`, `*.keystore`, `*.jks`
- Les dossiers de snapshots ou de tests lourds : `reports/snapshots/`
- Le dossier `keystore/`

## 3. ⚠️ Comportement avec Gradle (Environnement restreint)
- **Attention :** L'environnement d'exécution cloud ne possède pas toujours un accès complet à `git` ou aux `System.getenv()`.
- Lors de l'évaluation ou de la modification des scripts `build.gradle.kts`, **n'essaie jamais** d'exécuter des commandes système bloquantes (comme `git rev-list`, `git rev-parse` ou `adb`).
- Assume que les variables d'environnement (comme `KEYSTORE_PASS`, `APPBUNDLE_NAME`) sont configurées en amont. Ne bloque pas si elles sont manquantes.

## 4. Navigation dans le code
- Le code source principal (Activities, Fragments, ViewModels) se trouve sous : `app/src/main/java/social/entourage/android/`
- Les ressources UI (XML) se trouvent sous : `app/src/main/res/layout/` et `app/src/main/res/values/`
- Pour toute modification UI (comme les BottomSheets ou l'affichage conditionnel de boutons), priorise l'utilisation du ViewBinding existant.

## 5. Style de Code
- Préférer l'utilisation des standards Kotlin (Coroutines, Flow, fonctions d'extension) plutôt que l'ancien code Java.
- Respecter l'architecture en place sans suggérer des refontes massives pour des tâches isolées.