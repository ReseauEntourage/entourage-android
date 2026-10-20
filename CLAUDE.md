# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build commands

Single flavor (`entourage`) × 3 build types: `debug` (default, staging API, debuggable), `preprod` (staging API, release-signed, used for QA/Bitrise builds), `release` (prod API). The active variant for day-to-day dev is `EntourageDebug`.

```bash
# Compile check (fastest)
./gradlew :app:compileEntourageDebugKotlin

# Check resources
./gradlew :app:mergeEntourageDebugResources

# Build APK
./gradlew assembleEntourageDebug
./gradlew assembleEntouragePreprod
./gradlew assembleEntourageRelease

# Run unit tests
./gradlew :app:testEntourageDebugUnitTest

# Run a single test class
./gradlew :app:testEntourageDebugUnitTest --tests "social.entourage.android.SomeTest"
```

Never use bare `compileDebugKotlin` — always prefix with the `entourage` flavor (`compileEntourageDebugKotlin`).

## Architecture

**No DI framework.** Dependencies are wired manually via `EntourageApplication` (the singleton), which holds:
- `apiModule: ApiModule` — the Retrofit/OkHttp layer, instantiated once
- `authenticationController: AuthenticationController` — session/token management
- `complexPreferences: ComplexPreferences` — serialized user object in SharedPreferences

Access pattern everywhere: `EntourageApplication.get().apiModule.homeRequest.getSummary()`  
Current user: `EntourageApplication.get().me()` or `EntourageApplication.me(activity)`

**Naming convention for "Presenters"**: some classes named `*Presenter` are actually `ViewModel` subclasses (e.g. `HomePresenter : ViewModel()`). Others are plain classes with a reference to their Activity/Fragment (e.g. `MainPresenter(activity)`). Check the superclass before assuming lifecycle scope.

**Network layer**: Retrofit 2 + OkHttp. Each feature has its own `*Request` interface in `api/request/`. All requests go through `ApiModule`, which applies `AuthenticationInterceptor` (adds the user token header) and `HmacInterceptor` (signs account-creation calls). Base URLs are injected via `BuildConfig.ENTOURAGE_URL` — staging vs prod differ only here.

**Serialization**: JSON goes through kotlinx.serialization (Gson is gone). Shared config is `AppJson` (`api/json/`): unknown keys ignored, lenient numbers, `null`/unknown enum → Kotlin default (`coerceInputValues`), defaults written, nulls omitted. Rules for models:
- Annotate with `@KSerializable` (import `kotlinx.serialization.Serializable as KSerializable`, to avoid the clash with `java.io.Serializable`) and `@SerialName("json_key")`; `Date` properties work through `@file:UseSerializers(ApiDateSerializer::class)`.
- **Give every property a default** (`= null`, `= emptyList()`, `= 0`…): a missing key without default throws `MissingFieldException`, and the app swallows parse failures in `onFailure`. Wrapper roots (`class FooWrapper(@SerialName("foo") val foo: Foo)`) are deliberately required.
- Request bodies: prefer a `@Serializable` DTO. Legacy `@Body ArrayMap<String, Any>` still works (converted by `AnyMapConverterFactory`; nulls are dropped), but new code should not add more.
- No `@Expose`-style asymmetry: a property is read *and* written (`@Transient` to exclude it from both).

**Deep links / universal links**: `UniversalLinkManager` (`deeplinks/`) handles all `entourage://` and `https://www.entourage.social/app/…` URLs. Entry point is `MainActivity.handleUniversalLinkFromMain()`. When adding a new screen reachable from a link, add a `pathSegments.contains("your-path")` branch there.

**User roles** drive feature visibility in the UI. Key role strings:
- `"Animateur Entourage"` — ambassador (bénévole animateur)
- `"Équipe Entourage"` — internal team
- `"Association"` — partner association

Check with `user.roles?.contains("Animateur Entourage")`.

## Source layout

```
app/src/main/java/social/entourage/android/
├── api/
│   ├── model/          # Data classes (User, Summary, Events, Group…)
│   └── request/        # Retrofit interfaces (one per domain)
├── base/               # BaseActivity, BaseSecuredActivity (redirects to login if no session)
├── deeplinks/          # UniversalLinkManager — all deep link routing
├── home/               # HomeFragment, HomePresenter, moderator/pedago/chatbot sub-packages
├── profile/            # MyProfileFullActivity, ProfileFullActivity (others' profiles)
├── events/             # Event creation, list, detail
├── groups/             # Neighborhood groups
├── actions/            # Contributions & solicitations
├── discussions/        # Messaging / conversations
├── tools/
│   ├── log/            # AnalyticsEvents (Firebase) — use logEvent() for all tracking
│   └── utils/          # Const (shared extras keys, role strings, rules types)
└── MainActivity.kt     # Bottom-nav host, deep link entry point
```

