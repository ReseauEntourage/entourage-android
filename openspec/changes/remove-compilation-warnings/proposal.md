## Why

`./gradlew :app:compileEntourageDebugKotlin` currently emits **408 Kotlin compiler warnings across 110 files** in the `app` main source set. At that volume the compiler output is unreadable, so a genuinely new warning introduced by a PR is invisible — the signal is buried in known noise. Several of the existing warnings are also pointing at real defects (unreachable `when` branches, conditions that are constant, nullability that the compiler cannot honour), which have been sitting unnoticed precisely because nobody reads the wall of output.

Reducing the count to zero restores the compiler as a usable review signal and fixes the latent defects it has been flagging all along.

## What Changes

The work is tiered so that mechanical, zero-risk edits are separated from behavioural refactors that need QA. Nothing here changes intended user-facing behaviour; where a fix would alter observable behaviour, that is because the current code is defective, and it is called out explicitly.

**Tier 1 — Genuine defects and dead code (behaviour-affecting, small, high value)**
- Delete 5 unreachable duplicate `when` branches in `api/model/Events.kt` (`"animaux"`, `"cuisine"`, `"jeux"`, `"nature"`, `"sport"` are each listed twice with identical bodies).
- Resolve 5 `Condition is always 'true'/'false'` warnings — each read individually, since a constant condition is either dead code or an inverted check.
- Fix 2 `Java type mismatch: inferred type is 'Date?', but 'Date' was expected` in `events/create/CreateEventStepTwoFragment.kt` — an unguarded platform-type NPE risk.
- Fix 2 `Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver` in `events/EventFiltersActivity.kt` and `user/edit/place/UserActionPlaceFragment.kt`.
- Fix 1 unchecked cast (`ListAdapter!` → `ArrayAdapter<String>`) and 1 incompatible-upper-bounds type argument in `groups/GroupModel.kt`.

**Tier 2 — Mechanical cleanups (strictly behaviour-preserving)**
- Remove 67 unnecessary safe calls, 17 needless casts, 8 always-left Elvis operators, 3 redundant conversion calls, 2 redundant `else` branches, 2 unnecessary `!!`.
- Concentrated in `tools/view/WebViewFragment.kt` (25), `guide/GDSSearchFragment.kt` (19), `guide/GuideHubFragment.kt` (8) — these are ViewBinding migrations left half-done, where bindings are non-null but still accessed with `?.`.

**Tier 3 — Centralised compat helpers (replaces ~120 scattered call sites with a handful of helpers)**
- `overridePendingTransition` (56 sites, 17 files) → one `Activity.overrideTransitionCompat()` helper wrapping the API 34 `overrideActivityTransition` split.
- `bundleOf` (28 sites, 21 files) → platform `Bundle` construction.
- `getColor` (16 sites, 5 files) → `ContextCompat.getColor`.
- `getSerializableExtra` / `getSerializable` / `getParcelable` / `getParcelableExtra` / `readParcelable` / `readList` / `readSerializable` (~30 sites) → typed `IntentCompat` / `BundleCompat` / `ParcelCompat` helpers replacing the SDK-33 branch.
- `adapterPosition` (7 sites) → `bindingAdapterPosition`.
- Remaining long tail (~30 one-off deprecations: `setColorFilter`, `Locale.locale`, `toHtml`, `systemUiVisibility`, `LocationRequest` builders, `IntentService`, `defaultDisplay`, `getMetrics`, `toggleSoftInput`, `setHasOptionsMenu`, …) fixed individually or, where no replacement exists at `minSdk = 23`, given a narrowly scoped `@Suppress("DEPRECATION")` with a comment.

**Tier 4 — Activity Result API migration (behavioural refactor, gated on QA)**
- `startActivityForResult` (43 sites, 21 files) → Activity Result API. This is the only group that restructures control flow: it removes `onActivityResult` overrides and hoists launchers to field initialisers. Sequenced last, split per feature area, each with an explicit QA checklist, so it can be reviewed and reverted independently of Tiers 1–3.

## Capabilities

### New Capabilities

None. This is a code-health change: it fixes defects and removes deprecated API usage without introducing or altering any product capability. The repository has no `openspec/specs/` capability specs, and inventing one for "the build has no warnings" would be a requirement about the toolchain, not about system behaviour.

### Modified Capabilities

None. No spec-level behaviour changes.

Accordingly this change sets `skip_specs: true` in its `.openspec.yaml`.

## Impact

**Scope — in**
- `app/src/main/java/**` only. 408 warnings, 110 files.
- New/extended helpers in `tools/Extensions.kt` (which already establishes the `Build.VERSION.SDK_INT` + `@Suppress("DEPRECATION")` pattern at line 82) and `tools/utils/`.

**Scope — out (agreed explicitly)**
- `allWarningsAsErrors` is **not** enabled. No build-configuration guardrail is added; keeping the count at zero stays a review-time concern.
- The two `gradle.properties` AGP option-deprecation warnings (`android.enableJetifier=true`, `android.dependency.excludeLibraryComponentsFromConstraints=true`) are out of scope.
- `src/test/` and `src/androidTest/` are out of scope.
- Also observed but out of scope: ~56 `mergeEntourageDebugResources` warnings of the form *"Multiple substitutions specified in non-positional format of string resource"* across `values*/strings.xml` (`atKm`, `date_recurrence_event`, `start_and_end_time_event`, `share_request_group`, `popup_event_confirm_title`, `info_share_poi_sms`, `members_location`). These are aapt resource warnings, not Kotlin compilation warnings. Worth a follow-up change — they indicate strings needing `formatted="false"` or positional `%1$s` arguments, and they can mask real crashes on RTL/localised builds.

**Risk**
- Tiers 1–2 are compile-verifiable and carry no regression risk beyond the intended defect fixes.
- Tier 3 changes behaviour only if a helper is written wrongly; the transition and parcel helpers are the ones to review closely, since a mistake there is silent and reproduces on every screen.
- Tier 4 carries genuine regression risk on result-returning flows: photo pick/crop, event creation, group settings, member management, deep-link entry, profile edit. Requires manual QA per flow.

**Verification**
- Baseline is recorded: 408 warnings. `./gradlew :app:compileEntourageDebugKotlin --rerun-tasks --no-build-cache` is the measurement command (a plain build reports nothing when the task is up to date, and `--rerun-tasks` alone hit a build-cache packing failure).
- Target: 0 warnings from that command, plus `:app:testEntourageDebugUnitTest` still green and `assembleEntourageRelease` still building.
