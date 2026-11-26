# Inventaire — parcours de bienvenue (home)

Sources : `res/layout/home_welcome_journey.xml`, `home/HomeWelcomeJourneyAdapter.kt`, `home/WelcomeJourneyStep.kt`,
logique de visibilité / API dans `HomeFragment.handleWelcomeJourneyState`.
Version Compose : `home/compose/HomeWelcomeJourneyCompose.kt` → `HomeWelcomeJourney(states, onStepClick, onSkipClick, onHideRequested)`.

## Répartition des responsabilités

| Sujet | Où |
|---|---|
| Visibilité globale (partenaire, masqué, retiré par le back, inactivité, « tout fait au lancement »…) | fragment (`handleWelcomeJourneyState`), hors composable |
| Appels API passer / masquer, mise à jour optimiste, toast d'échec | fragment |
| Clic étape (vidéo, groupes nationaux, visio, événements), analytics `ACTION__HOME__WELCOME_JOURNEY_SKIP_STEP` / `..._HIDE` | fragment (callbacks) |
| Confirmation « Masquer le parcours » (bottom sheet `dialog_hide_welcome_journey`) | fragment, déclenchée par `onHideRequested` |
| Rendu des 4 états, barres, microcopy, félicitations, a11y | composable |
| Suppression des suggestions quand le parcours est visible (`onVisibilityChanged`) | fragment / `HomeUiState` |

Aucun événement analytics n'est émis par l'adapter lui-même (tous partent du fragment).

## Structure (de haut en bas)

Racine : marges extérieures 16 dp haut/bas, `entourage_marginstart` / `entourage_margin_end` (16 dp) côtés.

1. **Titre** « Votre parcours de bienvenue » : Quicksand Bold, `entourage_font_large` (18 sp), `grey_dark`. Largeur = tout sauf 40 dp.
2. **Bouton « ⋯ »** : 40×40 dp, centré verticalement sur la hauteur du titre (peut déborder), « ⋯ » Quicksand Bold 20 sp `grey_dark`, ondulation non bornée, contentDescription `welcome_journey_options`. Ouvre le menu (entrée `welcome_journey_hide_menu`) → `onHideRequested`.
3. **Barres de progression** : marge haute 12 dp, hauteur 6 dp, 4 segments de même poids, 6 dp d'écart, rayon 3 dp ; plein = `orange` si l'étape est DONE (jamais SKIPPED), vide = `grey_middle`. a11y : un seul nœud, contentDescription plural `welcome_journey_progress_a11y` (« X étape(s) faite(s) sur 4 »).
4. **Microcopy** : marge haute 8 dp, Nunito Sans Regular 13 sp `grey` ; `welcome_journey_subtitle_start` si 0 étape faite, sinon `welcome_journey_subtitle_progress`.
5. **Si les 4 étapes sont DONE** : encart succès (marge haute 16, padding 16, fond `green_light` rayon 12, texte `welcome_journey_complete` centré, Quicksand Bold 15 sp `green`) et aucune carte d'étape.
6. **Sinon 4 cartes** (marge haute 16 dp pour la 1re, 12 dp ensuite) : fond blanc / vert clair, contour 1 dp, rayon 12, padding 16 dp, carte entière cliquable (sans ondulation, `onStepClick(n)`).

### Carte d'étape (index n = 1..4)

- Pastille icône 40 dp (cercle), icône 24 dp (padding 8), masquée à TalkBack.
- Titre (Quicksand Bold 15 sp) + badge (Nunito Sans Bold 13 sp, padding 8×2, rayon 16) alignés au centre vertical du titre ; titre marge fin 8 dp.
- Description (Nunito Sans Regular 15 sp, marge haute 4 dp), alignée sur le début du titre.
- Textes des étapes : 1 « 1 min pour tout comprendre » / 2 « Rejoignez un groupe qui vous ressemble » / 3 « Rencontrez l'équipe en visio » / 4 `welcome_step4_title` (descriptions littérales en 1-3, `welcome_step4_desc` en 4).

