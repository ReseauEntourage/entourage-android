## Why

Une partie des utilisateurs d'Entourage est malvoyante, mais l'app est difficile à lire en basse vision. L'orange de la marque est utilisé pour du texte avec un contraste d'environ 2,2:1 sur blanc. Des cartes à hauteur fixe coupent le texte quand la police système est agrandie. Les informations clés, comme le lieu d'un événement, sont tronquées sur une ligne. Entourage n'est pas soumise à l'article 47 de la loi 2005-102. La démarche est volontaire, mais elle s'aligne sur le RGAA 4.1.2 (WCAG 2.1 AA) comme grille de référence.

## What Changes

- Démarche **par parcours** : chaque lot rend un parcours complet utilisable en basse vision avant de passer au suivant. Ordre retenu :
  1. Accueil
  2. Conversation (lire et répondre)
  3. Événement (liste, détail, inscription)
  4. Connexion / inscription
- **Lot 1 (cette change) : l'accueil.** Il couvre `fragment_home` et les cartes et sections qu'il affiche (événements, groupes, actions, aides, contenus pédagogiques, parcours de bienvenue, suggestions, outils).
- Le texte reste entièrement lisible jusqu'à la taille de police et d'affichage système maximale (environ 200 %). Il n'y a plus de conteneur de texte à hauteur fixe, ni de `maxLines="1"` sur une information clé.
- Contraste du texte : au minimum 4,5:1 pour le texte courant et 3:1 pour le gros texte et les composants. **L'orange de la marque `#FF9739` n'est pas modifié.** Il reste un fond ou un accent, et le texte posé dessus ou à sa place passe en couleur foncée.
- Un état (sélectionné, urgent, nouveau) n'est jamais signalé par la couleur seule.
- Le texte porteur de sens fait au minimum 12sp.
- Les images de l'accueil sont soit décrites, soit marquées décoratives, pour TalkBack (priorité secondaire).
- Un garde-fou lint est ajouté : les contrôles d'accessibilité d'Android Lint passent en erreur pour ne pas réintroduire de défauts.
- Hors périmètre : les lots 2 à 4 (changes suivantes), le mode sombre, la déclaration d'accessibilité légale et iOS (chantier parallèle qui reprend les mêmes décisions de design).

## Capabilities

### New Capabilities
- `accessibilite-basse-vision` : exigences de lisibilité en basse vision (agrandissement du texte, contraste, signification non portée par la seule couleur, taille minimale, alternatives textuelles). Elles s'appliquent écran par écran, en commençant par l'accueil.

### Modified Capabilities
<!-- Aucune -->

## Impact

- **Layouts** : `fragment_home`, `home_v2_*_item_layout`, `home_welcome_journey`, `home_suggestion_*_item`, `home_section_button`, `layout_home_tools`.
- **Styles et couleurs** : `values/styles.xml` (`orange_button`, `h2_orange`, `h3_orange`, `left_h2_orange`, `selected_filter_orange`…). Modifier un style partagé change aussi d'autres écrans hors de l'accueil. C'est voulu, mais il faut le vérifier visuellement.
- **Kotlin** : les adapters de `home/` qui fixent des couleurs ou des textes par programme (66 références à `R.color.orange*` dans toute l'app, à trier).
- **Build** : `app/build.gradle.kts`, bloc `lint` (sévérité des contrôles d'accessibilité, avec éventuellement une baseline pour l'existant hors accueil).
- **Tests** : le scénario e2e de l'accueil, sur la branche `end_to_end_test`, est à mettre à jour.
- **Design** : changement visuel des textes orange, à faire valider par la personne qui porte la marque.
