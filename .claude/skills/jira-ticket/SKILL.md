---
name: ent:jira-ticket
description: Traite un ticket Jira de bout en bout — récupération, analyse du code existant, enrichissement produit/technique posté en commentaire, implémentation, commit et passage au statut "To Merge". Invocation explicite uniquement (/ent:jira-ticket).
argument-hint: [clé-ticket]
allowed-tools: Bash, Read, Edit, Write, Grep, Glob, mcp__jira__jira_get_issue, mcp__jira__jira_search, mcp__jira__jira_add_comment, mcp__jira__jira_get_transitions, mcp__jira__jira_transition_issue
disable-model-invocation: true
---

Tu vas traiter le ticket Jira `$ARGUMENTS` de bout en bout dans ce repo Android (entourage-android).

## Étape 1 — Récupération

Récupère le ticket avec `mcp__jira__jira_get_issue` (clé `$ARGUMENTS`) : titre, description, critères
d'acceptation, commentaires existants. Si la clé est ambiguë ou introuvable, utilise
`mcp__jira__jira_search` (JQL sur le résumé) avant d'abandonner.

## Étape 2 — Analyse du code existant

Explore le repo pour comprendre ce qui existe déjà en lien avec ce ticket : fichiers concernés,
état actuel de l'implémentation, dépendances. Appuie-toi sur [CLAUDE.md](../../../CLAUDE.md) pour
te repérer rapidement (layout de `app/src/main/java/social/entourage/android/`, pattern d'accès
`EntourageApplication.get().apiModule...`, routing des deep links dans `UniversalLinkManager`,
rôles utilisateur, etc.) plutôt que de redécouvrir l'architecture à chaque fois.

Si le ticket touche un écran/une vue existante, repère aussi si un scénario e2e la couvre déjà
(ils vivent sur la branche `end_to_end_test`, pas sur `develop` — voir Étape 4).

## Étape 3 — Enrichissement du ticket

Rédige une version enrichie de la description :
- **Angle produit** : cas limites, impacts UX non mentionnés, questions ouvertes
- **Angle technique** : fichiers/composants à toucher, risques, points d'attention (variantes de
  build concernées, IDs `BuildConfig` à ne pas hardcoder, écran migré vers Compose ou non, etc.)

RÈGLE STRICTE : tu n'ajoutes JAMAIS de nouvelle fonctionnalité ni de changement de périmètre. Tu
précises et documentes ce qui est déjà spécifié, tu n'inventes rien. Si un point te semble
ambigu, signale-le comme question plutôt que de trancher.

Poste cette version enrichie en **commentaire** sur le ticket via `mcp__jira__jira_add_comment`
(ne remplace jamais la description originale).

## Étape 4 — Implémentation

Fais le travail décrit dans le ticket, dans le respect strict des specs d'origine, et des
conventions du repo (ViewBinding uniquement, IDs de layout en `snake_case`, logging via
`AnalyticsEvents.logEvent()` pour toute action utilisateur significative, strings ajoutées en
français via `./add_strings.sh` puis propagées, jamais directement dans `values-en/`).

Vérifie la compilation avant de considérer l'implémentation terminée :
```bash
./gradlew :app:compileEntourageDebugKotlin
```

**Test e2e** : si tu modifies le comportement ou la vue d'un écran, le test e2e de bout en bout
correspondant doit être ajouté/mis à jour — mais ces tests vivent sur la branche `end_to_end_test`,
pas sur `develop`. Ne modifie pas ces fichiers directement dans ce checkout. À la place :
- Signale explicitement dans ta synthèse finale le scénario e2e à créer/mettre à jour et le
  fichier concerné dans `app/src/androidTest/java/social/entourage/android/e2e/`.
- Propose à l'utilisateur de le faire dans un worktree dédié
  (`git worktree add <path> -b end_to_end_test origin/end_to_end_test`) plutôt que de le faire
  toi-même dans cette session, sauf s'il te le demande explicitement.

## Étape 5 — Commit

Commit les changements avec un message clair, dans le style conventionnel déjà utilisé dans
l'historique (`type(scope): résumé`, ex. `fix(events): ...`, `feat(discussions): ...`), en
référençant le ticket `$ARGUMENTS` (ex. suffixe `(ENT-1234)`).
- `git status` et `git diff` d'abord pour vérifier ce qui part dans le commit.
- Ajoute les fichiers un par un (pas de `git add -A`), jamais de fichier généré (`build/`,
  `.gradle/`) ni de secret.
- Ne commit jamais avec `--no-verify`.

## Étape 6 — Transition

Récupère les transitions disponibles avec `mcp__jira__jira_get_transitions` sur `$ARGUMENTS`,
trouve celle dont le nom correspond à "To Merge" (comparaison insensible à la casse), puis
applique-la avec `mcp__jira__jira_transition_issue`. Si aucune transition de ce nom n'existe dans
le workflow du ticket, ne force rien : liste les transitions disponibles à l'utilisateur et
demande laquelle utiliser.
