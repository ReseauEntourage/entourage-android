---
name: ent:prepare-commit
description: Prépare un commit Git (commande ent-prepare-commit) — vérifie si l'option "Enable staging area" est activée dans Android Studio, récupère les fichiers sélectionnés, propose une stratégie de test (unitaires/intégration), propose d'implémenter ces tests, et génère un message de commit structuré avec préfixe et clé JIRA.
---

Cette commande / skill (`ent-prepare-commit` / `ent:prepare-commit`) guide l'agent pour préparer un commit complet et rigoureux à partir des fichiers sélectionnés dans Android Studio et Git.

## Procédure à exécuter lors de la demande "ent-prepare-commit" ou "prépare le commit"

### 1. Vérification de la Staging Area & Récupération des fichiers sélectionnés
1. Obtenir la branche courante (`git branch --show-current`).
2. Extraire la clé du ticket JIRA depuis le nom de la branche si présente (regex : `[A-Z]+-[0-9]+`, ex. `EN-1234`).
3. **Vérifier si des fichiers sont staged (`git --no-pager diff --cached --name-only`) :**
   * **Si `git --no-pager diff --cached` contient des fichiers :** L'option *"Enable staging area"* d'Android Studio est active (ou les fichiers ont été indexés via `git add`). Utiliser exactement cette liste de fichiers staged.
   * **Si `git diff --cached` est VIDE :**
     1. Afficher une remarque prévenant l'utilisateur que l'option *"Enable staging area"* n'est pas activée dans Android Studio (**Settings > Version Control > Git > Enable staging area**), ce qui empêche Git de connaître la sélection exacte faite via les cases à cocher de l'IHM.
     2. Récupérer la Changelist active par défaut dans `.idea/workspace.xml` (balises `<change beforePath="...">` du composant `<component name="ChangeListManager">`) et compléter avec `git status --porcelain`.

### 2. Proposition de stratégie de test
Pour l'ensemble des fichiers récupérés à l'étape 1 :
- **Tests Unitaires (Local JVM / `app/src/test`) :** Proposer les cas de tests unitaires à ajouter ou exécuter pour valider la logique métier, les helpers ou la rétrocompatibilité des fichiers modifiés.
- **Tests Instrumentés & Intégration (`app/src/androidTest` / Espresso) :** Proposer les tests d'interface pour valider les activités, composants ou écrans impactés.
- **Vérifications Manuelles :** Indiquer les points d'attention particuliers (RTL, clavier, permissions, layouts, etc.).

### 3. Proposition d'implémentation des tests
- Proposer à l'utilisateur d'implémenter automatiquement les tests unitaires et/ou d'intégration proposés avant de procéder au commit.

### 4. Génération du message de commit
Générer un message de commit structuré basé sur le périmètre des fichiers récupérés :
- **Préfixe approprié :**
  - `feat:` nouvelle fonctionnalité
  - `fix:` correction de bug
  - `chore:` refactorisation, nettoyage du code, maintenance, dépendances, styles/formatage, tests
  - `doc:` documentation
- **Format du sujet :** `préfixe(portée): résumé court [JIRA-KEY]` ou `préfixe(JIRA-KEY): résumé court`
- **Corps du message :** Liste à puces résumant précisément chaque modification technique présente dans les fichiers sélectionnés.
