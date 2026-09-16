# Changelog

## Version_14045570 — 2026-07-28

### Commits
- chore: update Bitrise configuration for Google Play deployment (ed04c980f)
- Revert "chore: preparing bump app version to 14.5" (059348116)
- Revert "chore: upgrade Gradle to 9.6.1 and update dependency versions" (1e7286f22)
- fix: change image selector (removing READ_MEDIA permissions) (2c5e8679a)
- chore: upgrade Gradle to 9.6.1 and update dependency versions (6a1af8f3d)
- chore: preparing bump app version to 14.5 (a97c63df9)
- chore: bump app version to 14.4 (28d08aa25)
- feat: implement edge-to-edge support for the Event Feed screen (f5eae4e75)
- fix: EN-9409 fix button for all navigation modes in multiples screens (createAction, login & bonnesOndes) (5b6802b21)
- Potential fix for pull request finding (700265ef0)
- feat: implement edge-to-edge support across activities and refine layout insets (69816d964)
- chore: add dev utility scripts and cleanup legacy logging, remove obsolete tests (b8912361b)
- Potential fix for pull request finding (73466e5c0)
- fix: EN-9409 fix button for all navigation modes in final Onboarding screen (b96e06254)
- feat(signup): open account creation to Germany and Switzerland (694384c75)
- feat(events): add duplicate event feature (b0647171b)
- feature(ProfileAmbassador) : add "Mes ressources bénévoles" and "Mon référent" sections (62f21e1b6)

### Code structure & content changes

- **Edge-to-edge rollout (EN-9409)**: The bulk of this release is a sweep making the app draw behind the system status/navigation bars consistently across gesture and 3-button navigation modes. `tools/Extensions.kt` gained a unified `updatePaddingForEdgeToEdge` (Activity and Fragment variants, combining status-bar top inset with `max(IME, navigation-bar)` bottom inset) alongside the older, more narrowly-scoped `updatePaddingTopForEdgeToEdge`/`updatePaddingBottomForEdgeToEdge`. Dozens of activities/fragments were migrated to call `enableEdgeToEdge()` plus the new helper: onboarding (`LoginActivity`, `LoginChangePhoneActivity`, `OnboardingEndActivity`, `OnboardingStartActivity`, `OnboardingZoneChoiceActivity`, `OnboardingAssociationChoiceActivity`, `PartnerOnboardingActivity`, pre-onboarding screens), actions (`CreateActionActivity`/Fragment, `CreateActionCGUFragment`), events (`CreateEventFragment`, `EditRecurrenceActivity`), groups (`CreateGroupFragment`, `EditGroupActivity`, `GroupCommentActivity`), profile/settings (`EditProfileActivity`, `LanguageSettingsActivity`, `SettingsNotificationsActivity`, `UnblockUsersActivity`, `OSSLibsActivity`, `PartnerDetailActivity`), small talks, survey creation, guide (`GDSMainActivity`, `GuideMapFragment`), and badges (`BadgesListActivity`). The Event Feed screen (`EventFeedActivity`/`Fragment`, `fragment_feed_event.xml`) got dedicated treatment: its root layout moved from `FrameLayout` to `ConstraintLayout` so the join button anchors to the true bottom edge instead of behind system bars.
- **Image picker permission fix**: `ChoosePhotoModalFragment` was rewritten to use the Android Photo Picker (`ActivityResultContracts.PickVisualMedia`) instead of `GetContent` + a runtime `READ_MEDIA_IMAGES`/`READ_EXTERNAL_STORAGE` permission request; the corresponding `<uses-permission>` entries were removed from `AndroidManifest.xml` with a comment warning not to re-add them (Play Console flags apps requesting those permissions when a system picker is available).
- **Ambassador profile resources ("Mes ressources bénévoles" / "Mon référent")**: `ProfileFullActivity` gained a section, visible only to users with the "Animateur Entourage" role, showing three static bordered resource cards (volunteer toolbox, event charter, WhatsApp group) plus a referent card sourced from `HomeModerator` (from `/home/summary`) with avatar-initials fallback and an "Envoyer un message" CTA that opens an in-app conversation. New layout `layout_ambassador_resources.xml` and drawables `bg_card_bordered.xml`/`placeholder_ambassador_initials.xml` support this.
- **Signup expansion**: `CountryLightList` now includes Germany and Switzerland (with new flag string resources), opening account creation to those countries.
- **"Duplicate event" commit discrepancy**: commit `b0647171b` is titled "feat(events): add duplicate event feature" but its actual diff contains no event-duplication code — instead it touches `CLAUDE.md` (repo guidance doc, added) and a refactor of `OnboardingDisponibilityFragment`/`OnboardingViewModel` (availability day rows extracted into a reusable `item_availability_day_row.xml` layout, `enhanced_onboarding_time_disponibility_layout.xml` simplified accordingly, new `styles.xml` entries). No "duplicate event" UI or string resources exist anywhere in the diff between these tags; this appears to be a mislabeled/misattached commit message rather than a shipped feature.
- **Build/CI churn**: Gradle was upgraded to 9.6.1 (AGP 9.2.1 → 9.3.0, Kotlin 2.3.10 → 2.4.10, plus AndroidX Core/Material/Firebase BOM/OkHttp/Ktor/Mockito/UIAutomator/AboutLibraries bumps) and then reverted two days later within the same range, leaving dependency versions net-unchanged; similarly a "bump app version to 14.5" prep commit was reverted, leaving the shipped version at 14.4 (up from 14.3). Bitrise config gained `retry_without_sending_to_review: true` on the Play Store deploy step.
- **Cleanup**: obsolete instrumentation tests for the legacy `WelcomeOne`…`WelcomeFive` onboarding activities were deleted; the unused `EditPhotoFragment` (309 lines, superseded by the profile-photo flow in `EditPhotoActivity`) was removed; `BaseActivity.fromNotifLogFirebaseEvent` (dead push-notification analytics logic) was commented out; two Copilot-authored "Potential fix for pull request finding" commits made small hardening tweaks to `create_survey_activity.xml` and `OnboardingEndActivity`; new `toggle_nav_mode.sh`/`.ps1` dev scripts were added to switch the emulator between gesture and 3-button navigation for testing; `gradle.properties` now keeps APKs installed after instrumentation test runs.

---

## Version_14035551 — 2026-07-23

### Commits
- feat(guide): afficher une puce "Lieu climatisé" sur la fiche détail POI (deb5f57eb)
- fix(guide): éviter d'afficher des POI obsolètes lors du changement de filtre (75c2a47b2)
- chore(versionName) : 14.2 -> 14.3 (2033a8626)
- feat(guide): ajouter filtre lieux climatisés sur la carte et bandeau home (81cc39786)
- chore(versionName ) : 14.1 -> 14.2 (2dab04fc7)
- feat(deeplink): ajouter routes universal link vers liste et détail badge (0af01ffba)
- feat(deeplink): ajouter route universal link vers badge intro (67dfe4b88)
- fix(ui): fermer le clavier au bouton OK sur inscription et login (7e91fb0e9)
- fix(ui): rollback padding IME sur l'inscription (clavier par-dessus l'écran) (4abb4a41d)
- fix(ui): corriger le clavier qui recouvre les boutons bas (Android 15 edge-to-edge) (98d0917ef)
- fix(ui): agrandir la carte événement de 260dp à 290dp (9fd329d1d)
- fix(ui): corriger balise LinearLayout manquante dans activity_comments (998748ea7)
- fix(badges/ui): icônes SVG, titre détail et header messagerie (7ef1d520b)
- fix(notifs): gérer le routing badge depuis les notifications push (bd264cf8e)
- fix(badges): remplacer les emojis par les icônes SVG dans BadgeIntroBottomSheet (99d753dad)
- fix(ui): keyboard handling on login/signup and alert dialog button alignment (5f61bd525)
- fix(ui): augmenter la taille des icônes et textes dans les cartes événements (09e8e5875)
- feat(badges): import SVG badge icons and wire notification routing (1893a8d13)
- refactor(ProfileFull): séparer la vue profil en deux ViewControllers distincts (daa74037c)
- fix(home/errors): hide bouncing heart, rework 404 bottom sheet (c8d4fadb7)
- fix(badges): rework detail card, reversible icon, and title fixes (1507a1de8)
- fix(badges/profile): UI fixes, label updates, and attendance signing (9018f3597)
- fix(ConversationActivity):  fix proposals for event and smalltalk (d50796ce5)
- fix(BageListActivity) : reset view in onResume (cfd8cef87)
- fix(badgelistView) : progression fixed (c67646189)
- feature(APIErrorBottomSheet) : error server bottomsheet for every type of error (a2c07313c)
- fix(Group) : fix thematic groups (9c6c6d3a5)
- fix(Group) : fix thematic groups (d2a62f1ae)
- feature(badges) : several fixes (124cd0fbe)
- feature(badges) : several fixes (b0eb98d50)
- feature(BadgeListActivity) : Implemented logic for badges (d912acc97)
- fix(BadgeFrament) : fixed button capslock (682dcde4b)
- feature(ProfileSettingsActivity) : adding new settings view (f02a512e6)
- feature(ProfileSettingsActivity) : adding new settings view (db672163c)
- fix(MemberListAdapter) : fix animator title (c1a0c20ff)
- fix(EventFragment) : fix pagination (fb62b149d)
- fix(OnboardingCongratsFragment) : fixed event button (b9fee4167)
- feature(ProfilFullViewController) : first steps for badges (73f944c7f)
- feature(ProfilFullViewController) : first steps for badges (6b89e4b48)
- feature(ProfilFullViewController) : first steps for badges (43b1d5534)
- feature(ProfilFullViewController) : first steps for badges (3367e9241)
- feature(ProfilFullViewController) : first steps for badges (778b2d7d2)
- feature(ProfilFullViewController) : first steps for badges (36e90c94e)
- feature(ProfilFullViewController) : first steps for badges (d34594398)
- feat(HomeFragment) : little message from other users (9e1983dd1)
- feat: connexions suggérées + prochain pas — cards home, adapters, modèles et service réseau (0a67784c9)

### Code structure & content changes

