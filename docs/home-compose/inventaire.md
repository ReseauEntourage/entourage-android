# Inventaire de la home Android (avant passage en Compose)

Écrit à partir de `fragment_home.xml`, `HomeFragment.kt`, des adapters `Home*Adapter` et de `suggestion/`.
Détails par bloc : `inventaire-parcours.md`, `inventaire-sections.md`, `inventaire-bas-de-page.md`.

## 1. Architecture d'origine

`HomeFragment` (navigation_home.xml) : `fragment_home.xml` (ConstraintLayout) + `rv_home` (RecyclerView, `ConcatAdapter` à `isolateViewTypes`,
`itemAnimator = null`, `layoutAnimation = layout_animation_fall_down`).
Les adapters sont ajoutés au `ConcatAdapter` **une seule fois**, quand les 4 réponses initiales sont arrivées
(`totalchecksum >= 4` : événements, entraides, sensibilisation, contenus pédagogiques) ; avant, un squelette (4 blocs shimmer).

## 2. Écran (`fragment_home.xml`)

| Élément | Comportement |
|---|---|
| `iv_fond_home` | drawable `header` (vecteur 320x400 dp), `fitXY`, largeur écran, hauteur 400 dp, hors TalkBack |
| `home_header` | bloc wrap_content ; `topMargin` 0 au départ puis 80 px / 10 px au défilement (§3) ; `paddingTop` = hauteur de la barre d'état |
| `avatar` | 48x48 dp, bas du bloc, marge début 16 dp, Glide `circleCrop` + `placeholder_user` (sans URL : placeholder seul), `contentDescription` « Mon profil », clic -> `MyProfileFullActivity` + analytics `Action__Tab__Profil` |
| `iv_logo_home` | `logo_entourage` 148x29, centré à droite de la marge de début, `contentDescription` = nom de l'app ; clic **debug uniquement** -> parcours de bienvenue vierge (panneau de debug) |
| `ui_layout_notif` | cible 48 dp, fond `bg_partner_logo_inset6`, marge haute 4 dp, fin 16 dp, `contentDescription` « Notifications » ; clic -> `InAppNotificationsActivity` (extra `NOTIF_COUNT`) + analytics `Action__Home__Notif` |
| `ui_bell_notif` | `ic_notif_off` 24x22, hors TalkBack |
| `card_notif_number` | pastille 18 dp (`bg_main_filter_item_number_shape`, `a11yBadgeFill`), Quicksand Bold 13 sp blanc ; visible avec « 9+ » au départ ; `count>9` -> « 9+ », `>0` -> nombre, `0` -> INVISIBLE |
| `home_title` | `home_title`, Quicksand Bold 12 sp, blanc, centré entre avatar et cloche ; affiché/masqué au défilement |
| `rv_home` | marginTop 10 dp, paddingTop 20 dp, paddingBottom 50 dp, `clipToPadding=false`, fond `home_scrollview_shape` (`very_very_clear_orange`, coins hauts 35 dp), **`clipToOutline=true`** (contenu rogné sur l'arrondi), scrollbar verticale, animation d'entrée |
| `chatbotButton` (Lottie) | `gone`, jamais rendu visible : **code mort**, non porté |
| `progressBar` | mis à GONE à la création, jamais affiché : code mort |
| `frame_layout_change_location` | `gone`, jamais affiché (reliquat `OnHomeChangeLocationUpdate`) : code mort |
| `bouncing_heart` | `invisible`, `startBouncingHeart()` jamais appelé : code mort (cœur rebondissant, message de courage), non porté |

## 3. Défilement de l'en-tête

À chaque `onScrolled`, `scrollY = computeVerticalScrollOffset()` :
- `scrollY == 0` : marge = 80 px, titre visible (sans animation) ;
- `scrollY > 50`, `dy > 0`, titre visible : animation 100 ms de la marge vers 10 px, titre GONE ;
- `scrollY <= 50`, `dy < 0`, titre GONE : animation 100 ms vers 80 px, titre visible.
Valeurs en **pixels**. Pendant l'animation, les défilements sont ignorés.

## 4. Contenu de la liste, dans l'ordre

Squelette (4 blocs shimmer) puis, une fois les 4 réponses reçues :

1. **Parcours de bienvenue** : visible par défaut (états TODO) ; règles de `handleWelcomeJourneyState` (partenaire, retiré par le back, masqué par l'utilisateur, tout fait/passé au lancement, inactivité 14 jours). Voir `inventaire-parcours.md`.
2. Si `SuggestionFeature.ENABLED` (actuellement **faux**) : « Votre prochain pas » = card héros puis zone Explorer, supprimées tant que le parcours est visible.
   Sinon (cas actuel) :
   - Sensibilisation : en-tête + liste horizontale, visible si liste non vide **et** implication `resources` de l'utilisateur.
   - Entraides ou contributions (selon `summary.preference`) : en-tête (titres différents en contribution), liste horizontale, bouton « voir plus » (`goDemand`/`goContrib` + analytics `Action_Home_Demand_All`/`Action_Home_Contrib_All`) ; masqués si liste vide.
   - Événements : en-tête, liste horizontale, bouton (`goEvent` + `Action_Home_Event_All`) ; masqués si liste vide.
   - Carte « connexion » puis carte « prochain pas » (suggestions du back), actions / fermeture (`dismissSuggestion` « actioned » / « dismissed »).
   - Groupes : **toujours masqués** (jamais affichés), section non portée.
3. Outils solidaires (carte, pédagogie, charte, bandeau de don).
4. Contenus à découvrir : en-tête (visible s'il reste >= 1 contenu non lu) + liste verticale (3 non lus maximum).

`HomeHelpAdapter` / `home_v2_help_item_layout` : non utilisés, non portés.

## 5. Animation d'entrée

`layout_animation_fall_down`, une seule fois, quand les adapters remplacent le squelette. Par item : descente de 20 % de sa hauteur + fondu 0->1, 500 ms, décélération, décalage de 75 ms par position. Les items qui apparaissent plus tard ne s'animent pas.

## 6. Données et rafraîchissement

- `onResume` : `resetFilter`, `callToInitHome` (événements, contenus pédagogiques, contenus initiaux, compteur de notifications, utilisateur), `suggestionSection.onResume()`, `getUnreadCount()`, `sendUserDiscussionStatus`, vidéo de bienvenue si demandée par lien profond.
- Observateurs : `summary` (parcours, popups d'action non clôturée, préférence contribution -> `getAllContribs`/`getAllDemands`), `getAllEvents`, `getAllActions`, `pedagogicalContent` (-> `getSummary()`), `pedagogicalInitialContent`, `notifsCount`, `welcomeResource`, `suggestionData`, `unreadMessages` (badge de la barre du bas), `newConversation`.
- Pas de pull-to-refresh.
- Garde d'entrée : onboarding critique (but/zone), demande de notifications, intro de l'onboarding amélioré, une fois par utilisateur.
- Divers : jeton FCM, compteur `COUNT_DISCUSSION_ASK`, bouton retour (pile enfants/parente puis `finish`).

## 7. Accessibilité

- `contentDescription` : avatar « Mon profil », logo = nom de l'app, cloche « Notifications » ; fond et cloche graphique hors TalkBack ; pastille lue comme un texte.
- Ordre de focus : ordre du layout (avatar, logo, cloche, pastille, titre, puis la liste dans l'ordre du §4).
- Contraste amélioré (`AccessibilitySettings`, attributs `?attr/a11y*`) : couleurs lues via `a11yColor` / drawables themés.

## 8. Interrupteurs

- `SuggestionFeature.ENABLED` (faux) conservé ; la home Compose lit les mêmes états (`HeroItem()` / `ExplorerItem()` de `HomeSuggestionSection`).
- `HomeComposeFeature.ENABLED` (nouveau, `true`) : `HomeComposeFragment` à la place de `HomeFragment` via `HomeFragmentFactory` (posée dans `MainActivity.onCreate`). `false` : ancienne home.

## 9. États partagés

`HomeFragment.isContribProfile`, `signablePermission`, `summaryEventsOverride` (e2e) : `HomeComposeFragment` les lit et les écrit via ceux de `HomeFragment`.