| État | Fond / contour carte | Titre / desc. | Badge | Icône (fond) | Bouton | « Passer » | « Découvrir » |
|---|---|---|---|---|---|---|---|
| TODO | blanc / `orange_light_separator_event` | noir / `grey` | « À faire », texte `a11yTextOrange`, fond `#FFE8D9` | todo (`a11yButtonFill`) | oui : plein (`a11yButtonFill`, texte blanc) si c'est la 1re étape TODO, sinon contour orange 1,5 dp, fond blanc, texte `a11yTextOrange` | oui (`welcome_journey_skip_step`) → `onSkipClick(n)` | non |
| DONE | `green_light` / `green`, **carte à 50 % d'opacité** | `green` / `green` | « Terminé », `green`, fond transparent + contour 1 dp `green` | check (étapes 1, 3, 4) ou groupes (étape 2), fond `green` | seulement étape 1 : « Revoir la vidéo », fond `green` ; sinon aucun | non | non |
| SKIPPED | blanc / `grey_onboarding_item_border` | `grey` / `grey` | `welcome_journey_badge_skipped`, `grey`, fond `white_f5_f5_f6` | todo (`a11yButtonFill`) | aucun | non | oui (`welcome_journey_redo_step`, `orange_entourage` 13 sp, min 40 dp) → `onStepClick(n)` |

Libellés du bouton TODO : 1 `welcome_video_cta`, 2 « Voir les groupes », 3 « Voir les prochaines dates », 4 `welcome_step4_cta`.
Bouton Material : hauteur de vue 48 dp (min AppCompat), fond dessiné 36 dp (insets 6 dp), rayon 32 dp, padding 24×10 dp, Quicksand Bold 14 sp, interlettrage 0,07.
« Passer » : min 40 dp, padding horizontal 16 dp, centré, Quicksand Bold 13 sp `grey`, sous le bouton (en dessous de la vue de 48 dp).

## Accessibilité (TalkBack)

- Ordre de focus = ordre XML : titre, « ⋯ », barres (nœud unique avec la description du plural), microcopy, puis par carte : textes de la carte (carte cliquable = nœud fusionné titre + badge + description), bouton, « Passer » ; « Découvrir cette étape » est un nœud séparé (cliquable).
- Icônes sans description (`importantForAccessibility=no`).
- Mode contraste amélioré : `a11yTextOrange` (badge TODO, texte du bouton secondaire) et `a11yButtonFill` (bouton plein, pastille icône) lus via `Context.a11yColor` comme l'adapter ; le reste utilise les couleurs fixes de l'XML. Le mode est fixé au lancement d'une activité : pas de recomposition dédiée.
- Aucune annonce `announceForAccessibility` dans l'adapter.

## Animations / états de chargement

Aucune animation propre au parcours (le changement d'états est instantané : `notifyItemChanged` avec payload). Pas de squelette. L'animation d'entrée de la liste (`layout_animation_fall_down`) relève de l'écran, pas du composable.

## Écarts visuels connus ou probables

1. **Menu « ⋯ »** : `PopupMenu` (menu système, ancré au bouton) → `DropdownMenu` M3 (style Material 3, position et ombre différentes).
2. **Ondulations** : la ripple du bouton « ⋯ » (`selectableItemBackgroundBorderless`) et du `MaterialButton` (teinte, durée) est celle de Compose, pas exactement celle du système / de Material Components. La carte est cliquable sans ondulation dans les deux versions.
3. **Contour du bouton secondaire** : `MaterialButton` dessine le contour de 1,5 dp à cheval sur le bord du fond ; Compose le dessine à l'intérieur — écart possible d'un demi-pixel de rayon.
4. **Hauteur de ligne des textes** : `includeFontPadding = true` est appliqué pour reproduire les `TextView`, à vérifier au pixel sur appareil (Quicksand / Nunito Sans).
5. **Interlettrage du bouton** : 0,07 supposé issu de `TextAppearance.MaterialComponents.Button` (thème de l'app non surchargé trouvé) ; à confirmer.
6. **Bouton « ⋯ »** : le débordement du bouton de 40 dp au-dessus de la ligne de titre est reproduit, mais l'ancien layout le rognait au bord de l'item (`clipChildren`) ; en Compose il n'est pas rogné (seule la zone d'ondulation, non bornée, est concernée).
7. **Alpha de la carte DONE** : appliqué comme calque Compose (`alpha`), résultat visuel identique attendu, rendu du contour à vérifier.
8. **Rôle TalkBack** : les boutons d'étape annoncent « Bouton » via `Role.Button` (class `MaterialButton` côté vue) ; les cartes cliquables n'ont pas de rôle (comme avant).

Non vérifiable sans appareil : tous les points ci-dessus (rendu, ripple, ordre de focus TalkBack réel, comportement du menu).
