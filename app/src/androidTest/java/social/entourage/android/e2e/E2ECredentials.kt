package social.entourage.android.e2e

import social.entourage.android.BuildConfig

/**
 * Identifiants du compte de test dédié aux scénarios E2E.
 *
 * Rien n'est écrit en dur ici : le repo est public. Les valeurs viennent des variables
 * d'environnement TEST_ACCOUNT_LOGIN / TEST_ACCOUNT_PWD, ou à défaut des propriétés Gradle
 * entourageTestLogin / entourageTestPwd (dans ~/.gradle/gradle.properties), cf. le
 * defaultConfig de app/build.gradle.kts. Même mécanisme que LoginTest et SignUpTest.
 */
object E2ECredentials {
    val PHONE: String = BuildConfig.TEST_ACCOUNT_LOGIN
    val PASSWORD: String = BuildConfig.TEST_ACCOUNT_PWD
}
