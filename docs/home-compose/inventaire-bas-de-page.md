# Inventaire — bas de page de la home (sous-tâche C)

Code Compose : `app/src/main/java/social/entourage/android/home/compose/HomeBottomCompose.kt`.
Sources XML / adapters d'origine conservés (rien supprimé).

## 1. Carte pédago (liste verticale) — `HomePedagoCard`
Source : `home_v2_pedago_item_layout.xml` + `HomePedagoAdapter`. **Item de liste VERTICALE** (ajouté directement à la ConcatAdapter, sous l'en-tête « Découvrir des contenus » ; `HomeFragment.handlePedago` ne garde que 3 contenus non lus max).
- Marges 16 / 10 (haut) / 16 dp, hauteur 90 dp, fond `home_v2_action_item_shape` (blanc, bordure beige 1 dp, rayon 5 dp), pas de ripple.
- Vignette 90 dp pleine hauteur : fond blanc arrondi 15 dp, Glide `CenterCrop + GranularRoundedCorners(16 px)`, placeholder/erreur `ic_event_placeholder`; sans URL : `ic_placeholder_action`. `importantForAccessibility=no`.
- Pastille catégorie (Tout/Agir/Inspirer/Comprendre/vide) : rayon 12 dp, padding 4/6 dp, Quicksand Bold 12 sp, couleurs par catégorie (`pedago_text_*`/`pedago_bg_*`, « Tout » et vide = `a11yTextLightOrange` sur `beige_clair`).
- Durée : icône `ic_time_grey` 10 dp + « %d min » (Nunito Sans Regular 12 sp, `grey`), masquées si `duration == null`.
- Titre Quicksand Bold 14 sp noir, 2 lignes max, ellipsis.
- Clic : analytics `Action_Home_Article` puis `OnItemClick.onItemClick(pedago)` (ouvre `PedagoDetailActivity` si `html` et `id` — logique laissée à l'appelant).
- TalkBack : carte cliquable fusionnée, ordre pastille → durée → titre (images muettes).

## 2. Carte pédago « sensibilisation » (liste horizontale) — `HomeInitialPedagoCard`
Source : `home_v2_initial_pedago_item_layout.xml` + `HomeInitialPedagoAdapter`. **Item de liste HORIZONTALE** (wrapper `HomeHorizontalWrapperAdapter`) : conteneur fourni par l'appelant (sous-tâche B).
- 300 x 130 dp, marge droite 10 dp, haute 10 dp (pas de marge gauche : le padding de liste est celui du conteneur).
- Pastille à 10 dp du haut, titre dessous (4 dp), 3 lignes max sans ellipsis, durée « %d min » ancrée en bas (10 dp), sans icône horloge. Même image, mêmes clics et analytics que ci-dessus.
- Affichée seulement si `suggestionSection == null`, contenus non vides et `involvements` contient « resources » (logique dans `handleInitialPedago`).

## 3. Outils solidaires + bandeau de don — `HomeTools`
Source : `layout_home_tools.xml` + `HomeToolsAdapter` (1 item, toujours affiché, même avec `SuggestionFeature.ENABLED`).
- Padding 16/20 dp, titre « Vos outils solidaires » Quicksand Bold 18 sp, marge basse 12 dp.
- 3 cartes de poids égal (marge droite 8 dp sur les deux premières), hauteur mini 140 dp et hauteur commune (`IntrinsicSize.Min`), padding 12 dp, fond action item. Icône 48 dp ronde (`#FEF5EB`, padding 10 dp), libellé centré (Quicksand Bold 14 sp) centré verticalement dans l'espace restant.
- Clics (la logique reste à l'appelant) : carte → `GDSMainActivity` + analytics `Action__Home__Map` ; ressources → `PedagoListActivity` + `Action__Home__Pedago` ; charte → lien externe (debug/prod) sans analytics.
- Bandeau don : marge haute 16 dp, fond blanc, bordure `#FEEAE3` 1 dp, rayon 16 dp, padding 12 dp ; icône cœur 40 dp (muette) ; titre Quicksand Bold 16 sp ; texte Nunito Sans 13 sp ; bouton « Faire un don » (fond `a11yButtonFill`, rayon 22 dp, mini 44 dp, texte blanc Quicksand Bold 14 sp). Clic bouton seul : analytics `Click_Home_Donation_Banner` + `http://don.entourage.social/?cid=97`.

## 4. Squelette — `HomeSkeleton`
Source : `home_skeleton_horizontal_list.xml`, `home_skeleton_item.xml`, `HomeSkeletonAdapter` (4 blocs).
- Bloc : marge haute 20 dp, padding gauche 16 / bas 20 dp ; faux titre 150x20, faux sous-titre 200x14 (+8 dp), liste shimmer (+16 dp) de 3 cartes 160 dp (fond `#F5F5F5` = drawable `rounded_layout` de la lib Firebase IAM, rayon 4 dp, padding 8 dp, marge droite 16 dp) : image 90, barre 16 (+12), barres 12 (100 et 80 dp, +8, marge basse 8), tout en `#E0E0E0`.
- Liste plus large que l'écran, rognée par le conteneur (comme le `LinearLayout` wrap dans le `ShimmerFrameLayout`).
- Shimmer : facebook/shimmer 0.5.0 par défaut (alpha 0,3 → 1,0, 1 000 ms, linéaire, inclinaison 20°, dropoff 0,5, répétition infinie) reproduit par un masque DST_IN en dégradé animé.
- Pas de sémantique (aucun texte, aucun contentDescription d'origine).

## 5. Cartes de suggestion (ancienne version) — `HomeSuggestionConnectionCard`, `HomeSuggestionNextStepCard`
Source : `home_suggestion_*_item.xml` + adapters. Type : `social.entourage.android.api.model.Suggestion`. Affichées seulement si `suggestionSection == null` (flag `SuggestionFeature.ENABLED` à `false` aujourd'hui) et si `homePresenter.suggestionData` fournit `connection` / `nextStep`.
- Marges 16/8 dp, padding 16 dp, fond action item.
- Connexion : avatar 48 dp (liseré `light_orange`, padding 2 dp, Glide `circleCrop`, placeholder `placeholder_user`), prénom Quicksand Bold 16 sp (1 ligne), raison Nunito 14 sp `grey_dark` (2 lignes), boutons ancrés **sous l'avatar** (comme le XML : ils peuvent chevaucher une raison sur 2 lignes) : CTA « Dire bonjour » (fond `a11yButtonFill`, rayon 8 dp, Nunito Bold 14 sp) et « Pas maintenant » (bouton texte).
- Prochain pas : en-tête « TON PROCHAIN PAS » (`a11yTextOrange`, Nunito Bold 12 sp, majuscules), titre (entité ou raison, 2 lignes), raison, CTA selon `suggestedAction` (7 libellés, défaut « Rejoindre l'événement ») et « Pas maintenant ».
- Callbacks : `onAction(suggestion)` / `onDismiss(suggestion)`. La logique (conversation, `dismissSuggestion`, ouverture `EventFeedActivity`) et le retrait de la carte restent à l'appelant (`HomeFragment`).
- Boutons : vue 48 dp mini, fond 36 dp (inset 6 dp), texte en MAJUSCULES, espacement 0,089 em (style `TextAppearance.MaterialComponents.Button`, `textAllCaps` non surchargé), élévation 2 dp sur le CTA, ripple clippé.

## Écarts visuels connus ou probables (à vérifier sur appareil)
1. **Shimmer** : masque DST_IN approximé (rotation 20° autour du centre, translation de -w à +w) ; la géométrie exacte de `ShimmerDrawable` (taille du masque inclinée, interpolation) peut différer de quelques pixels/millisecondes.
2. **Boutons Material** : hauteur (48/36 dp), élévation 2 dp et ripple reconstruits à la main ; l'ombre Compose (`shadow`) n'est pas exactement celle du `MaterialButton` (stateListAnimator, couleur d'ombre).
3. **Texte** : `includeFontPadding=true` reproduit le padding des TextView ; l'interligne exact de Quicksand/Nunito peut différer d'un pixel (pas de `lineSpacingMultiplier` dans ces layouts).
4. **Bordure des cartes** : `Modifier.border` est intérieure à la forme, le `<stroke>` XML est centré à 0,5 dp du bord ; le fond blanc à angles droits sous la bordure est conservé comme dans le layer-list.
5. **Pastille catégorie vide** (`category == null`) : pastille de 12 dp de large conservée comme l'original (artefact d'origine, pas un choix).
6. **Pas de ripple** sur cartes, outils et bouton de don (conforme à l'original) ; ripple présent sur les boutons Material.
7. **Images** : Glide dans un `ImageView` (`AndroidView`), recharge uniquement si l'URL change ; pas de transition de fondu supplémentaire.
8. Pastille/durée de la carte verticale : `ConstraintLayout` d'origine laissait la pastille déborder sans contrainte de fin ; ici `Row` (pas de scénario réaliste de débordement).
9. La drawable `rounded_layout` du squelette vient de la lib `firebase-inappmessaging-display` (`#F5F5F5`, 4 dp) — dépendance implicite héritée de l'XML.
