## 1. Préparation et état initial

- [ ] 1.1 Faire des captures de référence de l'accueil sur émulateur, à taille normale puis avec la police et l'affichage au maximum. Vérification : captures « avant » rangées dans un dossier de travail, avec les zones coupées notées.
- [ ] 1.2 Passer Accessibility Scanner sur l'accueil et noter les défauts. Vérification : liste des défauts de contraste, de libellé et de zone de clic.
- [ ] 1.3 Lister les layouts et le code Kotlin qui utilisent les styles `*_orange` de `values/styles.xml` (`orange_button`, `h2_orange`, `left_h2_orange`, `selected_filter_orange`…). Vérification : liste des écrans hors accueil à contrôler visuellement (tâche 7.3).

## 2. Couleurs et styles (contraste)

- [ ] 2.1 Ajouter la couleur `text_accent` dans `values/colors.xml`, avec une valeur foncée d'au moins 4,5:1 sur blanc et sur les fonds beiges. Vérification : rapport calculé avec un outil de contraste.
- [ ] 2.2 Dans `values/styles.xml`, remplacer la couleur de texte `orange` et `light_orange` des styles `*_orange` par `text_accent` (ou `orange_entourage` pour un style de gros texte, 18sp et plus ou 14sp et plus en gras). Vérification : `./gradlew :app:mergeEntourageDebugResources` passe.
- [ ] 2.3 Remplacer les `textColor="@color/orange|light_orange"` en dur dans les layouts de l'accueil : `home_section_button`, `home_v2_action_item_layout`, `home_v2_pedago_item_layout`, `home_v2_initial_pedago_item_layout`, `home_welcome_journey`, `home_suggestion_next_step_item`. Garder un accent orange (icône, puce, soulignement) là où l'orange porte l'identité visuelle. Vérification : `rg 'textColor="@color/(orange|light_orange)"'` ne renvoie plus rien sur ces fichiers.
- [ ] 2.4 Mettre les libellés posés sur un fond orange de l'accueil en couleur foncée au lieu du blanc. Vérification : contraste d'au moins 4,5:1 mesuré par Accessibility Scanner.
- [ ] 2.5 Auditer les `R.color.orange*` utilisés en Kotlin dans `home/` (adapters et `HomeFragment`) et remplacer ceux qui colorent du texte. Vérification : `./gradlew :app:compileEntourageDebugKotlin` passe, et aucun texte de l'accueil n'est en orange de taille courante à l'écran.

## 3. Agrandissement du texte (pas de troncature)

- [ ] 3.1 `home_v2_help_item_layout` : passer le conteneur de 65dp à `wrap_content` + `minHeight="65dp"`. Vérification : à police maximale, le titre de l'aide est entier ; à taille normale, le rendu est identique à la capture de référence.
- [ ] 3.2 `home_v2_pedago_item_layout` (90dp) et `home_v2_initial_pedago_item_layout` (130dp) : appliquer le même traitement. Vérification : identique à 3.1.
- [ ] 3.3 `home_v2_group_item_layout` : conteneur de 130dp et badge `card_news_group` de 24dp en `wrap_content` + `minHeight`. Vérification : identique à 3.1, nom du groupe et badge entiers.
- [ ] 3.4 `fragment_home` : passer le badge de notifications (`card_notif_number` et `tv_number_of_filter`, 18dp) en `wrap_content` + `minHeight`/`minWidth`. Vérification : à police maximale, un nombre à deux chiffres reste lisible.
- [ ] 3.5 `home_welcome_journey` : passer `btn_more` de 40dp à `wrap_content` + `minHeight="48dp"`. Vérification : libellé entier à police maximale.
- [ ] 3.6 Supprimer `maxLines="1"` sur les infos clés, en mettant au moins 3 lignes ou aucune limite : `tv_place_home_v2_event_item`, `tv_suggestion_name`, `tv_action_item_author`. Vérification : un lieu ou un nom long s'affiche sur plusieurs lignes (donnée de test sur staging, ou `tools:text` long dans l'aperçu).
- [ ] 3.7 Vérifier que les titres tronqués (`tv_title_event_item`, `tv_title_item_pedago`, `tv_next_step_text`, `tv_suggestion_reason`) sont affichés en entier sur l'écran de détail correspondant. Vérification : ouvrir chaque détail depuis l'accueil avec un titre long.