## Key conventions

**ViewBinding only** — no `findViewById`. Layouts use `snake_case` IDs; binding fields are `camelCase`. For `<include>` tags, bind the included layout with `LayoutXyzBinding.bind(binding.includeId.root)`.

**Analytics**: log every meaningful user action with `AnalyticsEvents.logEvent(AnalyticsEvents.ACTION__FEATURE__ACTION)`. Constants live in `AnalyticsEvents.kt` — add new ones there, following the existing `VIEW__` / `ACTION__` prefix pattern.

**Opening a conversation** with a user by ID: `discussionsPresenter.createOrGetConversation(userId.toString())` — then observe `discussionsPresenter.newConversation` to get the `Conversation` object and launch `DetailConversationActivity`.

**Rules/charter screens**: `GroupRulesActivity` takes `putExtra(Const.RULES_TYPE, Const.RULES_EVENT)` or `Const.RULES_GROUP`. Prefer this over opening a browser URL.

**Strings**: French is the source language (`values/strings.xml`). Run `./add_strings.sh` to add a new string and propagate stubs to other locale files. Do not add strings only in `values-en/` or other locales directly.

## Testing

**Whenever you modify code for a screen/view, update or add that screen's end-to-end scenario test.** This applies even when the task itself wasn't described as a testing task — a UI/behavior change without a matching e2e update is incomplete.

E2E scenarios live under `app/src/androidTest/java/social/entourage/android/e2e/`, directly on `develop`. Compile with `:app:compileEntourageDebugAndroidTestKotlin`.
- JSON safety net (JVM, `app/src/test/.../api/`): `ApiJsonCharacterizationTest` / `UserJsonCharacterizationTest` (read fixtures in `src/test/resources/api/`), `WriteRequestFixturesTest` (real request bodies captured from the creation scenarios, in `resources/api/requests/`), `LocalPersistenceCharacterizationTest` (on-disk format). When you change a model or a request body, update the matching fixture — a failing fixture test means the wire format changed.
- `ApiReadEndpointsSmokeTest` (e2e) calls ~50 read endpoints with the test account and fails if a real response no longer decodes into the models. Run it after touching `api/model` or `api/request`.
- Creation scenarios create real objects named `Unit test …` (group, event, action, post, message) and clean up after themselves where the API allows it (groups and events remain: purge by the `Unit test` prefix). They need `adb emu geo fix 2.3522 48.8566` and a French device/app locale.
- These tests log into a real staging account and act on its real conversations/events (see `E2E_TEST_ACCOUNT.md`, gitignored) — keep scenarios self-cleaning (e.g. delete/undo what they created) and use `org.junit.Assume` to skip gracefully when a precondition (e.g. "a message from someone else exists") isn't guaranteed.
- Message/post bubbles are rendered in Compose inside a plain `ComposeView` (no `ComposeTestRule`), so Espresso can't reach their content via `withText`/`withContentDescription` — targeting relies on fractional-position `GeneralClickAction`/`GeneralLocation` hacks (see `ConversationScenarioTest.longClickOnMessageBubble()`). Tune these against a real device/emulator; a value that merely compiles is not verified to hit the right element.

## Environment / build types

| Variant | API base | App ID suffix | Deep link scheme |
|---|---|---|---|
| `entourageDebug` (default) | `api-preprod.entourage.social` | `.debug` | `entourage-debug://` |
| `entouragePreprod` | `api-preprod.entourage.social` | `.preprod` | `entourage-staging://` |
| `entourageRelease` | `api.entourage.social` | _(none)_ | `entourage://` |

`BuildConfig.PEDAGO_CREATE_EVENT_ID`, `PEDAGO_CREATE_GROUP_ID`, etc. differ between `debug`/`preprod` and `release` — never hardcode those IDs.

## What to avoid

- Do not scan or list `build/`, `app/build/`, `.gradle/`, `.idea/` — they are large and generated.
- Do not run `git rev-list`, `git rev-parse`, or `adb` inside Gradle scripts.
- Do not use `CardView` with `strokeColor`/`strokeWidth` — those attributes belong to `MaterialCardView`. For a bordered card without elevation use a `LinearLayout` with a `<shape>` drawable background instead.
