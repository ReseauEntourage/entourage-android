---
name: ent:jira-ticket
description: Traite un ticket Jira de bout en bout — analyse, enrichissement produit/technique, implémentation, commit et passage en "To Merge"
disable-model-invocation: true
argument-hint: [clé-ticket] (ex. EN-1234)
allowed-tools: Bash(git status:*), Bash(git diff:*), Bash(git add:*), Bash(git commit:*), Read, Grep, Glob, Edit
---

Tu vas traiter le ticket Jira `$ARGUMENTS` sur ce dépôt (app Android Entourage).

## Étape 1 — Récupération

Récupère le ticket avec `mcp__jira__jira_get_issue` (`issue_key: $ARGUMENTS`, `include: "comments"`) :
titre, description, critères d'acceptation, commentaires existants.

Si la clé ne matche pas un format Jira valide (ex. `EN-1234`) ou que le ticket n'existe pas, arrête-toi et demande une clé valide plutôt que de deviner.

## Étape 2 — Analyse du code existant

Explore le projet pour comprendre ce qui existe déjà en lien avec le ticket (fichiers concernés, état actuel de l'implémentation, dépendances). Appuie-toi sur les conventions de [CLAUDE.md](CLAUDE.md) :
- module/écran probable sous `app/src/main/java/social/entourage/android/...`
- vérifie si un `*Presenter` en jeu est un `ViewModel` ou une classe liée à l'Activity/Fragment
- repère les `*Request` Retrofit concernés dans `api/request/` si le ticket touche au réseau
- pour un écran atteignable par deep link, vérifie `UniversalLinkManager`

Pour une exploration large, utilise l'agent `Explore` plutôt que de multiplier les recherches manuelles.

## Étape 3 — Enrichissement du ticket

Rédige une version enrichie de la description, en deux angles :
- **Produit** : cas limites, impacts UX non mentionnés, questions ouvertes
- **Technique** : fichiers/composants à toucher, risques, points d'attention (ex. variantes de build, rôles utilisateur, analytics à ajouter)

RÈGLE STRICTE : n'ajoute JAMAIS de nouvelle fonctionnalité ni de changement de périmètre. Précise et documente ce qui est déjà spécifié, n'invente rien. Si un point est ambigu, signale-le comme question ouverte plutôt que de trancher à sa place.

Poste cette version enrichie en commentaire via `mcp__jira__jira_add_comment` (`issue_key: $ARGUMENTS`) — ne remplace jamais la description originale.

## Étape 4 — Implémentation

Fais le travail décrit dans le ticket, dans le respect strict des specs d'origine, en suivant les conventions du projet :
- ViewBinding uniquement (pas de `findViewById`)
- `AnalyticsEvents.logEvent(...)` pour toute action utilisateur significative
- chaînes de caractères ajoutées en français dans `values/strings.xml` via `./add_strings.sh` (jamais directement dans `values-en/`)
- pas de nouvelle dépendance/abstraction non nécessaire au ticket

Vérifie la compilation avec `./gradlew :app:compileEntourageDebugKotlin` (nécessitera une confirmation, cette commande n'est pas pré-approuvée par ce skill).

Si le ticket modifie un écran/une vue existante : selon [CLAUDE.md](CLAUDE.md), le test e2e correspondant doit être mis à jour. Ces tests vivent sur la branche `end_to_end_test` (pas `develop`) — signale ce suivi dans le commentaire Jira de l'étape 3/5 plutôt que de le faire silencieusement en dehors du scope de ce ticket, sauf si l'utilisateur demande explicitement de traiter aussi l'e2e dans cette même passe.

## Étape 5 — Commit

Vérifie `git status`/`git diff`, ajoute les fichiers pertinents (jamais `git add -A`/`.` à l'aveugle) et commit avec un message clair référençant le ticket, par ex. :

```
fix(<scope>): <résumé court> ($ARGUMENTS)
```

## Étape 6 — Transition

1. Appelle `mcp__jira__jira_get_transitions` (`issue_key: $ARGUMENTS`) pour lister les transitions disponibles.
2. Repère l'ID correspondant au statut "To Merge" (comparaison insensible à la casse).
3. Appelle `mcp__jira__jira_transition_issue` avec cet ID.

Si aucune transition "To Merge" n'existe dans la liste, arrête-toi et signale les statuts disponibles à l'utilisateur plutôt que de choisir une transition approchante.