## 4. Taille minimale et information non portée par la couleur

- [ ] 4.1 `home_v2_event_item_layout` : passer les tags de 10–11sp à 12sp minimum (tags Entourage, femmes et urgence). Vérification : lint `SmallSp` sans alerte sur ce fichier.
- [ ] 4.2 `layout_home_tools` : passer `tv_climate_map_badge` de 11sp à 12sp minimum. Vérification : identique à 4.1.
- [ ] 4.3 Vérifier que les tags d'urgence et « femmes uniquement », le badge « nouveau » d'un groupe et les étapes terminées du parcours de bienvenue ont un libellé texte ou un indice non coloré ; en ajouter un sinon. Vérification : capture de l'accueil convertie en niveaux de gris où chaque état reste distinguable.

## 5. Alternatives textuelles (TalkBack)

- [ ] 5.1 Marquer `contentDescription="@null"` sur les images décoratives de l'accueil : fonds (`iv_fond_home`, `iv_tools_*_bg`), flèches, icônes qui doublent un texte (distance, durée, étapes). Vérification : TalkBack ne les annonce pas.
- [ ] 5.2 Ajouter un `contentDescription` aux images porteuses de sens : avatar (« Mon profil »), cloche de notifications (avec le nombre de non lues, mis à jour en Kotlin), avatars des participants d'un événement (regroupés en « N participants »), logo. Les chaînes passent par `./add_strings.sh`. Vérification : passage TalkBack sur l'accueil sans « bouton sans libellé ».
- [ ] 5.3 Pour les images de contenu des cartes (`iv_event_item`, `iv_group_item`, `iv_action_item`, `iv_pedago_item`), soit marquer `@null` si le titre de la carte suffit, soit regrouper la carte en un seul élément lu. Vérification : TalkBack lit chaque carte une seule fois, de façon compréhensible.

## 6. Garde-fou lint

- [ ] 6.1 Dans `app/build.gradle.kts`, bloc `lint`, passer en `error` les contrôles `ContentDescription`, `LabelFor`, `SmallSp`, `KeyboardInaccessibleWidget`, et `TouchTargetSizeCheck` s'il est disponible. Garder `abortOnError = false`. Vérification : `./gradlew :app:lintEntourageDebug` génère un rapport qui contient ces contrôles en erreur.
- [ ] 6.2 Générer `app/lint-baseline.xml` pour l'existant hors accueil et le référencer dans le bloc `lint`. Vérification : un nouveau lint ne remonte aucune erreur d'accessibilité sur les layouts de l'accueil, et ajouter une `ImageView` sans description dans un layout de test fait apparaître une erreur.

## 7. Vérification globale et livraison

- [ ] 7.1 Refaire le protocole sur l'accueil avec la police et l'affichage au maximum, plus Accessibility Scanner et TalkBack. Vérification : captures « après » sans texte coupé, scanner sans défaut de contraste de texte, aucun élément sans libellé.
- [ ] 7.2 Comparer les captures « avant » et « après » à taille normale. Vérification : pas de régression de mise en page hors couleurs.
- [ ] 7.3 Contrôler visuellement les écrans hors accueil listés en 1.3 (styles partagés modifiés). Vérification : chaque écran est vérifié et noté OK ou corrigé.
- [ ] 7.4 Présenter les captures avant / après à la personne qui porte la marque et faire valider la valeur de `text_accent`. Vérification : validation obtenue.
- [ ] 7.5 Sur la branche `end_to_end_test`, mettre à jour le scénario e2e de l'accueil pour vérifier que les éléments clés restent affichés (titres de cartes, lieu d'un événement, cloche). Vérification : `./gradlew :app:compileEntourageDebugAndroidTestKotlin` passe, et le scénario passe sur émulateur.
- [ ] 7.6 Transmettre à l'équipe iOS les décisions de design (texte foncé et orange en accent, pas de hauteur fixe sur le texte, pas de troncature des infos clés). Vérification : note partagée.