- **Badges system (new `android/badges/` package)**: A full gamification feature was added — `ApiBadge`/`ApiBadgeMetadata` (Parcelable model with progress metadata), `UserBadge` (badge definitions/progress logic), `BadgesListActivity` + `BadgesListAdapter` (list screen with obtained/in-progress/not-started sections and an empty state), `BadgeDetailBottomSheet`, `BadgeIntroBottomSheet`, `BadgeUnlockedBottomSheet`, and `DotsAnimationView`. Badge icons are shipped as SVGs (`badge_bienvenue.svg`, `badge_fidele_papotages.svg`, `badge_moteur_rencontres.svg`, `badge_premier_contact.svg`, `badge_voix_presente.svg`, rendered via the newly added `androidsvg` dependency) replacing an earlier emoji-based iteration. `MainActivity` and `PushNotificationManager`/`NotificationActionManager` were wired to open `BadgeUnlockedBottomSheet` from push notifications, and `UniversalLinkManager` gained `badges`, `badges/intro`, and `badges/{key}` deep-link routes into the list/detail/intro screens.
- **Profile screen split**: `ProfileFullActivity` (previously used for both "my profile" and viewing others) was cut down from ~900 to ~50 lines and now only renders other users' profiles; a new `MyProfileFullActivity` (694 new lines) was created to own the current user's profile screen, including badge summary cards, settings entry point, and edit-profile flows. A companion `ProfileSettingsActivity` (new, `android/profile/`) centralizes account settings (language, notifications) previously scattered across the profile screen.
- **Suggested connections / next step ("connexions suggérées + prochain pas")**: New `Suggestion`/`SuggestionResponse`/`SuggestedUser`/`SuggestedEntourage` models and a `SuggestionRequest` (`GET/PUT suggestions`) were added and wired into `ApiModule`. `HomeFragment`/`HomePresenter` gained `HomeSuggestionConnectionAdapter` and `HomeSuggestionNextStepAdapter` plus new layouts (`home_suggestion_connection_item.xml`, `home_suggestion_next_step_item.xml`) to surface personalized suggestion cards on the home feed, with accept/dismiss actions calling the new PUT endpoint.
- **Global API error handling**: New `ApiErrorBus` (a `SharedFlow`-based event bus), `ApiErrorInterceptor` (OkHttp interceptor that emits on any 4xx/5xx response except 401, which is already handled by `AuthenticationInterceptor`), and `ApiErrorBottomSheet` (a reusable bottom sheet with per-status-code title/description and a "return home" / "dismiss" action) give the app a consistent server-error UX across activities, hooked in via `BaseActivity`.
- **Guide / map "air-conditioned places" filter**: `Poi.airConditioned`, `GuideFilter.isAirConditionedSelected`/`requestedAirConditioned`, and updated `PoiRequest`/`GuideMapPresenter` calls add a climate-controlled-venue filter to the map and a home banner shortcut (`HomeToolsAdapter.onClimateMapClick`, `cardClimateMap`); `GuideMapPresenter.retrieveClustersAndPois` also gained a request-id guard so stale/late responses from a superseded filter or camera state no longer overwrite the current POI list.
- **UI/UX fixes**: Reworked keyboard handling on login/signup (closing the keyboard on the OK button, then rolling back an IME-padding change that pushed the keyboard over the screen, then fixing Android-15 edge-to-edge keyboard overlap of bottom buttons); enlarged event-card map height (260dp → 290dp) and icon/text sizing in event cards (`new_event_item.xml`, `AllEventAdapter`, `GroupEventsListAdapter`, `HomeEventAdapter`); fixed a missing `LinearLayout` closing tag in `activity_comments.xml`; reworked the home "404" error bottom sheet and hid a stray bouncing-heart animation; various thematic-group and event-pagination fixes (`Group`, `EventFragment`, `MemberListAdapter`).
- **Build/dependency**: version bumped 14.1 → 14.2 → 14.3; added `androidsvg` (1.4) to `gradle/libs.versions.toml`; `ApiModule` wires `SuggestionRequest` and installs `ApiErrorInterceptor` in the OkHttp client.
- **Localization**: string tables (`values/strings.xml` and all `values-*` locale files) grew substantially to support badges, API error messages, and suggestion cards.

---

## Version_14015502 — 2026-07-23

### Commits
- fix(UniversalLinkManager) : fixed national group (c8babcc7c)
- feature(NationalGroupActivity) : added loader on view (92f8605ef)
- feature(NationalGroupActivity) : added loader on view (2dc12333b)
- chore(VersionName) : 14.0 -> 14.1 (7b35e6a19)
- feat(NationalGroupActivity) : takes event as model for group UI EN-9183 (5d6be1d9f)
- feat(NationalGroupActivity) : adding a fourth step for home welcome EN-9183 (f8d88a85b)
- feat(NationalGroupActivity) : adding a fourth step for home welcome EN-9183 (49894b4d2)
- fix(WelcomeJourneyAdapter) : fix UI welcome group on example of WelcomeEvent (18ede9a84)
- fix(WelcomeJJourneyAdapter ) : remove last view for integration complet (9b909dea9)
- fix(PostAdapter) : fix emoji (6bb26b312)
- fix(MemberActivity) : modified condition for plus button (da152f0e1)
- feat(NationalGroupActivity) : adding a fourth step for home welcome EN-9183 (fdd57a456)
- feat(NationalGroupActivity) : adding a fourth step for home welcome EN-9183 (19346ad38)
- feat(NationalGroupActivity) : adding a fourth step for home welcome EN-9183 (4b8ded659)
- feat(NationalGroupActivity) : adding a fourth step for home welcome EN-9183 (5361d9077)
- chore(vibe.toml): adding mistral vibe configuration (7f7f4d099)

### Code structure & content changes

