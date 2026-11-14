## Context

Voir proposal.md, section « Why », pour la motivation. Ce que montre le code de l'accueil :

- **Oranges utilisés pour du texte** (contraste sur blanc) :
  - `orange` `#FF9739` : environ 2,2:1
  - `light_orange` `#ff9c5d` : environ 2,1:1
  - `orange_entourage` `#F55F24` : environ 3,2:1
- **Contraste du noir sur ces oranges** : environ 9,7:1 sur `#FF9739` et environ 6,5:1 sur `#F55F24`.
- **Styles partagés à texte orange** : `orange_button`, `h2_orange`, `left_h2_orange`, `selected_filter_orange`, etc. dans `values/styles.xml`. Les modifier touche aussi d'autres écrans que l'accueil.
- **Conteneurs de texte à hauteur fixe sur l'accueil** :
  - `home_v2_help_item_layout` : 65dp
  - `home_v2_pedago_item_layout` : 90dp
  - `home_v2_group_item_layout` : 130dp, avec un badge de 24dp
  - `home_v2_initial_pedago_item_layout` : 130dp
  - badge de notifications : 18dp
  - `btn_more` du parcours de bienvenue : 40dp
- **Infos clés tronquées par `maxLines="1"`** : lieu d'un événement, nom dans une suggestion, auteur d'une action.
- **Textes de 10 à 11sp** : tags d'un événement (urgence, femmes, Entourage), badge de la carte climat.
- **Ce qui est déjà correct** : la taille de texte est en `sp` partout (XML et Compose), et l'app ne force pas sa propre taille de police (`fontScale`).
- **Lint** : `abortOnError = false`. Aucun contrôle d'accessibilité n'est bloquant.
- **Contrainte** : la valeur des couleurs orange de la marque ne change pas.

## Goals / Non-Goals

**Goals :**
- Fixer des règles de mise en page et de couleur réutilisables pour les lots suivants (conversation, événement, connexion).
- Corriger l'accueil sans casser visuellement les autres écrans qui partagent les mêmes styles.
- Empêcher la régression grâce au lint.

**Non-Goals :**
- Refaire la charte graphique ou toucher les valeurs des oranges.
- Mettre en place un mode sombre (`values-night`).
- Faire un audit RGAA complet et chiffré.
- Rendre l'app pleinement utilisable avec TalkBack. Seules les alternatives textuelles de l'accueil sont traitées ici.

## Decisions

### 1. Le texte n'est plus orange, l'orange devient un accent

Une nouvelle couleur sémantique `text_accent` est ajoutée dans `colors.xml`, avec une valeur foncée (noir ou gris très foncé, 4,5:1 minimum sur blanc et sur les fonds beiges). Les textes aujourd'hui orange passent sur `text_accent`. L'orange de la marque reste présent sous forme de fond, de bordure, d'icône, de soulignement ou de puce.

- **Exception autorisée :** `orange_entourage` (environ 3,2:1) peut rester sur du **gros texte**, c'est-à-dire 18sp et plus, ou 14sp et plus en gras. Le seuil de 3:1 est alors atteint.
- **Texte posé sur un fond orange :** il passe en couleur foncée plutôt qu'en blanc.
- **Alternatives écartées :**
  - Foncer l'orange : c'est contraire à la contrainte de marque.
  - Garder l'orange en ajoutant une ombre ou un contour au texte : c'est peu lisible et ça ne garantit pas le contraste mesuré.

### 2. On corrige les styles partagés, pas chaque layout un par un

Les styles `*_orange` de `styles.xml` sont modifiés directement. Ils sont réutilisés ailleurs que sur l'accueil, et ce gain de contraste y est aussi souhaitable.

- Chaque écran impacté hors accueil est listé dans les tâches pour une vérification visuelle.
- Les `textColor="@color/orange"` en dur dans les layouts de l'accueil et les `R.color.orange*` en Kotlin dans `home/` sont traités au cas par cas.
- **Alternative écartée :** créer des styles `*_a11y` parallèles. Ça double la maintenance et laisse les autres écrans en défaut.

