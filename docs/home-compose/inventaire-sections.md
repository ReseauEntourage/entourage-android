# Inventaire — sections Entraides/Contributions, Événements, Groupes (home Compose)

Portée : sous-tâche B de la home Compose (branche `homeCompose`). Code : `app/src/main/java/social/entourage/android/home/compose/HomeSectionsCompose.kt`.
Sections affichées uniquement quand `SuggestionFeature.ENABLED` est faux (sinon la home n'affiche que parcours/suggestions + outils + pédago).

## Correspondance XML/adapter → composable

| Origine | Composable |
|---|---|
| `home_section_header.xml` + `HomeSectionHeaderAdapter` | `HomeSectionHeader(title, subtitle?)` |
| `home_section_button.xml` + `HomeSectionButtonAdapter` | `HomeSectionButton(text, onClick)` |
| `home_horizontal_recycler_view.xml` + `HomeHorizontalWrapperAdapter` | `HomeHorizontalList { … }` + `homeActionCards` / `homeEventCards` / `homeGroupCards` (extensions `LazyListScope`) |
| `home_v2_action_item_layout.xml` + `HomeActionAdapter` | `HomeActionCard(action, isContrib)` |
| `home_v2_event_item_layout.xml` + `HomeEventAdapter` | `HomeEventCard(event)` |
| `home_v2_group_item_layout.xml` + `HomeGroupAdapter` | `HomeGroupCard(group)` |
| `home_v2_help_item_layout.xml` + `HomeHelpAdapter` | **non portée : code mort** (`HomeHelpAdapter` n'est instancié nulle part, seul son fichier le référence) |

Les wrappers d'adapter (visibilité de la section, mise à jour des données, `isContrib`) restent à la charge de l'écran : l'écran ne
compose l'en-tête, la liste et le bouton que si la liste n'est pas vide (voir « Règles de visibilité »).

## En-tête de section
- Padding : start 16 dp (`entourage_marginstart`), end 16 dp, top 28 dp (`entourage_margin_section`).
- Titre : Quicksand Bold, `grey_dark`, `entourage_font_large` (18 sp). Sous-titre : Nunito Sans Regular, `entourage_font_small` (14 sp),
  couleur par défaut du TextView (`textColorPrimary` du thème), visible seulement s'il existe.
- Aucun `contentDescription`, pas de rôle « titre » (comme l'XML).
- Textes (fournis par l'écran) : `home_title_action(_contrib)`, `home_subtitle_action(_contrib)`, `home_title_event`, `home_subtitle_event`,
  `home_title_group`, `home_subtitle_group` (groupes : la section est masquée, `setVisible(false)` dans `handleGroup`).

## Bouton de section
- Ligne alignée à droite : padding top/bottom 10 dp (`entourage_little_margin_top`), end 16 dp ; texte Quicksand Bold 14 sp, couleur
  `?attr/a11yTextLightOrange` (résolue via `a11yColor`, suit le mode contraste amélioré), marge droite 10 dp, flèche `arrow_light_orange` 25 dp
  (`importantForAccessibility=no`).
- Toute la ligne est cliquable, sans ripple (comme `itemView.setOnClickListener`). Lu par TalkBack comme le texte du bouton.
- Actions (fournies par l'écran, inchangées) : demandes → `Action_Home_Demand_All` + `MainActivity.goDemand()` ; contributions →
  `Action_Home_Contrib_All` + `goContrib()` ; événements → `Action_Home_Event_All` + `goEvent()` ; groupes → `Action_Home_Group_All` +
  `setGoDiscoverGroupFromDeepL(true)` + `goGroup()`.

## Liste horizontale
- `marginTop=20dp`, `paddingStart=16dp`, `clipToPadding=false`, hauteur wrap, pas de décoration entre items (les marges sont dans les items).
- Animation d'entrée `layout_animation_slide_from_right` : glissement de 20 % de la largeur de l'item depuis la droite + fondu, 300 ms,
  interpolateur décélérant, décalage de 15 % de la durée (45 ms) entre items. Jouée une fois par item et par apparition de la liste
  (ne rejoue pas en défilant), comme le `layoutAnimation` d'un RecyclerView.

## Carte d'entraide / contribution
- Largeur 280 dp (`home_action_card_width`), marges start 4 dp / top 10 dp, padding interne 16 dp.
- Fond `home_v2_action_item_shape` : carré blanc (rayon 0) + bordure beige 1 dp à coins de 5 dp (les coins blancs dépassent l'arrondi, comme l'original).
- Avatar 48 dp, bordure ovale `light_orange` 1 dp, padding 2 dp, Glide `CircleCrop` ; placeholder/erreur `placeholder_user` ; sans URL :
  `placeholder_user` chargé avec CircleCrop.
- Nom : Quicksand Bold 16 sp noir, 1 ligne, ellipse. Pastille catégorie : fond `beige` coins 14 dp, padding 10/4 dp, Nunito Sans 12 sp,
  `a11yTextOrange`, texte `ActionUtils.showTagTranslated` (pastille vide si pas de catégorie, comme l'original).
- Titre : marge 16 dp, Quicksand Bold 16 sp noir, **2 lignes réservées** (`lines=2`), ellipse. Description : marge 8 dp, Nunito Sans 14 sp
  `grey_dark`, 2 lignes réservées, première lettre en majuscule.
- Distance : picto `ic_location_gds_detail` 14 dp teinté `orange`, texte Nunito Sans 12 sp `a11yTextOrange` (`Action.displayDistance`,
  vide si pas de distance), marge haute 12 dp, texte centré verticalement sur le picto.
- RTL/arabe : l'adapter force `layoutDirection` RTL (arabe) ou LTR + alignement début sur titre/pastille/distance/description ; reproduit
  par un `LayoutDirection` imposé sur chaque texte (`HomeText`).
- Clic : `Action_Home_Contrib_Detail` (contribution) ou `Action_Home_Demand_Detail` (demande), puis `ActionDetailActivity` avec
  `ACTION_ID`, `ACTION_TITLE`, `IS_ACTION_DEMAND` (= non contribution), `IS_ACTION_MINE`.
- TalkBack : l'item entier est l'unique élément focusable (cliquable, contenu fusionné), ordre de lecture : nom, catégorie, titre,
  description, distance ; avatar et pictos ignorés. Pas de `contentDescription` propre (comme l'XML).

## Carte d'événement
- CardView : largeur 290 dp, marges start 5 / top 4 / bottom 4 dp, coins 16 dp, élévation 2 dp, fond blanc, contenu rogné.
- Image 150 dp (Glide `CenterCrop`, placeholder/erreur `ic_event_placeholder`, `landscapeUrl`). Annulé/clos (`Status.CLOSED|CANCELLED`) :
  voile `background_transparent`, badge « Annulé » (`event_canceled_badge`, picto `ic_event_canceled` 10 dp blanc, fond `onboard_black_36` coins 20 dp),
  titre en gris.
- Tags bas-gauche (marge 10 dp) : « Réservé aux femmes » (violet `#7C3AED`) si `reserved_female`, sinon « Événement Entourage »
  (fond `a11yButtonFill`) si l'auteur est « Équipe Entourage » ou « Animateur Entourage » ; textes en dur comme l'original ;
  le tag « femmes » garde sa marge gauche de 6 dp.
- Infos : padding 14/12/14/14 ; urgence (`#D53F00`, 11 sp) si 1 à 5 places restantes (« Plus qu'une place » / « Plus que N places », textes en dur) ;
  titre Quicksand Bold 15 sp, 2 lignes réservées ; date (`Utils.formatEventDateWithTime`, picto `ic_date_grey` 15 dp) ; lieu (dernier segment de
  `displayAddress` après la virgule, 1 ligne, picto `ic_location_grey`) ; séparateur 1 dp `#F5F5F5` ; ligne sociale (si `membersCount` > 0) :
  jusqu'à 3 avatars 24 dp décalés de 17 dp (Glide circleCrop, placeholder/erreur `placeholder_user`) + `number_of_people` (13 sp).
- Clic : `EventsFragment.isFromDetails = true`, `Action_Home_Event_Detail`, `EventFeedActivity.isFromMyEvent = true`, `EventFeedActivity` avec `EVENT_ID`.
- TalkBack : item unique, textes lus dans l'ordre urgence ? → titre → date → lieu → participants ; texte « Annulé » et tags lus s'ils existent.

## Carte de groupe
- Carte 160×130 dp, marges start 4 / top 10 dp dans un conteneur paddingTop 8 / paddingEnd 2 dp ; fond blanc + bordure beige 1 dp, coins 15 dp.
- Image 70 dp (CenterCrop + `GranularRoundedCorners(15,15,0,0)` en **pixels**, comme l'adapter ; sans URL : coins `(15,0,0,15)`, quirk conservé).
- Nom : Quicksand Bold 14 sp noir, centré, 2 lignes réservées, centré verticalement dans les 60 dp restants.
- Pastille de messages non lus : cercle 24 dp `a11yBadgeFill`, texte 12 sp blanc (« 9+ » au-delà de 9), débordant de 6 dp à droite et 10 dp en haut de la carte.
  Visible si `unreadPostsCount` > 0 **ou nul** (le XML l'affiche par défaut avec « 1 » : comportement conservé) ; masquée au clic.
- Clic : `Action_Home_Group_Detail`, `unreadPostsCount = 0`, pastille masquée, `GroupFeedActivity` avec `GROUP_ID`.
- Note : la section Groupes est masquée dans `HomeFragment` (`groupWrapperAdapter.setVisible(false)`), le composable existe pour la parité.

## Règles de visibilité (à appliquer côté écran, depuis `HomeFragment.handle*`)
- Entraides/contributions : section visible si la liste n'est pas vide ; titre/sous-titre/bouton selon `isContribution`.
- Événements : visible si la liste n'est pas vide.
- Groupes : jamais affichés.

## Écarts visuels connus ou probables (non vérifiables sans appareil)
1. **Ombre de la carte d'événement** : `Modifier.shadow(2.dp)` n'a pas exactement le rendu de l'élévation d'un `CardView` (ombre légèrement différente).
2. **Hauteur de ligne** : les `TextView` utilisent `includeFontPadding=true` (reproduit par `PlatformTextStyle`) ; un écart de ±1 dp reste possible
   sur les polices Quicksand / Nunito Sans.
3. **Largeur de l'item groupe** : 172 dp (4 + 160 + débord de la pastille 6 + 2) estimée d'après la logique du ConstraintLayout ; à confirmer.
4. **Couleur du sous-titre de section** : la couleur par défaut d'un `TextView` (`textColorPrimary`) est résolue au runtime ; à comparer.
5. **Centrage vertical de la distance sur son picto** : reproduit par un `layout` sur mesure (moitié du débord comptée) ; écart sous le dp possible.
6. **Animation d'entrée** : déclenchée à la composition de chaque item (une fois), alors que le RecyclerView la joue à la première mise en page ;
   un item qui arrive plus tard (liste mise à jour) s'anime aussi, alors qu'il apparaîtrait sans animation dans l'ancienne liste.
7. **Images Glide** : rendues dans de vrais `ImageView` (`AndroidView`) : pas de recomposition de l'image, mais pas de recyclage de vue non plus
   (surcoût mineur dans un `LazyRow`).
8. **Pas de ripple** sur les cartes (comme l'original, qui n'en avait pas), mais l'état « focus clavier » de Compose peut différer.