- **National groups feature (EN-9183)**: New `NationalGroupsActivity`, `NationalGroupsAdapter`, and `NationalGroupsBottomSheet` (`android/home/`) let users browse and join/leave nationwide (non-geolocated) groups, backed by a new `GET neighborhoods/national` endpoint (`GroupRequest.getNationalGroups`) and matching handlers added to both `GroupPresenter` and `HomePresenter`. `Group.kt` gained several new fields (`radius`, `createdAt`, `updatedAt`, `slug`, `defaultGroup`, `groupType`, `parentId`, `children`, `ancestors`) and an `isJoined` convenience property to support the parent/child group hierarchy, and several previously non-nullable fields (`latitude`, `longitude`, `displayAddress`, `unreadPostsCount`) became nullable.
- **Home welcome journey**: The onboarding "welcome journey" widget (`HomeWelcomeJourneyAdapter`, `HomeFragment`) was extended from 3 to 4 steps, inserting the new national-groups step as step 2 and shifting the two event-related steps to 3 and 4; `HomeFragment` now launches `NationalGroupsActivity` via `startActivityForResult`, shows a confirmation snackbar with a "Voir" action routing to the Groups tab, and refreshes the summary on return.
- **Deep linking**: `UniversalLinkManager` gained a `pathSegments.contains("national")` branch to open `NationalGroupsActivity` from a universal link, and `onRetrievedGroup` was hardened against a null `Group`/`Group.id` (previously would NPE).
- **Minor fixes**: `PostAdapter` fixes an emoji rendering issue; `MembersActivity` changes the condition gating the "add member" FAB to check `HomeFragment.signablePermission && ActionSheetFragment.isSignable` instead of an "Équipe Entourage" role check; `CurlLoggingInterceptor` now escapes special shell characters (`"`, `$`, `` ` ``) in logged request bodies and adds a `Content-Type: application/json` header when missing.
- **Build/misc**: Version bumped 14.0 → 14.1 (`app/build.gradle.kts`); `NationalGroupsActivity` registered in `AndroidManifest.xml`; a new `vibe.toml` (Mistral Vibe tooling configuration) was added at the repo root — unrelated to app runtime behavior.

---

## Version_14005486 — 2026-07-23

### Commits
- fix(HomeFragment) : fix UI home android (323e99b5b)
- Merge pull request #334 from ReseauEntourage/fix/home-small-talk-margin-and-title-1716763503680802750 (eb803990b)
- fix(home): Remove 'Rejoindre une discussion solidaire' section header and update small talk card margins (33c0ac4bf)
- Merge pull request #333 from ReseauEntourage/fix/home-smalltalk-alignment-1314067499649754858 (e89add780)
- Fix home small talk card layout: remove duplicate title and align width/height (fbd628c79)
- fix(ImageZoomActivity) : changed image MIME type (69cb3912f)
- Merge pull request #332 from ReseauEntourage/fix-download-image-mime-type-8408233867408826588 (b9bd5ae93)
- Fix image download issue by setting correct MIME type (49792a959)
- fix(build) (684cfe1cf)
- build(fixbuild) (288513ca5)
- fix(HomeFragment): smalltalk card width (351d0ce66)
- fix(item_home_small_talk_active) : fix hieght cell (8995671c4)
- fix(OnboardingPhase1) : fix nullable fields (1c6c7bd32)
- Merge pull request #330 from ReseauEntourage/update-smalltalk-active-layout-4517590392403662489 (3960b32fd)
- fix(smalltalk): refine active smalltalk layout and avatar logic (f9a642359)
- fix(ImageZoomActivity): reduce button size (6ffe76f99)
- Merge pull request #329 from ReseauEntourage/fix/zoom-buttons-size-1343912614145990226 (b7a13f5d5)
- Réduire la taille des boutons dans le zoom d'image (86ecca4cb)
- Merge pull request #328 from ReseauEntourage/fix/event-image-crop-ratio-17247778470014727837 (d03bfbb6a)
- fix: Enforce strict 16:9 aspect ratio for event creation image uploads (90418518c)
- Merge pull request #327 from ReseauEntourage/update-smalltalk-waiting-ui-12889934844434394932 (f4f77b8bc)
- Update smalltalk waiting UI with an hourglass vector icon (c7e11e418)
- Merge pull request #325 from ReseauEntourage/feat/add-header-to-language-screen (13caf615c)
- Merge pull request #323 from ReseauEntourage/feat/home-welcome-video-deeplink-15002740395195068653 (e88eccf7b)
- feat: Update pre-onboarding language selection UI (b8bef307f)
- chore: migrate from kapt to ksp (05725fa89)
- build: migrate desugar dependency to Version Catalog (44849bef1)
- feat: Add deeplink for welcome video on home screen (952c49a9d)
- feature(MainFilterActivity) : added some subtitles (72cb431da)
- feat: Add overview documentation for the Android client (42b2de742)
- Merge pull request #322 from ReseauEntourage/feature/remove-other-interest-and-add-subtitle-14795602669235495365 (ddec20967)
- feat: Remove "Other" interest option and add subtitle to event filter (716b19fde)
- fix(MemberListAdapter) : fix user emargement (68e43c08b)
- fix(layout_item_onboarding_interests) : fix center text (b95ec6251)
- build(HomeFragment) : try build (bc876e37e)
- Revert "build(yaml)" (2da5bb0cc)
- build(yaml) (fe56831e9)
- Merge branch 'feat/add-integrity-test' into develop (ebac85554)
- feature(signIn protection ) : added path to global properties (f514d10df)
- fix(CreateEventStepOneFragment) : fix button top side (c152f66fc)
- Merge pull request #321 from ReseauEntourage/feature/event-gallery-photo-upload-7335928810997773955 (1f7d18771)
- fix(SmallTalkAdapter) : fix small talk cell (d1d2cabcd)
- feat: Add gallery photo upload for events (8c1decd02)
- Merge pull request #319 from ReseauEntourage/fix-small-talk-button-clipping-14484097182512414487 (33de20011)
- Merge pull request #320 from ReseauEntourage/feat/interests-subtitles-6229994601739400933 (d3d73a0fa)
- feat: add subtitles to interests in profile and onboarding (e4a1c9753)
- Fix clipping issue on 'Voir' button in active small talk cell (d5220faf8)
- feat: update app version to 14.0 (6f15254e6)
- feat: add HMAC authentication for user creation requests (6fc48f20a)
- fix(ActivityZoomActivity) : changed icon (4878613b2)
- fix(ActivityImageZoom) : changed UI (ec29047ae)
- Merge pull request #317 from ReseauEntourage/fix/image-zoom-ui-like-ios-1758578516235565266 (29b491a2e)
- style: update image zoom icons to match iOS (4e9e402fb)
- chore: typo in test strings (b29661e25)
- chore(rebuild) (9b7c130b9)
- fix(strings.xml) : fixed merge (7a7036c1a)
- Merge remote-tracking branch 'origin/fix-small-talk-home-card-7959169516780749301' into develop (2f5e4f0d6)
- Merge remote-tracking branch 'origin/feature/image-zoom-download-883291066702498678' into develop (560439207)
- Refonte de la cellule Bonnes Ondes sur la page d'accueil (85b42a726)
- fix(strings.xml) : corrected wording (5ea28d42f)
- feat: add back button and download photo functionality to ImageZoomActivity (4fc990071)
- Merge pull request #314 from ReseauEntourage/fix/bottomsheet-femmes-isolees-wording-logic-9101398291351861697 (ea9bbaa07)
- feat: update wording and logic for femmes isolées in bottom sheet (2a6ce0a2f)
- Merge pull request #312 from ReseauEntourage/feat/unsubscribed-female-participants-15574084968266570647 (129ca7bf2)
- Merge pull request #313 from ReseauEntourage/fix-unsubscribed-bottom-sheet-visibility-1939095830123725362 (9949acae3)
- fix(members): show unsubscribed bottom sheet only for signable events and organisers (e0e81817f)
- chore(gemini.md) : updated gemini.md (7595bc078)
- feat: Add unsubscribed female participants to event members UI (e0efcb81f)
- chore(versionName) : 13.6-> 13.7 (8a2bcd7ff)
- fix(MemberListAdapter) : fix checkbox for isMe condition (df14c8add)
- fix(memberlistAdapter) : fixed isMe condition (5edf9c11b)
- Merge pull request #311 from ReseauEntourage/fix/memberlist-unsubscribed-padding-17435512422459356994 (cfbc101b0)
- UI: Remove bottom padding from unsubscribed participants in member list (47e577e05)
- fix(EventFeedFragment) : fix button UI (98288b385)
- fix(EventFeedFragment) : fix button UI (c1e1cfb46)
- feature(EventFeedFramgent) : test new button (f8e8979c1)
- fix(AboutEventFragment) : fix UI Button (a8c609f59)
- Merge pull request #310 from ReseauEntourage/feature/event-button-style-2268767920618853765 (a6a560cbd)
- Update 'Quitter l'événement' button style (3968131ea)
- fix(MembversActivity) : open button to animateur (20a3a0f0f)
- fix(build) (c067b3e08)
- fix(HomeFragment) : added analytics event for buffet button (3f440031e)
- Merge pull request #307 from ReseauEntourage/fix/conversation-settings-buttons-15092783441190341858 (ce3263989)
- Merge branch 'develop' into fix/conversation-settings-buttons-15092783441190341858 (3394ff53c)
- Merge pull request #309 from ReseauEntourage/fix/update-wording-discussions-events-7904160291284938255 (488ac27e7)
- Merge branch 'develop' into fix/update-wording-discussions-events-7904160291284938255 (638c0a84a)
- feature(SettingModalFragment) : changed UI for added people (f79962bfe)
- fix: update wording for deleting discussions and leaving small talk (36a849bca)
- fix(EventFeedFragment) : changed button event detail (62c67e5c9)
- fix(EventFeedFragment) : modified ui for buttons (df98d8b19)
- fix(MemberActivity) : changed sheet fragment (1c0aa2dbd)
- Update conversation settings buttons for 1-1 and events (88bbbe2f0)
- Merge pull request #306 from ReseauEntourage/fix-reunion-display-name-1417292771196600644 (307bdab51)
- Update Reunion display name to "La Réunion" (0336cbf51)
- Merge pull request #305 from ReseauEntourage/fix/event-member-self-check-in-4325706793551902038 (b16fa939e)
- feat: allow users to self-check-in on event members list (fbeb00407)
- Merge pull request #304 from ReseauEntourage/ui-fixes-members-fragment-1029790727191572937 (ada5deaa3)
- Refactor participants added on the spot layout (ebe975041)
- Merge pull request #303 from ReseauEntourage/jules-4185106242538036802-367bd85d (101185438)
- feat(events): Add floating action button to manage unsubscribed participants (f9dd7edbc)
- Merge pull request #302 from ReseauEntourage/feat/unsubscribed-participants-bottom-sheet-11320438600862139132 (e5b0921d9)
- feat: add unsubscribed participants bottom sheet and sync logic (4a57be819)
- feat: add unsubscribed participants bottom sheet (c24db43ca)
- Merge pull request #300 from ReseauEntourage/remove-old-welcome-journey-15091578654350586956 (1e2c275fe)
- Merge pull request #301 from ReseauEntourage/feature/restore-countries-12468563699956094348 (5d7001624)
- Restore expanded country list for authentication (0b4c00de7)
- Remove legacy Welcome activities and associated notification logic (a89c3ea6e)
- chpre(versionName) : 13.5 -> 13.6 (b743fb5e0)
- fix(ContryLightList) : hide other numbers (8901d4b74)
- DiscussionMainFragment) : removed list supression (8a2a5f06b)
- fix( DetailConversationActivity) : remove demo hack (92d4d8f4c)
- Merge pull request #298 from ReseauEntourage/fix-1-1-conversation-buttons-18200199958203407494 (9df8574b8)
- feat: add quit option and hide delete for 1-1 conversations (7ce161efd)
- fix(many file) minor wording fix (317d19dcc)
- fix(many file) : minor UI and wording fixes (d0d717ce7)
- Merge pull request #297 from ReseauEntourage/jules-154247935624551558-0a648daa (604a73d0c)
- feat: Add conversation deletion via detail parameters and long click in list (78685b68f)
- Merge pull request #296 from ReseauEntourage/fix/staff-banner-visibility-5605994197775614504 (33ce96f4c)
- fix(CommentActivity) : remove banner bug (f59944b97)
- fix: Masquer la bannière staff par défaut dans les commentaires (874c9a90b)
- Merge pull request #295 from ReseauEntourage/fix-country-codes-and-placeholders-834188238185129607 (005a25d47)
- feat: Add flags for Morocco, Guadeloupe, and Reunion, and update phone placeholder dynamically based on country code (143ba38e6)
- Merge pull request #293 from ReseauEntourage/feat/add-dom-tom-countries-to-login-3993211210409521011 (b1a2eddf9)
- feat: Add DOM-TOM countries to the registration country list (41e4644e5)

### Code structure & content changes

**Legacy welcome journey removed.** `WelcomeOneActivity` through `WelcomeFiveActivity` (~800 lines total) and their five XML layouts were deleted, along with the `stage`-string routing logic ('h1', 'j2', 'j5', …) in `NotificationActionManager` and `PushNotificationManager`, related intent extras in `MainActivity`, `UniversalLinkManager`, `GroupFeedFragment`, and the corresponding obsolete `AnalyticsEvents` constants. `WelcomeEventsListActivity` (introduced in the previous release) is now the sole welcome-journey entry point, and `HomeFragment` gained a direct deep link to the welcome video.

**Event image upload pipeline (gallery photos for events).** Three new classes under `events/create/`: `EventImageUploadPresenter` (orchestrates prepare→upload→cleanup), `EventImageUploadRepository` (raw OkHttp PUT to a presigned S3 URL), and `PrepareEventImageUploadRepository` (calls new `POST outings/presigned_upload` via `EventsRequest.prepareImageUpload()` to obtain an `upload_key`/`presigned_url` pair). `CreateEventStepOneFragment` now implements `EventImageUploadView`, integrates `UCrop` (new `libs.ucrop` dependency) to enforce a strict 16:9 crop ratio on gallery-picked images, and uploads the cropped file through the new presenter before enabling the "next" button.

**Unsubscribed / walk-in participants.** New `AddUnsubscribedParticipantsBottomSheet` (bottom sheet with +/- counters for "isolés", "riverains", and "femmes isolées" categories) and `layout_unsubscribed_participants_footer.xml` / `bottom_sheet_add_unsubscribed_participants.xml` layouts. `MembersActivity` wires this in via a new floating action button (SpeedDial), fetches counts through `EventsPresenter.getEvent()`/new `unsubscribedParticipantsUpdated` LiveData backed by `EventsRequest.updateUnsubscribedParticipants()` (`POST outings/{id}/users/unsubscribed_participants`), and gates the add-UI to signable events and organisers/"Équipe Entourage"/"Animateur Entourage" roles (`isEquipeEntourage`). `MembersListAdapter` and `Events`/`MetaData` model gained matching fields (`unsubscribedParticipantsAskForHelp/OfferHelp/Female`). Note: a leftover `Timber.wtf("wtf" + iAmOrganiser)` debug log was left in `MembersActivity.onCreate`.

**Conversation settings / quit vs delete.** `SettingsDiscussionModalFragment` and `ActionSheetFragment` were reworked so 1-to-1 conversations always show a "Quitter"/"Supprimer" pair (delete now maps to the same `leaveConverstion()` call as quit) while group/event conversations keep the creator-gated single "Quitter"/"Supprimer l'événement" button; small talks get "leave_group" wording instead of "quit". `DiscussionsListAdapter` gained a long-press-driven `isDeletionMode` that reveals a per-row delete icon and hides date/unread badges, feeding a new list-level deletion flow in `DiscussionsMainFragment`.

**Small talk ("Bonnes Ondes") home card redesign.** `HomeSmallTalkAdapter` was substantially rewritten (143-line diff) with a new `item_home_small_talk_active.xml` layout, refined avatar logic, an hourglass vector icon (`ic_hourglass_small_talk.xml`) replacing the old waiting-state graphic, and several follow-up fixes to width/height/margins/clipping on the active and waiting cell states; the redundant "Rejoindre une discussion solidaire" section header was removed from Home.

**ImageZoomActivity overhaul.** New download capability via `DownloadManager` with dynamic MIME-type/extension detection from the image URL (fixing wrong-MIME-type downloads), a back button, and icon/UI updates to match the iOS zoom screen; button sizes were reduced in a follow-up fix.

**Onboarding / auth.** `OnboardingInterestFragment` (enhanced onboarding) and `MainFilterActivity` both now populate interest subtitles from new string resources instead of blank strings, and the "Other" interest option was removed from the event filter. `OnboardingPhase1Fragment` and `LoginActivity` gained dynamic phone-number placeholders that update via `CountryCodePickerListener` based on the selected country (`Utils.getPhoneNumberPlaceholder()`), and fixed a nullable-field crash. `CountryLightList`/`CountryList` gained Guadeloupe, Martinique, Réunion (renamed to "La Réunion"), and Morocco entries with flags, restoring/expanding the DOM-TOM country list for registration; "other numbers" are hidden. `PreOnboardingLanguage` UI was updated with a header on the language selection screen.

**Security.** New `HmacInterceptor` (OkHttp `Interceptor`) signs `POST /users` (account creation) requests with `HmacSHA256` over `POST\n/api/v1/users\n{timestamp}\n{phone}`, keyed by a `BuildConfig.HMAC_SECRET` sourced from `HMAC_SECRET_ANDROID` env var or a Gradle property; wired into `ApiModule`.

**Build/tooling.** Version bumped to 14.0 (`versionMajor`/`versionMinor`), `compileSdk`/`targetSdk` raised 36→37, AGP 9.0.1→9.2.1, Gradle wrapper 9.1.0→9.4.1. Annotation processing migrated from `kapt` to `ksp` (`glide-compiler`→`glide-ksp`), desugaring dependency moved into the version catalog, and a Foojay toolchain resolver plugin (`gradle-daemon-jvm.properties`, JetBrains JDK 21) was added for consistent daemon JVM provisioning across CI/dev machines. New root docs (`OVERVIEW.md`, `README.md` HMAC section, `gemini.md`) and dev scripts (`add_strings.sh`, `add_strings_en.sh`, `copy_strings.py`, `update_activity.sh`) were added. A stray `build_log.txt` (1124 lines of raw build output) was accidentally committed in the "Remove Other interest option" commit and never cleaned up — a repeat of the `gradle_error.txt` slip in the previous release.

---

## Version_13055354 — 2026-07-23

### Commits
- fix(GroupCommentActivity) : remove banner (294e645a5)
- fix(build.gradle) : EN-9053 Fix bug on old version of java , with a google mechanism (cd01de6e5)
- fix() (6defed7b1)
- fix(WelcomeEventsListActivity) : fix webinar / welcome /papoatage links (bb517ddb0)
- fix(WelcomeEventsListActivity) : fix webinar / welcome /papoatage links (49e3dfc37)
- fix(WelcomeEventsListActivity) : fix webinar / welcome /papoatage links (e9ccbddc2)
- chore(remove test code ) (c8b59d773)
- feature(HomeFragment) : EN-8942 congrats bottom sheet (04a176b0f)
- feature(HomeFragment) : EN-8942 congrats bottom sheet (3f8ab9e6f)
- feature(HomeFragment) : EN-8942 congrats bottom sheet (1908e5f9a)
- feature(HomeFragment) : EN-8942 congrats bottom sheet (6de3999db)
- feature(HomeFragment) : EN-8942 congrats bottom sheet (4ca2b3b7d)
- Merge pull request #292 from ReseauEntourage/fix-home-congrat-bottom-sheet-16134076104130291558 (4557d9645)
- feat(home): Re-introduce congratulation popup as a BottomSheet after Welcome Journey (6a5854764)
- feature(HomeFragment) : EN-8942 fix call event firsts steps (e6230b358)
- chore(HomeFragment): remove hack (0e295413d)
- feature(HomeFragment) : EN-8942 fix video UI (44cd64aa9)
- feature(HomeFragment) : EN-8942 fix video UI (257f7c53d)
- feat(onboarding): Re-enable enhanced onboarding flow and adjust payload (661867fc2)
- Merge pull request #291 from ReseauEntourage/feature/welcome-events-list-back-button-13243735096530011466 (c8ad365c5)
- Add standard onboarding back button to WelcomeEventsListActivity (05e4abe54)
- feature(HomeFragment) : EN-8942 fix preoading video (aec6539ae)
- feature(HomeFragment) : EN-8942 fix preoading video (2fb67d942)
- fix(HomeWelcomeListEvent) : changed padding left (fc1a52571)
- chore(VersionName) : 13.4 -> 13.5 (1f5358561)
- feature(WelcomeActivity) : EN-8942 welcome flow not show for asso and team (2d2a61014)
- feature(WelcomeActivity) : EN-8942 welcome flow (77d58413f)
- Merge pull request #290 from ReseauEntourage/feature/unify-welcome-events-deep-links-3635033028594635551 (5c9e88551)
- refactor: use WelcomeEventsListActivity for welcome deep links (c76fbce39)
- feature(HomeWelcome) : adapt routes and fix welcom flow EN-8974 EN-8942 (c3ab13478)
- fix(assets) : entoutou logo (80a87a92d)
- fix(build) (72b03492b)
- Merge pull request #289 from ReseauEntourage/feature/home-welcome-video-resource-18288130977781458196 (eb470bb9a)
- Integrate welcome video resource from API to Home welcome journey (8c8327f42)
- Merge pull request #287 from ReseauEntourage/fix-welcome-journey-ui-and-strings-7923677501474148121 (28521173b)
- Fix UI details in welcome journey and add dynamic headers to events lists (09ef74529)
- Merge pull request #286 from ReseauEntourage/fix-recurrent-event-edit-dialog-btn-state-17677774774606169547 (85d0c0a36)
- Fix validate button state in recurrent event edit dialog (46cd80bc2)
- Merge pull request #285 from ReseauEntourage/fix/android-youtube-error-153-12250138948866577778 (ac5eab724)
- fix(pedago): Fix YouTube iframe Error 153 by providing base URL (6b269afee)
- fix(build) (ba1176b9d)
- Merge pull request #284 from ReseauEntourage/fix/empty-placeholder-image-post-17479561161585791817 (2639e8201)
- chore(build) (4e6397354)
- fix: prevent sending placeholder text as message when posting only an image (6d01aef79)
- Merge pull request #282 from ReseauEntourage/feat/welcome-journey-native-lists-1719120168567455122 (eff0b4ae2)
- feat: replace welcome journey web links with native event lists (98e1425f8)
- Merge pull request #279 from ReseauEntourage/update-welcome-journey-17865602672232079750 (6b81cf747)
- Merge pull request #278 from ReseauEntourage/feature/home-shimmer-skeleton-16190285824814720829 (6fefba5e1)
- Update Welcome Journey UI, wording, and logic (784d3bb21)
- feat: add Shimmer skeleton loading for Home screen lists (9eebe7e2a)
- Merge pull request #277 from ReseauEntourage/jules-home-welcome-journey-events-17191615134482054241 (d482e376d)
- feature(Home): connect welcome journey state to Summary events (da6fde25b)
- feature(AnalyticsEvent) : EN-8740 tracking onboarding and enhancedonboarding (7051f7857)
- feature(DetailConversationActivity) : EN-8966 banner for offline staff (2ccb51ae6)
- chore(HomeFragment) : reset home welcome (918408557)
- Merge pull request #276 from ReseauEntourage/feat/staff-out-of-office-banner-10013243510090752273 (29391c7bc)
- feat: Add staff out-of-office banner to 1-to-1 conversations (9cdc9dd85)
- Merge pull request #275 from ReseauEntourage/tracking-onboarding-4563297702861338127 (34039ddf6)
- feat: Add comprehensive tracking events for Onboarding flows (72ca14ff0)
- chore: Update bitrise.yml (typo in slack field) (d88b75be7)
- test: prevent API tests from running on production flavor (2130251d2)
- fix bitrise.yml (ff3ecea4d)
- Add CI workflow for pull request checks in bitrise.yml (af62577ab)

### Code structure & content changes

**Welcome Journey / Home screen overhaul.** `HomeFragment.kt` gained a driven-by-API "welcome journey" flow: `handleWelcomeJourneyState()` now derives the 3-step progress (video watched, webinar/first-steps attended, papotage attended) directly from `Summary.events` flags (`onboarding.resource.welcome_watched`, `onboarding.outing.webinar_or_first_steps`, `onboarding.outing.papotages`) instead of local taps, hides itself entirely for partner users and for users who had already completed the journey before install, and triggers a new `HomeCongratPopFragment` BottomSheet (rewritten from a dialog-based popup, `fragment_home_congrat_pop.xml` shrunk ~300→~180 lines) the first time all 3 steps complete. The video modal (`showVideoModal()`) now embeds a real WebView (`loadCleanVideo()`) fed by a new `homePresenter.welcomeResource` observer, with a 5-second countdown before the "Continue" button unlocks. The home RecyclerView's ConcatAdapter is now bootstrapped with only a new `HomeSkeletonAdapter` (shimmer loading, new `home_skeleton_item.xml` / `home_skeleton_horizontal_list.xml` layouts, `libs.shimmer` dependency added) and swaps in the full adapter set once the summary/pedago/notifications calls complete, avoiding content jumps.
- `WelcomeEventsListActivity` is a new screen (`activity_welcome_events_list.xml`) that replaces the old single-event webinar/papotage/welcome deep-link targets; `EventsPresenter` gained `getEventsPapotages()`, `getEventsWebinar()`, `getEventsFirstSteps()` backed by new `EventsRequest` endpoints (`outings/papotages`, `outings/webinar`, `outings/firsts_steps` returning lists instead of a single `EventWrapper`). `UniversalLinkManager` now routes `papotages`/`webinar`/`welcome` path segments straight to `WelcomeEventsListActivity` with a `TYPE` extra instead of calling `UniversalLinkPresenter`; the old `getEventSmallTalk()/getEventWelcome()/getEventSensibilisation()` methods were deleted from `UniversalLinkPresenter`.
- A stray `WelcomeEventsListActivity.kt.orig` backup file (63 lines) was accidentally committed alongside the real file and never cleaned up.

**Staff out-of-office banner.** `DetailConversationActivity` adds `checkStaffBannerDisplay()`/`setupStaffBannerContent()`: for 1-to-1 conversations with a member holding the "Équipe Entourage" role, a dismissible banner (`binding.layoutStaffBanner`) is shown outside business hours (weekday nights, and Friday evening through Monday morning), with clickable spans for `tel:` links and an `entourage://groupe` link that opens the default group via `groupPresenter.getDefaultGroup()`.

**Messaging fixes.** `CommentActivity.handleCommentAction()` and `DetailConversationActivity`'s send-message path now treat an empty/whitespace comment as `null`/`""` rather than converting it to an HTML string, fixing a bug where posting only an image sent a stray placeholder text message. `universalLinkManager` visibility was widened from `private` to internal.

**Analytics.** `AnalyticsEvents.kt` gained ~20 new constants covering the full onboarding/enhanced-onboarding funnel (input names, code, profile, association search, location, confirmation, notifications, association description — view + click events for each step).

**Build/CI.** `versionMinor` bumped 4→5; `app/build.gradle.kts` enabled core library desugoring (`coreLibraryDesugaring`, `desugar_jdk_libs:2.0.4`) to fix a Java-version-related build issue (EN-9053). `bitrise.yml` was substantially rewritten (395-line diff) to add a PR-check CI workflow and fix a Slack-field typo. A stray `gradle_error.txt` (332 lines of raw Gradle error output) was accidentally committed as part of the congrat-popup commit.

---

## Version_13045284 — 2026-07-23

### Commits
- fix(versionName) : 13.3 -> 13.4 (8b67579af)
- chore(versionName) : 13.2 -> 13.3 (da1eddc6f)
- fix(EventFragment) : replace event members (ebf1a2eaa)
- Fix CustomAlertDialog.showWithCancelFirst usage across app (b995a4d4f)
- Update event cancellation flow wording and remove recurrence popup (75e9ffb8b)
- fix(eventDetail) : fix member place (02ee48858)
- Revert "Fix recurring event cancellation in Event Settings and Event Details" (891dfedca)
- fix(EventCancel) : fix popevent event cancel (d995282d0)
- Fix recurring event cancellation in Event Settings and Event Details (7bb153124)
- feature(EventFragment) : changed Ui (ba5f40afc)
- feat(events): move reserved female toggle from step 2 to step 3 (91004a5ef)
- fix: Fix missing discussion box for online events and close page on leave/cancel (85a26ae9a)
- feature(HomeFragment) : first steps (57b98d787)
- feature(HomeFragment) : first steps (63b5a9461)
- feature(HomeFragment) : first steps (e998443ae)
- feature(HomeFragment) : first steps (77ecd7c96)
- feature(HomeFragment) : first steps (98382b8da)
- fix(build) (2f7f5433d)
- chore(build) (68bcd3734)
- feat: Add Welcome Journey section to Home screen (0fbea2faa)
- feature(EventFragment) : added leave event fragment (908c9305d)
- chore(libs) : return into 4.3.1 for place (126a407cb)
- feat: update event detail layout according to new design specifications (277e7a1e7)
- feat(event-detail): update event feed fragment layout and actions (e13c7a47f)
- Revert "Merge pull request #259 from ReseauEntourage/fix-places-sdk-migration-7462024471788387503" (013ba56db)
- Migrate Google Places SDK to 5.1.1 fields (b6d86a686)
- chore(libs.versions.toml) : update all libraries (fa9c19832)
- chore(build) (cfeb905ca)
- perf: optimize SimpleDateFormat instantiation in GroupEventsListAdapter (1f...)
- Move reserved_female switch to step two and remove info bubble (82456a6f1)
- Fix: Reduce start margin of home action cards to match section title alignment and other horizontal items (fdcba1a90)
- feat: move reserved female switch to step 3 in event creation (97cf69fec)
- chore(build) (3ed0882b2)
- Fix compile error on CreateEvent reserved_female metadata reference (2ce56e...)
- Fix reserved female icon visibility alongside Entourage badge and remove switch duplication (aeff8be87 area)
- feature(HomeFragment) : remove progressbar (aeff8be87)
- Move reserved_female field to EventMetadata (37f6b3e03)
- fix(HomeFragment) : charte etic click (87eaa96e7)
- feature(HomeFragment ) : minor UI fixes (d8386629b)
- feat: update ethical charter card to open a deep link instead of GroupRulesActivity (6b940b92f)
- Move reserved_female field to EventMetadata (779a79dd7)
- feature(HomeFragment) : adding padding on bottom (2dfb44749)
- feature(HomeFragment) : minor UI changes (717bb371d)
- feature(HomeFragment) : for action set number of line to preserve height (2...)
- feature(HomeFragment) : modified a card height (b97d302a1)
- Add fluid layout animations to HomeFragment RecyclerViews (3bd0d5f32)
- feature(HomeFragment) : action refacto beeing horizontal (22b15bb8a)
- Wording: Passage au vouvoiement pour la carte modérateur (f069e4916)
- Wording: Passage au vouvoiement pour la carte modérateur (e57be3f8b)
- Update Home action card design to match new mockup (2331f59c5)

### Code structure & content changes

**Home screen overhaul.** `HomeFragment.kt` received the largest single change of the release (+240/-113 lines), moving from a flat list of adapters toward a modular ConcatAdapter composition. Notable additions:
- `HomeWelcomeJourneyAdapter` (new, 202 lines) drives a new "Welcome Journey" onboarding card on the home feed: a 3-step checklist (watch a welcome video via a new `BottomSheetDialog` + `dialog_welcome_video.xml`, visit a webinar link, visit a "papotages" link) with step-state tracking, a completion celebration `PopupWindow` (`layout_celebration_tooltip.xml`), and new drawables for step badges (`bg_welcome_step_active/completed/future`, `bg_progress_bar_welcome`, `bg_welcome_success`).
- `HomeToolsAdapter` (new) and `layout_home_tools.xml` extract the map/pedagogy/ethical-charter shortcut cards into their own recycler section; the ethical-charter card now opens a deep link (`ETHICAL_CHARTER_ID`-based URL, added to `app/build.gradle.kts` for both `staging` and `prod` flavors) via `Intent.ACTION_VIEW` instead of launching `GroupRulesActivity`.
- `HomeModeratorAdapter` (new) replaces the old `HomeHelpAdapter`/`OnHomeHelpItemClickListener` mechanism with a dedicated moderator card (`home_moderator_item.xml`) showing the assigned moderator's avatar/name with vouvoiement wording ("Wording: Passage au vouvoiement pour la carte modérateur", two commits).
- `HomeActionAdapter`, `home_v2_action_item_layout.xml`, and `home_v2_pedago_item_layout.xml` were reworked for a new action-card mockup (horizontal layout, fixed line counts to preserve height, adjusted start margins/card heights) across roughly a dozen small "HomeFragment" iteration commits.
- Fluid list animations were added: `item_animation_slide_from_right.xml` / `layout_animation_slide_from_right.xml` for horizontal RecyclerViews (Events/Actions/Groups) plus a fall-down animation on the main vertical list.
- `HomePresenter.kt` was trimmed (-22 lines), and the old blocking progress bar on `HomeFragment` was removed (now hidden by default rather than shown while loading).
- A stray debug-only long-press/click handler (`testNotifDemandePage`, injecting a fake "birthday" push payload) was added to `HomeFragment` — appears to be leftover test scaffolding tied to `BirthdayActivity`/`PushNotificationManager` changes.

**Event cancellation & detail flow.** The recurring-event cancellation UI was added and then reverted within the same release (`7bb153124` → `891dfedca`), landing instead on a simplified, non-recurrence-aware flow: `SettingsModalFragment.cancelEventWithRecurrence()` and its custom recurrence-choice dialog (`layout_custom_alert_dialog_cancel_event.xml` usage) were deleted, replaced by a single `CustomAlertDialog.showWithCancelFirst(...)` call. `CustomAlertDialog` gained an optional `cancelText` parameter so callers can override the "no" button label; several call sites across the app were updated to the new `showWithCancelFirst` signature. `EventFeedFragment` now observes `eventPresenter.eventCanceled` and finishes the activity on cancellation, distinguishes organiser vs. participant ("Cancel"/"Leave" button text), shows/hides a `discussionBox` view instead of a separate `participateView`/`btnAddCalendar` pair, formats event start/end time as a range, and adds a `reserved_female` badge (`tvReservedFemale`). Several date-formatting helpers were centralized into `Utils.kt` (`formatEventDateShort`, `formatEventDateWithTime`, `formatEventDateLong`, `formatEventDateForDisplay`), replacing ad-hoc `SimpleDateFormat` usage in fragment code, and `GroupEventsListAdapter` was changed to avoid re-instantiating `SimpleDateFormat` per bind (perf commit).

**`reserved_female` event metadata.** A new nullable `reserved_female` field was added to `EventMetadata` (`api/model/MetaData.kt`) and threaded through event creation: the toggle switch was moved from step 2 to step 3 of event creation (`CreateEventStepThreeFragment`), with several follow-up fixup commits (compile error on a stale reference, icon visibility alongside the "Entourage badge", removing a duplicate switch, removing an info bubble). This was a multi-commit back-and-forth (moved twice, "Move reserved_female field to EventMetadata" appears twice) suggesting rebased/duplicated work rather than a single clean change.

**Google Places SDK migration — landed then reverted.** A commit (`b6d86a686`, authored by the `google-labs-jules[bot]` automation) migrated `Place.Field` usages from deprecated members (`NAME`, `LAT_LNG`, `ADDRESS`) to their new SDK 5.1.1 equivalents (`DISPLAY_NAME`, `LOCATION`, `FORMATTED_ADDRESS`) across `EventFiltersActivity`, `MainFilterActivity`, `OnboardingZoneChoiceActivity`, `EditProfileActivity`, and `UserActionPlaceFragment`. This was merged and then reverted (`013ba56db`, reverting PR #259), and a later commit pinned the `places` library version back down to `4.3.1` (`126a407cb`), indicating the migration caused a regression that wasn't ready to ship.

**Dependency/build updates.** `gradle/libs.versions.toml` had a broad version bump: Firebase BOM 34.6.0→34.10.0, Play Services Maps 19.2.0→20.0.0, Maps Utils KTX 5.2.2→6.0.0, Material 1.13.0 unchanged but Glide 4.16.0→**5.0.5** (major bump), Kotlin 2.2.21→2.3.10, AGP 9.0.0→9.0.1, Navigation 2.9.6→2.9.7, Ktor 3.2.3→3.4.1, Mockito 5.21.0/6.1.0→5.22.0/6.2.3, and others. The `androidx-multidex` dependency was dropped entirely. Test-related dependency declarations (`espresso-core`, `espresso-intents`, `androidx-test-*`) were reorganized into `androidx-test` and `espresso-test` bundles with a shared `espresso` version ref, and a new `espresso-contrib` dependency was added. `bitrise.yml` gained a new `pr_check` workflow (triggered on all pull requests) that runs a release-sources compile, posts a Slack notification to `#androiddev`, and posts a Jira comment with build details — a new CI gate for PRs that previously only built on push to `develop`.

**Dead code / screen removal.** Several legacy screens and their manifest entries were deleted outright: `ActionCategoriesFiltersActivity` + `ActionCategoriesFiltersListAdapter`, `ActionLocationFilterActivity` (346 lines), `MyActionsListActivity`, `ActivityChooseLanguage` (language-choice screen, replaced elsewhere), `WelcomeTestActivity`, and `WebViewActivityForTest` — along with their layouts (`activity_action_cat_filters.xml`, `activity_action_location_filters.xml`, `activity_choose_language_layout.xml`, `activity_my_actions_list.xml`, `webview_activit_for_test.xml`) and now-unused drawables (`new_profile_header.xml`, `new_profile_header_orange_dark.xml`, `planete.xml`). Roughly 80 dead/unused `AnalyticsEvents` constants (old Home/Profile/Event tracking keys, several blank trailing lines) were pruned from `AnalyticsEvents.kt`.

**Image viewer restructure.** The image-viewing feature was moved out of `discussions/imageviewier/` into a new `tools/image_viewer/` package: `ImageDialogFragment.kt` was deleted, `ImageDialogActivity` was renamed to `ImageViewerActivity`, and a new `ImageListActivity` + `ImageGridAdapter` were added (with corresponding layout rename `fragment_image_viewer.xml`). Call sites (e.g. `ActionSheetFragment`) were updated to the new package and to use a shared `Const.CONVERSATION_ID` extra key instead of a hardcoded string literal.

**Misc.** `ActionSheetFragment` gained small-talk leave-conversation handling (`isSmallTalk`/`smallTalkId` args, `RefreshController.shouldRefreshFragment` + `dismiss()`/`finish()` on leave) and refined event-membership visibility logic for the "quit" menu item. The app's launch mode changed from `singleInstance` to `singleTask` in the manifest. Version bumped 13.2 → 13.3 → 13.4 across the release.

---

## Version_13025089 — 2026-07-23

### Commits
- Revert "Update SmallTalk wording on Home screen for all languages" (f801e61d9)
- Revert "Update SmallTalk wording on Home screen for all languages" (ce6d469d3)
- fix(BirthdayActivity) : changed by a lottie (f72ea4bed)
- Revert "Fix: Use correct SmallTalk ID when leaving a conversation" (847a23337)
- fix(strings.xml) : fixed a wrong caracter (9d035db48)
- Update SmallTalk wording on Home screen for all languages (73c311084)
- Fix: Use correct SmallTalk ID when leaving a conversation (030f7a051)
- chore(VersionName) : 13.1 -> 13.2 (6d8f40779)
- fixed(EventFeedFragment + others) : fix condition in the app (a1f41d4a0)
- feature(NotificationActionManager) : reroute the notification click (a178605d2)
- Handle 'birthday' stage for in-app and push notifications (ae0f24501)
- fix(Ambassador) : fixed ambassador name (d19ee7143)
- fix(UniversalLinkManager) : fixed endpoint (a817c6aad)
- Add webinar deeplink handling and sensibilisation event API call (b5b7531d4)
- feat: Display last message date for discussions and event date for ... (d8897e549)
- Update UniversalLinkManager.kt (fe8d29559)
- Add deep links for Good Waves, Event Creation, and Solidarity Chat (afafccb9d)
- feature(GuidMapFragment) : EN-8835 changed button map (cfaf14790)
- chore(strings.xml) : fix build with & (5fb4ba57d)
- Replace Guide '+' button with 'Help & Orientation' button (f13a1c4c1)
- Fix Home tab redirection issue in MainActivity. (8ba8051f5)
- Refactor onboarding navigation logic to use sealed class and Intent extras (58a1616b9)
- Fix Home tab redirection loop by consuming Intent extras (45af1617e)
- Fix Home tab redirection loop by using Intent extra for event navigation (828d2afe3)
- fix(OnboardingPhase1Fragment) : change other to not specify (b45896e88)
- fix(EditProfileActivity) ; changed other to "not specified" (0aa6cd1c9)
- fix(strings.xml) : changed a wording in EditProfile (95c0da7bd)
- Refresh user profile data on Resume (ed74d9785)
- feat: Condition 'Se sensibiliser' visibility on user wish (38f82ffd7)
- fix(ZoneChoiceActivity) : fix visibility of potential events (b92b1e7b3)
- feat(profile): update profile display for association users (84c4ffa6a)
- Rename 'Ambassadeur' to 'Animateur Entourage' and optimize role display (492037624)
- Rename 'Ambassadeur' to 'Animateur Entourage' and optimize role display (8c41f8962)
- Rename 'Ambassadeur' to 'Animateur Entourage' and optimize role display (08d6404e2)
- feature(EntourageUser) : changed field for birthday (f10dc50ff)
- Rename 'Ambassadeur' to 'Animateur Entourage' and optimize role display (6741c2111)
- chore(build) (599ee5ea7)
- chore(update gradle) : 8.13 -> 9.1.0, and mixed with java 17 (74fcc8f25)
- fix(build) (db42602b9)
- fix(HomeFragment) : fix the upgrade of recyclerview (712fa0fec)
- feature(OnboardingZoneChoiceActivity) : fix UI map counter (a86769c5e)
- chore: removing useless api key (21947412a)
- chore: reorganize and enhance Android instrumentation tests (a1cfaa066)
- chore: cleanup string resources and refactor action creation (2e192e3df)
- feat: Migrate from Java 11 to Java 17 (20210b419)
- feat: Migrate from Java 11 to Java 17 (ad01957a3)
- feat: Migrate from Java 11 to Java 17 (883f6c15b)
- fix(EditProfileActivity) : fixed date format (95cba1fac)
- feature(DiscussionListAdapter) display camera emoji for image-only list (27ba5e4fa)
- Add weekly event stats to onboarding zone choice (cb9dacfce)

### Code structure & content changes

- **Java 17 / Gradle 9 migration**: `app/build.gradle.kts` moved `sourceCompatibility`/`targetCompatibility`/`jvmTarget` from Java 11 to 17, and `gradle/wrapper/gradle-wrapper.properties`/`gradle.properties` were bumped for Gradle 9.1.0, alongside `buildToolsVersion` 36.0.0 → 36.1.0 and further `libs.versions.toml` cleanup (androidTestImplementation now correctly scopes espresso).
- **HomeFragment RecyclerView rewrite**: `HomeFragment.kt` (818 lines touched) replaced its collection of separately-managed adapters with a `ConcatAdapter` composed of new reusable building blocks — `HomeSectionHeaderAdapter`, `HomeSectionButtonAdapter`, `HomeHorizontalWrapperAdapter`, `HomeSingleLayoutAdapter` (all new files) — giving each home section (sensibilisation/pedago, small talk, actions, events, groups, map/"hors zone") a consistent header+content+button structure instead of bespoke per-section wiring. `fragment_home.xml` shrank drastically (-473 lines) as markup moved into the new adapter-driven item layouts (`home_section_header.xml`, `home_section_button.xml`, `home_horizontal_recycler_view.xml`, `home_map_card.xml`).
- **Birthday feature**: new `BirthdayActivity.kt` + `activity_birthday.xml` show a Lottie animation (`birthday_animation.json`) celebrating the user's birthday; `NotificationActionManager.kt` and `PushNotificationManager.kt` gained a "birthday" stage handled for both in-app and push notifications, and `EntourageUser`/`User`/`Summary` models were extended with a birthday-related field.
- **Onboarding navigation refactor**: new `OnboardingNavigation.kt` sealed `Parcelable` class (`Home`, `WelcomeGroup`, `Events`, `Donations`, `CreateActionDemand`, `Quiz`, `Profile`) replaces a set of loose boolean flags (`shouldLaunchEvent`, `shouldLaunchActionCreation`, `shouldLaunchQuizz`, `shouldLaunchWelcomeGroup`, …) previously read off `MainActivity` state; `MainActivity.kt` (349 lines touched) now routes redirection through Intent extras carrying this sealed type, fixing a Home-tab redirection loop. A `TestHelper.kt` utility (`isRunningInTestHarness()`) was added to avoid clearing the launch `Intent` during instrumentation tests.
- **Role renaming**: "Ambassadeur" was renamed to "Animateur Entourage" across the app (multiple commits) with role-display logic optimized — consistent with the CLAUDE.md-documented `"Animateur Entourage"` role string. `profile/AssociationProfileActivity.kt` (new, 143 lines) presents a dedicated profile screen for `Association`-role users, backed by an extended `AssociationPresenter.kt` (103 lines) and `Partner.kt` model changes.
- **Deep links expansion**: `UniversalLinkManager.kt`/`UniversalLinkPresenter.kt` added handling for webinar links, Good Waves, Event Creation, and Solidarity Chat deep links, plus new `getEventSmallTalk()`/`getEventSensibilisation()` calls backed by new `EventsRequest` endpoints; an incorrect endpoint used by the link manager was fixed.
- **CountryCodePicker de-ViewBinding**: `CountryCodePicker.kt` (313 lines touched) dropped `LayoutCodePickerBinding` in favor of manually-held view references, as part of a broader login-screen layout overhaul (`activity_login.xml`, `activity_login_change_phone.xml` rewritten ~350 lines each; the `layout-h720dp` variant of `activity_login.xml` was deleted in favor of a single adaptive layout).
- **Instrumentation test reorganization**: `app/src/androidTest` tests were reorganized into `beforeLogin/`, `afterLogin/`, and `unchecked/` packages (e.g. `LoginTest`, `SignUpTest`, `PreOnboardingTest` → `beforeLogin/`; `PushNotificationTest`, `UniversalLinkManagerTest`, `MyEntouragesTest` → `afterLogin/`; `DeeplinksTest`, `GuideMapTest`, `HomeExpertTest`, `OnboardingTest` → `unchecked/`), with new tests added (`CreateActionActivityTest`, `InAppDisplayTest`, `OpenUniversalLinkManagerTest`, `UniversalLinkManagerTestWithHack`) and old catch-all files (`EntourageTestAfterLogin`, `EntourageTestBeforeLogin`) split up.
- **Guide screen**: `GuideMapFragment.kt` replaced the "+" quick-action button with a "Help & Orientation" button (EN-8835), adjusting `fragment_guide_map.xml`; the long-click helper layout `layout_guide_longclick.xml` was removed.
- **Misc fixes/reverts**: several wording/ID fixes for SmallTalk (leaving a conversation using the correct SmallTalk ID) were shipped then reverted twice in quick succession, indicating an unstable rollout later reverted; `EditProfileActivity`/`OnboardingPhase1Fragment` changed the "Other" gender/option label to "not specified"; `EditProfileActivity` date-format fix; `DiscussionListAdapter` now shows a camera emoji for image-only messages in the conversation list; profile data now refreshes on `onResume`.

---

## Version_12104963 — 2026-07-23

### Commits
- fix(EditTextExtension): fix year selection in `transformIntoDatePicker` (67ce7f91f)
- chore(VersionName) : 12.9 ->12.10 (79bbd66ab)
- feature(HomeFragment) : Gating home for fullfilling many informations (d00473776)
- fix(AssociationRequest) : fix calls ongoing (5afa79a5b)
- chore(OnboardingAPI) : change compagny and event field in subscription (7e81c7080)
- refactor(build) : firebase BOM up to 34.6.0 (fb9bbe078)
- refactor(build) : add dependabot yaml file (1f4da8ff9)
- refactor(build) : remove duplicate lib (990b5242a)
- refactor(build) : add OSS bundle (d7b9349d2)
- refactor(build) : migrate plugins to version catalog (79c25c2b7)
- chore: migration to version catalog (aed9d1ad6)
- chore(deps) : bump aboutlibraries to 13.1.0 (e18c22918)
- chore(deps) : bump aboutlibraries to 12.2.4 (3f0b7092f)
- feat(OSS): add open source licenses screen using AboutLibraries (ea94804fb)
- chore(new lib system) : switch to new way for lib, with toml (3151e1846)
- fix(OnboardingPhase1Fragment) : added Firebaseevent (9a0db1d84)
- feature(strings.xml) : wording (3d9087bdd)
- feature(ActionSheetFragment) : expanding right to modify to users EN-8714 (96071dfcb)
- feature(HomeSmallTalkAdapter) : adding new message EN-8538 (1d9b3cd8b)
- feature(HomeSmallTalkAdapter) : adding new message (98db7cecf)
- feature(PartnerOnboardingActivity) : new asso onboarding EN-8585 EN-8430 (a93f7ec68)
- fix(EnhancedOnboarding) : remove UserPresenter usage and calls (0517c6f5f)
- fix(BaseSecuredActivity) : skip language selection if already set (05b946070)
- fix(build) : upgrade Gradle to 8.13, AGP to 8.13.1 and update runCommand (b2dcc48ef)
- refactor(app) : centralize SharedPreferences access and cleanup AuthenticationController (748ebd346)
- fix(resources) : rename bottom nav and background drawables (f7e34d93e)
- fix(MainActivity) : set specific bottom bar color for staging debug (d6eae6601)
- refactor(ProfileFullActivity) : split specific activity for my profile vs others (f1a4a5950)
- Update app/src/main/java/social/entourage/android/discussions/DiscussionsMainFragment.kt (b92980482)
- fix : EN-8674 fix crash on showDetail if position out of bounds in DiscussionsMainFragment (596192ee1)
- fix(ProfileFullActivity) : EN-8555 fix crash in Profile, extends BaseSecuredActivity and secure user check (d6e1a9759)
- fix: fix crash on action creation success appeared on EN-8209 (CreateActionFragment) (9d7938c0f)
- fix : fix crashes in fragments in onDestroy (1fafaea39)
- fix: EN-8556 prevent crash on autocomplete suggestions (EditProfileActivity) (d023e4887)
- Update app/src/main/java/social/entourage/android/discussions/DiscussionsPresenter.kt (04b02935f)
- chore: refactofring DiscussionsMainFragment (e4604aeda)
- fix: EN-8535 fix error view onDestroy in CreateActionStepThreeFragment (d9ebe4279)
- feat(OnboardingEditPhotoFragment): EN-8553 handle SecurityException on photo edit (bf594fb5b)
- fix(ProfileFullActivity): EN-8541 fix crash when phone was null (cd96cc9e3)
- chore: add a monochrome icon for material3 design (36357fae1)
- fix(ActionSheetFragment) : supress corrected (cbc1193dd)
- fix(EventFeedActivity) : remove quit button if not part of event (3c47251b2)
- fix(OnboardingZoneChoiceActivity) : fix top margin (114e617d4)

### Code structure & content changes

- **Build system modernization**: the Gradle build migrated to a version catalog (`gradle/libs.versions.toml`, +163 lines) — all `implementation("group:artifact:version")` declarations in `app/build.gradle.kts` were replaced with `libs.*` aliases, plugins moved to `alias(libs.plugins.*)`, and Gradle/AGP were bumped to 8.13/8.13.1. Firebase BOM bumped to 34.6.0, duplicate libraries removed, and `.github/dependabot.yml` added for automated dependency PRs.
- **Open-source licenses screen**: new `OSSLibsActivity.kt` (using `com.mikepenz:aboutlibraries`, bumped 12.2.4 → 13.1.0) renders an OSS-libraries screen via `LibsBuilder`, replacing a previous manual implementation; `activity_oss_libs.xml` added.
- **`ProfileFullActivity` split**: `ProfileFullActivity.kt` became an abstract base `OpenProfileFullActivity : BaseSecuredActivity()` with two concrete subclasses, `MyProfileFullActivity` (own profile) and `ProfileFullActivity` (viewing another user), replacing the previous single-activity, `BaseActivity`-derived design — this also fixes EN-8555 (crash in profile) and EN-8541 (crash when phone was null) by centralizing secured-access checks.
- **New partner/association onboarding**: `PartnerOnboardingActivity.kt` (new, 302 lines) and `AssociationViewModel.kt` (new, 188 lines) implement a new "join via association/partner" onboarding path (EN-8585/EN-8430) with autocomplete partner search seeded by location passed from `OnboardingZoneChoiceActivity`.
- **`AuthenticationController` / SharedPreferences cleanup**: dead/unused preference accessors were removed (`isTutorialDone`, `entourageDisclaimerShown`, `isOnboardingUser`, `editActionZoneShown`, `isShowNoEntouragesPopup`, `mapFilter`, `saveMapFilter`, `saveMyEntouragesFilter`), and a null-token user is now routed through `logOutUser()` instead of being silently nulled — part of a broader centralization of SharedPreferences access (`EntourageApplication.kt`, `ComplexPreferences.kt`).
- **`HomeFragment` gating**: large rework (574 lines) gating home-screen content behind "complete your profile" prompts/conditions.
- **Bottom navigation asset rename**: `new_bottom_navigation_*.xml` drawables were renamed to `bottom_navigation_*.xml` / `ic_bottom_navigation_active_*` / `ic_bottom_navigation_inactive_*`, and a monochrome adaptive icon was added for Material You theming.
- **Stability pass**: numerous crash fixes across fragments' `onDestroy`/`onDestroyView` (EN-8535, EN-8556, EN-8674, EN-8553 SecurityException handling on photo edit), plus `EventFeedActivity` no longer shows the "quit" option for non-participants and `ActionSheetFragment` gained expanded edit rights (EN-8714).

---

## Version_12094905 — 2025-12-29

### Commits
- fix(seebar_thumb) : fix seekbar size ellipse (8ba923858)
- fix(MainActivity) : fix navcontroller bug EN-8105 (3fb444831)
- fix(onboarding) very very minor fixe (35acc209a)
- fix(onboarding) minor fixe (bbc53a9a8)
- chore(versionName) : 12.8 -> 12.9 (dd558aa1d)
- fix(onboarding) minor fixe (d6517c96d)
- fix(onboarding) minor fixe (070e34268)
- fix(OnboardingZoneChoiceActivity) : dropdown fixed (dbee2d32b)
- fix(OnboardingStart) : fix many UI UX matter (ff0b38604)
- fix(new_fragment_setting_discussion_modal_fragment) : fix ui (eb6960554)
- fix(new_fragment_setting_discussion_modal_fragment) : fix ui (86ea42e1c)
- fix(HomeFragment & OnbordingPhase1Fragment) : fix lifecycle crash (ce9b661ea)
- feature(OnboardingAssociationChoice) : asso path (0c879f819)
- feature(OnboardingAssociationChoice) : asso path (384205b55)
- feature(OnboardingZone) : correct call to zone and reditection from... (d7ab80ab2)
- feature(OnboardingZone) : correct call to zone and reditection from... (7f270ecc3)
- feature(Onboarding) : refactoring onboarding UI and YUX (8e660b84d)
- fix(new_fragment_setting_discussion_modal_layout) : EN-8564 fixed s... (4d28d2c8c)
- fix: EN-8396 fix crash when trying to Toast on photo failure (862f5b880)
- fix: EN-8443 fix crash when closing screen (cc88c0263)
- fix(MemberListAdapter) : adding me signable (a31ae4a6e)
- feature(ImageListActivity) : add pagination on Image list (47bfeda57)
- fix(ActionSheetFragment) : EN-8563 fix deleting event (9f8cc09a8)
- fix(DetailConversationActivity) : EN-8562 fix redirection user for ... (fce63222c)
- fix(EventChart) : fix last item (760e31cd7)

### Code structure & content changes

- **New onboarding screens**: `OnboardingAssociationChoiceActivity.kt` (new, 156 lines) adds an "association picker" step (mock dropdown list of associations with "Autre"/Other free-text fallback), and `OnboardingZoneChoiceActivity.kt` (new, ~330 lines) adds a Google Maps + Places Autocomplete zone-selection step with a radius seekbar circle overlay, backed by new drawables `seekbar_thumb.xml`/`seekbar_track.xml`. `ProfileChoiceAdapter.kt` is a new adapter for profile-type selection (`item_onboarding_profile_choice.xml`).
- **Onboarding UI/UX refactor**: `OnboardingPhase1Fragment.kt`, `OnboardingPhase2Fragment.kt` (+353/-lines), `OnboardingPhase3Fragment.kt`, `OnboardingStartActivity.kt`, and their layouts (`activity_onboarding_start.xml`, `fragment_onboarding_phase2.xml`, `fragment_onboarding_phase3.xml`) were substantially reworked as part of a broader onboarding redesign, adding new drawables (`ic_been_entoured_onboarding.xml`, `onboarding_asso.xml`, `otp_box.xml`, `ic_info_24.xml`) and many new/changed strings across all locale files (fr, en, es, de, pl, ro, uk, ar).
- **MainActivity navigation fix** (EN-8105): simplified the bottom-nav item-selection handling — removed manual `navController.navigate()` calls for home/donations/messages tabs (now purely `AnalyticsEvents.logEvent`), letting `NavigationUI.onNavDestinationSelected` own all navigation to fix a NavController bug.
- **Discussion image pagination**: `ImageListActivity.kt` gained pagination (100 lines added) and `DetailConversationActivity.kt`/`DiscussionsRequest.kt` were extended accordingly; `DetailConversationActivity.kt` also fixed a user-redirection bug (EN-8562).
- **Crash fixes**: lifecycle-safety fixes in `HomeFragment.kt` and `OnboardingPhase1Fragment.kt`; photo-failure Toast crash fix; screen-close crash fix (EN-8443); `ActionSheetFragment.kt` event-deletion fix (EN-8563, +237/-lines overall touching sheet-mode visibility logic).
- **Misc**: `MembersListAdapter.kt` now marks "me" as a signable member; seekbar thumb ellipse sizing fix; discussion settings modal UI fixes (EN-8564).

---

## Version_12084879 — 2025-12-29

### Commits
- fix(DiscussionMainFragment) : fix discussion list (b200de6cf)
- fix(EventFeeedFragment) : fix modify evnet from everyone (c91ca940c)
- fix(EventFeeedFragment) : fix modify evnet from everyone (b868cd104)
- fix(EventFeeedFragment) : fix a title and photo back button (336fcaaa8)
- chore(VersionName) : 12.7 -> 12.8 (c749296b5)
- fix(EventFeeedFragment) : fix a title and photo back button (9e193a521)
- feature(OnboardingPhase1) : new subs with material design (f0e92e77a)
- feature(OnboardingPhase1) : new subs with material design (c37f8c7cc)
- feature(OnboardingPhase1) : new subs with material design (b6980c5a0)
- feature(OnboardingPhase1) : new subs with material design (78e26aab4)
- fix(ActionSheetFragment) : button modify repaired (f403500ff)
- chore(rebuild) (9286fbc50)
- feature(GroupeRuleActivity) : new event chart (9b7094e04)
- feature(GroupeRuleActivity) : new event chart (77265458f)
- fix(GroupDetailsFragment) : fix UI (049caded0)
- feature(ImageListActivity) : EN-8505 adding photo list activity (3f5962464)
- feature(OnboardingPhase1Fragment) : new field subscription (ecce7fb4e)
- fix(DiscussionListAdapter) : if outing do not add member (a0e83e984)
- fix(OnboardingAPI) : good field to subscribe user (cc55b85a7)
- fix(OnboardingAPI) : good field to subscribe user (d151ecb85)
- feature(OnboardingPhase1Fragment) : new field subscription (34a8b0580)

### Code structure & content changes

- **Onboarding phase 1 redesign**: `OnboardingPhase1Fragment.kt` (~700 lines touched) and its layout `fragment_onboarding_phase1.xml` were substantially rewritten for a Material Design subscription form — new spinner/dropdown item layouts (`item_spinner_text.xml`, `item_spinner_dropdown_text.xml`), new outline drawables/colors (`bg_spinner_outline.xml`, `ent_textinput_hint.xml`, `ent_textinput_stroke.xml`, `ent_end_icon_tint.xml`). `OnboardingAPI.kt` was updated with the correct field names for the subscription call, and a new `PreloginModels.kt` was added defining `Metadata`/`UserMetadata`/`TagsMetadata` and related tag data classes (sections, interests, involvements, concerns, signals, POI categories) consumed by the new onboarding flow.
- **New photo viewer**: `ImageListActivity.kt` and `ImageGridAdapter.kt` are new (EN-8505), showing a grid of conversation image thumbnails (`activity_image_list.xml`, `item_image_thumb.xml`) with a "view all photos" entry point wired from `ActionSheetFragment`. `DiscussionsPresenter.kt` gained `fetchConversationImages()`/`fetchConversationLargeImage()` plus `conversationImages`/`largeImage` LiveData, backed by new `DiscussionsRequest` endpoints and `ConversationImage`/`ConversationImagesWrapper`/`ConversationImageSingleWrapper` models in `Discussions.kt`.
- **Event rules/charter**: `GroupRulesActivity.kt` and `RulesListAdapter.kt` were reworked to support sectioned content for events — the adapter now distinguishes `TYPE_SECTION` vs `TYPE_RULE` view types (new `EventRulesSectionItemBinding`/`event_rules_section_item.xml`), and the event rule set expanded from 4 flat items to a "positive" and "negative" section grouping 8 rules total; `GroupDetailsFragment.kt` and `ic_edit_event.xml` support an edit-event icon.
- **ActionSheetFragment**: substantial rework (~280 lines) around modify/edit visibility per sheet mode (group, discussion, event), and a fix restoring the broken "modify" button behavior.
- **Housekeeping**: `app/build.gradle.kts` version bump (12.7 → 12.8), new string resources across all locale files for the onboarding/event-chart features, and unrelated small fixes to `CommentsListAdapter`, `MembersListAdapter`, `DiscussionsListAdapter` (skip adding members for outings), `EventFeedFragment` (title/back-button fixes).

---

## Version_12074858 — 2025-12-29

### Commits
- fix(OnboardingPhase1Fragment) : adding additionnal protection (614f00fec)
- fix(OnboardingPhase1Fragment) : fix crash context on subscribe (4bd059856)
- fix(PostAdapter) : fix asso name (6b50edf3d)
- feature(UniversalLinkManager) : link for chart and image event (592758ceb)
- chore: remove service from manifest (b11e4ba54)
- chore:removing old share pref V7 (e98acb3fa)
- fix(ActionSheetFragment) : smalltalk leaving (06262f641)

### Code structure & content changes

- **Onboarding hardening** (`OnboardingPhase1Fragment.kt`): the fragment moved from an unsafe `lateinit var binding` to a nullable `_binding`/`binding` pattern with `onDestroyView()` cleanup, plus new `isViewUsable()`/`safeUI{}` helpers guarding every UI mutation. The "corporate awareness" string comparison was cached once in `onViewCreated` (`labelCorporateAwareness`) instead of repeated `getString()` calls, and several call sites switched to null-safe `activity as?`/`context ?:` patterns to prevent crashes when the fragment is detached (fixes a subscribe-time crash and a general robustness pass).
- **Deep links**: `UniversalLinkManager.kt` gained a `chart-event` path segment that opens `GroupRulesActivity` with `Const.RULES_EVENT`, giving event charters/rules a direct universal link entry point (per the CLAUDE.md convention of preferring `GroupRulesActivity` over a browser URL).
- **Small talk vs. regular conversations**: `ActionSheetFragment.kt` now instantiates a `SmallTalkViewModel` and branches the "leave conversation" action based on `DetailConversationActivity.isSmallTalkMode`, calling `smallTalkViewModel.leaveSmallTalk()` instead of the generic `discussionPresenter.leaveConverstion()` when appropriate.
- **Misc fixes**: `PostAdapter.kt` now displays the partner/association name (`post.user?.partner?.name`) for posts authored by an "Association" role user. A leftover `EntService` `<service>` declaration was removed from `AndroidManifest.xml`, and legacy "V7" SharedPreferences cleanup code was removed. `app/build.gradle.kts` bumped `versionMinor` 6 → 7.

---

## Version_60802690 — 2020-12-10

### Commits
- chore: bump to version 6.8 (ca448217d)
- chore: add automatic changelog for release 6.7 (ac87841db)
- [EN-3110] Add search inside GDS (de077048a)
- [EN-3291] wording (a33893bab)
- [EN-3290] [EN-3230] Add info event online -> feed list (e109e6584)
- [EN-3295] Add check + pop if first & last name empty (a5e41ce79)
- [EN-3058] change back arrow size (f8cd0367e)
- [EN-3292] Change image GDS Hub + round image (71f62c6ce)
- [EN-3262] Change wording message list (ccf3b68a1)
- [EN-3309] wording + mod detail poi (c916df3f2)
- [EN-3293] change wording share event/action (53612ab93)
- Add Analytics Search (5d36ecc90)
- [EN-3318] add analytics filters GDS (0d81337b5)
- [EN-3309] update mail information poi id (2a6fea106)
- [EN-3288] add reset filters inside GDS filter (23832c029)
- [EN-558] change wording -> when about user not empty (944963c7b)
- [EN-1003] Change profil menu view (687e421f7)
- [EN-1003] fix sharing (4ad61aec1)
- [EN-3289] Tab agir change wording + link for goal=organization (555fc55ed)
- Fix crash (9932297d0)
- Fix crash bis (7b9f37f56)
- [EN-2107] add report user/entourage inside 1to1 messaging (29636ea8f)
- fix: change event information screen to display right icon chore: fix typo ...
- chore: some hidden fixes on tour functions (8917af973)

### Code structure & content changes

- **GDS (Guide de Solidarité) search** — new `GDSSearchFragment` + `GDSSearchAdapter` (`app/src/main/java/social/entourage/android/guide/`) with a matching `fragment_g_d_s_search.xml` layout and `layout_search_poi_empty.xml` empty state. `PoiRequest` gained a `retrievePoisSearch()` endpoint (`pois.json` with `query`/`v` params) to back it. `GuideFilterFragment` was extended with a "reset filters" action and per-category analytics events (`ACTION_GUIDE_SEARCHFILTER_ORGANIZ/DONAT/VOLUNT`).

- **Onboarding — first/last name capture** — new `InputNamesFragment` dialog (`onboarding/`) backed by a new `OnboardingAPI` request interface, with validation that blocks submission when first/last name are empty. Layout `fragment_inputs_names.xml` added.

- **Profile menu rework** — `MainPresenter.handleMenu(@IdRes menuId)` was replaced by `handleMenuProfile(menuPosition: String)`, moving menu dispatch from Android resource IDs to string keys (e.g. `"editProfile"`, `"charte"`, `"scb"`, `"ambassador"`, `"appVersion"`). `DeepLinksManager` and `MainActivity` were updated to call `selectMenuProfileItem(...)` instead of `selectItem(R.id...)`. `layout_mainprofile.xml` and `MainProfileFragment.kt` received a large layout/behavior overhaul (+593/+69 lines) to support the new menu, and `fragment_about.xml` / `EntourageAboutFragment` gained new content bindings referenced from the profile screen.

- **Entourage/feed information screens refactor** — `FeedItemInformationFragment` was trimmed substantially (net -279 lines): dead code, unused imports, and commented-out blocks removed; `entourageServiceConnection` renamed to `serviceConnection`; conversation-type detection switched from a type/id comparison to an `is EntourageConversation` check. `EntourageInformationFragment` grew (+265/-~) to surface the "event happening online" info introduced in the newsfeed (`FeedItem`, `NewsfeedPresenter`, `FeedItemViewHolder`, `NewsFeedFragment`/`NewsFeedWithTourFragment`). `TourInformationFragment` and `EntourageReportFragment` were adjusted in step (1:1 reporting for users/entourages inside conversations).

- **API layer** — `ApiModule` now registers a `BaseEntourageJsonAdapter` type adapter with the shared Gson instance, and `BaseEntourage`/`Entourage`/`LastMessage`/`LastMessageAuthor` model classes were adjusted accordingly (parsing fixes tied to the "Fix crash" / "Fix crash bis" commits).

- **Cleanup** — `map/filter/MapFilterFactory.kt` was deleted (19 lines removed, logic superseded by the GDS filter/search work). Several new vector drawables were added (`ic_back_new`, `ic_close_new`, `ic_search_new`, `ic_exit`, `ic_fb`, `ic_insta`, `ic_twit`) to support the reworked profile and search UIs, and `hub_number3_bg` raster assets were resized/normalized across densities.

- **Build/versioning** — `build.gradle`, `app/build.gradle`, and `dependencies.gradle` were bumped for the 6.8 release; `changelogs/changelog.sh` was tweaked and a legacy changelog file for `Version_60702534`→`Version_60702535` was added under `changelogs/` (later superseded by this consolidated `CHANGELOG.md`).

---