### 3. Pas de hauteur fixe sur un conteneur de texte

Les hauteurs fixes en `dp` deviennent `wrap_content`, avec `minHeight` pour garder l'aspect visuel actuel à taille normale. Les images gardent leur hauteur fixe, puisqu'une image n'est pas du texte. Dans les carrousels horizontaux, toutes les cartes d'une ligne ne feront plus forcément la même hauteur quand la police est agrandie, et c'est accepté.

- **Alternative écartée :** l'auto-dimensionnement du texte (`autoSizeTextType`). Il rétrécit le texte, ce qui va à l'encontre du choix de la personne.

### 4. `maxLines` seulement là où le texte complet est accessible ailleurs

- Une **info clé** (lieu, nom de personne, auteur) n'a jamais `maxLines="1"`. On passe à 2 ou 3 lignes au minimum, ou sans limite.
- Un **titre de carte** peut rester tronqué, si l'écran de détail l'affiche en entier.

### 5. La couleur ne porte jamais seule une information

Tags (urgence, femmes uniquement), filtres sélectionnés et badges « nouveau » gardent toujours un libellé texte, ou ajoutent un second indice visuel : gras, icône ou coche. La taille minimale du texte porteur de sens est de 12sp.

### 6. Garde-fou lint

Dans le bloc `lint` de `app/build.gradle.kts`, ces contrôles passent en `error` :
- `ContentDescription`
- `LabelFor`
- `TouchTargetSizeCheck` (s'il est disponible)
- `SmallSp`
- `KeyboardInaccessibleWidget`

L'existant hors accueil est absorbé par un fichier `lint-baseline.xml`, pour ne pas bloquer le build, et la baseline diminue au fil des lots. `abortOnError` reste à `false` dans un premier temps : les erreurs sont visibles dans le rapport et en CI sans bloquer les builds Bitrise. On le passe à `true` quand la baseline est stable.

- **Alternative écartée :** passer `abortOnError = true` tout de suite. Plusieurs centaines de violations existent hors du périmètre.

### 7. Protocole de vérification

Le protocole est à répéter pour chaque lot, sur émulateur :
1. Taille de police au maximum, puis taille d'affichage au maximum.
2. Parcourir l'accueil en entier et vérifier : aucun texte coupé ou superposé, aucune info clé tronquée.
3. Vérifier le contraste avec Accessibility Scanner (Google).
4. Faire un passage rapide avec TalkBack sur l'accueil.

Le scénario e2e de l'accueil, sur la branche `end_to_end_test`, est mis à jour. Il vérifie au minimum que les éléments clés de l'accueil restent affichés quand la police est agrandie.

## Risks / Trade-offs

- **[Changement visuel perçu comme une perte d'identité, avec moins d'orange dans le texte]** → On présente des captures avant / après à la personne qui porte la marque avant de fusionner. L'orange reste dominant en fonds, icônes et accents.
- **[Modifier un style partagé change des écrans hors périmètre]** → Les écrans qui utilisent ces styles sont listés, avec une vérification visuelle de chacun dans les tâches.
- **[Cartes de hauteurs inégales dans les carrousels avec une grande police]** → C'est accepté. À taille normale, `minHeight` garde le rendu actuel.
- **[La baseline lint masque des défauts existants]** → La baseline est régénérée à chaque lot, et on suit la diminution du nombre d'entrées.
- **[Couleurs fixées en Kotlin qui écrasent les styles]** → Les références à `R.color.orange*` dans `home/` sont auditées (tâche dédiée).

## Migration Plan

Il n'y a pas de migration de données. Tout passe par la livraison applicative habituelle. Pour revenir en arrière, il suffit d'annuler le commit. Le changement de style étant isolé dans `styles.xml` et `colors.xml`, on peut aussi l'annuler seul.

## Open Questions

- La valeur exacte de `text_accent` (noir pur ou gris très foncé) est à choisir avec la personne qui porte la marque. N'importe quelle valeur d'au moins 4,5:1 sur blanc et sur beige convient, et ce choix ne change ni les specs ni les tâches.
