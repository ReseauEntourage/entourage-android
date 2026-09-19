# Compte utilisé pour les scénarios E2E

À utiliser pour tous les scénarios E2E (`app/src/androidTest/java/social/entourage/android/e2e/`),
quel que soit le test, via `entourageTestLogin` / `entourageTestPwd` dans `~/.gradle/gradle.properties`
(ou les variables d'env `TEST_ACCOUNT_LOGIN` / `TEST_ACCOUNT_PWD`) :

```properties
entourageTestLogin="0601886036"
entourageTestPwd="123456"
```

Note : c'est un compte personnel réel (staging), pas un compte de test anonyme dédié —
les scénarios E2E lisent/écrivent donc dans ses vraies conversations/événements.

Ce fichier n'est volontairement pas versionné (voir `.gitignore`).
