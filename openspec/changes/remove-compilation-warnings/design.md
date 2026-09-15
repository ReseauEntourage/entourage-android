## Context

See `proposal.md` — Why. Baseline: 408 Kotlin warnings, 110 files, `app` main source set only.

Constraints that shape the approach:

- `minSdk = 23`, `compileSdk = targetSdk = 37`. Every deprecated platform API in the inventory still *functions* at all supported levels — deprecated is not removed. So "fix the warning" and "change runtime behaviour" are separable choices, and this design makes that choice explicitly per API family rather than uniformly.
- Dependency versions all already carry the modern replacements, so no dependency bumps are needed: `androidx.core 1.19.0` (`IntentCompat`, `BundleCompat`, `ParcelCompat` — all added in 1.10.0), `places 4.3.1` (`DISPLAY_NAME` / `FORMATTED_ADDRESS` / `LOCATION`), `recyclerview 1.4.0` (`bindingAdapterPosition`), `play-services-location 21.4.0` (`LocationRequest.Builder`), `appcompat 1.7.1` / `fragment 1.8.9` (Activity Result API).
- No DI framework and no `allWarningsAsErrors` (excluded by the user). There is therefore no mechanism preventing regression to a non-zero count; the design compensates by concentrating unavoidable suppressions in a small number of named, commented locations so a reviewer can audit them at a glance.
- `tools/Extensions.kt:79-84` already establishes the house pattern for SDK-gated compat: `if (Build.VERSION.SDK_INT >= …) new else @Suppress("DEPRECATION") old`.

The warning distribution is extremely uneven, and that drives the plan more than the raw total: 4 files hold 90 warnings (`deeplinks/UniversalLinkManager.kt` 27, `tools/view/WebViewFragment.kt` 25, `guide/GDSSearchFragment.kt` 19, `home/HomeFragment.kt` 19), and 3 API families hold 127 (`overridePendingTransition` 56, `startActivityForResult` 43, `bundleOf` 28). Fixing by *API family* rather than by file is what makes this tractable.

## Goals / Non-Goals

**Goals:**
- Zero warnings from `./gradlew :app:compileEntourageDebugKotlin --rerun-tasks --no-build-cache`.
- Every suppression that remains is single-site, commented with the reason and the API level that would allow its removal — auditable, not scattered.
- Tier 4 (`startActivityForResult`) is independently revertible from Tiers 1–3, because it is the only tier that can regress user-visible flows.

**Non-Goals:**
- Not reproducing Android 14's new open/close transition model. See the `overridePendingTransition` decision — this design deliberately keeps current animation behaviour.
- Not converting the codebase to ViewBinding-only or fixing the underlying nullability modelling that produced 67 unnecessary safe calls; those are removed where the compiler proves them redundant, nothing more.
- No dependency upgrades, no build-config guardrail, no test/androidTest changes.

## Decisions

### D1 — Fix by API family, in tiers, not file-by-file

Warnings cluster by API, and a per-family fix is written once and applied mechanically. Per-file would mean re-deriving the same `getSerializableExtra` migration 8 times with 8 chances to differ.

Tier order is chosen so risk increases monotonically and each tier ends at a compiling, warning-reduced state: Tier 1 (defects) → Tier 2 (mechanical) → Tier 3 (compat helpers) → Tier 4 (Activity Result). Tier 1 goes first deliberately: those warnings mark real bugs, and doing them while the output is still noisy is easier than after 300 unrelated edits have churned the diff.

*Alternative considered:* one commit per file, easier to review in isolation. Rejected — it multiplies the number of times each migration decision gets made, and the review benefit is illusory when 21 files receive the identical three-line change.

### D2 — `overridePendingTransition` (56 sites): one helper that suppresses internally, rather than migrating to `overrideActivityTransition`

This is the most consequential decision, and it is deliberately *not* a mechanical swap.

`overridePendingTransition(enter, exit)` is called on the **originating** activity immediately after `startActivity`. Its API 34 replacement `overrideActivityTransition(OVERRIDE_TRANSITION_OPEN|CLOSE, enter, exit)` uses a different model: it is called on the activity **whose own** transition is being overridden, ahead of time (typically in the target activity's `onCreate` for OPEN, and before `finish()` for CLOSE). A drop-in call on the originating activity after `startActivity` does not reproduce the old effect.

Faithfully migrating 56 sites across 17 files therefore means deciding, per site, whether it is an open or a close, and relocating the call into a different class — an animation-behaviour refactor across most of the navigation surface, with visual regressions that unit tests cannot catch.

Decision: add one helper and suppress inside it.

```kotlin
// tools/utils/CompatExtensions.kt
/**
 * Kept on the deprecated API on purpose.
 * The API 34 replacement (overrideActivityTransition) inverts the call site:
 * it must be invoked on the activity being opened/closed, not on the caller.
 * Migrating all sites is an animation refactor tracked separately.
 * Safe: overridePendingTransition is deprecated, not removed, and still
 * functions on API 34+.
 */
@Suppress("DEPRECATION")
fun Activity.overrideTransitionCompat(@AnimRes enter: Int, @AnimRes exit: Int) =
    overridePendingTransition(enter, exit)
```

56 warnings collapse to zero, animation behaviour is bit-for-bit unchanged, and the future migration has exactly one entry point. This is the honest reading of Option B's "helper extensions so those are fixed centrally rather than 38× inline" — the point of centralising is to make the remaining debt a single reviewable line.

*Alternative considered:* full `overrideActivityTransition` migration. Rejected as disproportionate — it is a larger and riskier change than the Tier 4 Activity Result work, for a warning class that carries no correctness risk. Should be its own change with design-side sign-off on the animations.

### D3 — `getSerializableExtra` / `getSerializable` / `getParcelable` / `getParcelableExtra` / `readParcelable` / `readList` / `readSerializable` (~30 sites): migrate genuinely, via AndroidX `*Compat`

Unlike D2 these have true drop-in, type-safe replacements with identical semantics, already on the classpath, and they fix a real defect class: the untyped overloads are the ones that break on API 33+ and silently return `null` for the wrong type.

```kotlin
IntentCompat.getSerializableExtra(intent, KEY, Foo::class.java)
BundleCompat.getParcelable(bundle, KEY, Foo::class.java)
ParcelCompat.readParcelable(parcel, loader, Foo::class.java)
ParcelCompat.readList(parcel, loader, list, Foo::class.java)
```

Thin reified wrappers (`inline fun <reified T> Intent.serializableExtra(key: String): T?`) keep call sites readable. No suppression needed anywhere in this family.

This also resolves the related `groups/GroupModel.kt:37` incompatible-upper-bounds warning, which is a *symptom* of the untyped `readParcelable` — the compiler cannot reconcile `Parcelable!` with `Translation?`. Passing the class explicitly removes the cause rather than silencing the warning.

### D4 — `bundleOf` (28 sites): inline platform `Bundle`, no helper

`androidx.core.os.bundleOf` was deprecated for being type-unsafe (`Pair<String, Any?>`). Any helper wrapping it would reintroduce exactly the unsafety the deprecation exists to flag, so the fix is `Bundle().apply { putInt(…); putString(…) }` at each site. Verbose but correct, and the compiler now type-checks each put.

### D5 — Places SDK (32 sites, 6 files): migrate; nullability changes are real and must be handled

`Place.Field.NAME` → `DISPLAY_NAME`, `ADDRESS` → `FORMATTED_ADDRESS`, `LAT_LNG` → `LOCATION`; accessors `place.name`/`place.address`/`place.latLng` → `place.displayName`/`place.formattedAddress`/`place.location`.

Consequential detail: the new accessors are properly `@Nullable`, where the old ones were platform types. This is why `events/EventFiltersActivity.kt:243` and `user/edit/place/UserActionPlaceFragment.kt:109` currently emit *"Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver"* — the migration forces the null handling the old API let the code skip. Each of the 6 files needs its null branch decided, not `!!`-ed. Grouped with Tier 1 rather than Tier 3 for that reason.

`Geocoder.getFromLocation` / `getFromLocationName` (4 sites) is a separate matter: the API 33 replacement is an async callback, so migrating changes the call from synchronous to asynchronous and restructures the caller. These get a documented single-site `@Suppress("DEPRECATION")` behind a small `geocodeCompat` helper, on the same reasoning as D2.

### D6 — 67 unnecessary safe calls: delete the `?.`, do not restructure

62 of the 67 sit in 5 files (`WebViewFragment` 25, `GDSSearchFragment` 19, `GuideHubFragment` 8, `ShareMessageFragment` 5, plus scattered) and are all the same artefact: ViewBinding fields are non-null, but the code still guards them with `?.` from the pre-binding `findViewById` era. The compiler has *proved* the receiver non-null, so dropping `?.` cannot change behaviour.

Constraint for the implementer: this is a delete-the-operator change only. Do not "tidy" surrounding binding access, rename, or extract while in these files — a 25-warning file invites scope creep, and mixing mechanical and judgement edits in one diff is what makes this class of cleanup unreviewable.

### D7 — Constant conditions and always-left Elvis (13 sites): read each, never blanket-delete

`Condition is always 'true'/'false'` (5) and `Elvis operator always returns the left operand` (8) are the only categories where the mechanical fix can *hide* a bug instead of fixing it. A constant condition is either dead code (delete the branch) or an inverted/wrong check (fix the condition) — and deleting the branch of a wrong check silently cements the bug.

`groups/details/members/MembersFragment.kt:176-177` and `:205-206` are the clearest example: a `Condition is always 'true'` immediately followed by an `Unnecessary non-null assertion (!!) on a non-null receiver of type 'Int'` — a null check on something that was never nullable. Each of the 13 gets read with its surrounding function, and the task list requires stating which of the two cases it was.

The 5 duplicate `when` branches in `api/model/Events.kt:190-196` are confirmed the benign case — `"animaux"`, `"cuisine"`, `"jeux"`, `"nature"`, `"sport"` each appear twice with byte-identical bodies, so the second is unreachable and deleting it is behaviour-preserving. Note that the *neighbouring* lines 189/193/197 (`"activités manuelles"`, `"art & culture"`, `"rencontres nomades"`) are distinct keys mapping to the same string and must be kept — an over-eager dedupe here would break accented and multi-word tag lookups.

### D8 — `startActivityForResult` (43 sites, 21 files): genuine Activity Result migration, sequenced last and split per feature area

No suppression: this is the one family where the deprecated API has a real behavioural downside (`onActivityResult` request-code dispatch is fragile and already tangled here) and a clean, well-supported replacement.

Mechanics per site: `registerForActivityResult(StartActivityForResult()) { result -> … }` hoisted to a field initialiser (registration must happen before `STARTED`, so it cannot go in a click listener), the matching `onActivityResult` request-code branch removed, and the launcher invoked with the intent.

Split by feature area — actions, events, groups, discussions, deeplinks, home/profile — so each is one reviewable commit with its own QA checklist. `deeplinks/UniversalLinkManager.kt` is called out as the hard case: it is not a `Fragment`/`ComponentActivity`, it holds 4 of the sites, and it therefore needs the launcher owned by the hosting activity and passed in, not registered locally.

## Risks / Trade-offs

- **A compat helper is written wrongly and the bug is silent on every screen** → The parcel/serializable helpers are the dangerous ones: a wrong class argument returns `null` rather than throwing. Mitigation: migrate one call site per family first, run the affected screen, then apply the pattern; helpers get the class parameter from the reified type so it cannot drift from the cast target.
- **D2 leaves 56 sites on a deprecated API** → Accepted, explicitly. Trade-off is stated above: zero warnings and zero animation risk now, versus a large visual refactor. The single suppression site with a comment naming the blocker is the mitigation; it is discoverable by grep, unlike 56 inline suppressions.
- **Tier 4 regresses a result-returning flow silently** → Highest real risk in the change. `onActivityResult` removal and launcher registration must land together; a half-migrated activity compiles fine and simply stops receiving results. Mitigation: per-area commits, an explicit manual QA checklist per area (photo pick/crop, event create, group settings, member management, deep-link entry, profile edit), and the ability to revert Tier 4 alone.
- **Tier 1 changes observable behaviour** → Intended, but it means "no visible change" is *not* the acceptance criterion for this tier. Each Tier 1 item records what behaviour changed and why the previous behaviour was wrong.
- **Warning count regresses immediately after the change** → Real, and unmitigated by design: `allWarningsAsErrors` was excluded. Noted here so the choice is visible rather than implicit; the baseline command in the proposal lets a reviewer re-measure in one step.
- **Volume of mechanical edits buries a judgement call in review** → Mitigation: Tier 2 and Tier 3 commits never mix with Tier 1 or Tier 4, and D6 forbids incidental tidying inside high-count files.

## Migration Plan

Per-tier, each ending in a compiling tree with a lower warning count:

1. Re-measure baseline (`--rerun-tasks --no-build-cache`) and record the count.
2. Tier 1 — defects and Places nullability. Verify: compile, unit tests, manual check of the touched screens (event create step two, members list, event filters, place pickers).
3. Tier 2 — mechanical cleanups. Verify: compile + unit tests only; no behaviour to check by construction.
4. Tier 3 — add `tools/utils/CompatExtensions.kt`, then apply per family. Verify per family: compile, plus one screen exercised for the parcel/serializable and transition families.
5. Tier 4 — Activity Result, one commit per feature area, each with its QA checklist.
6. Final: confirm 0 warnings, `:app:testEntourageDebugUnitTest` green, `assembleEntourageRelease` builds (release differs from debug in `BuildConfig` IDs and R8, so a debug-only check is insufficient).

Rollback: Tiers are independent commits in increasing-risk order, so reverting Tier 4 (or a single feature area within it) restores the previous result-handling behaviour without giving up Tiers 1–3.

## Open Questions

- Whether the ~56 aapt string-format warnings (out of scope per the proposal) should become a follow-up change. Deferrable: it touches `values*/strings.xml` only and does not affect any decision above.
- Whether a follow-up change should do the real `overrideActivityTransition` migration described in D2. Deferrable for the same reason — D2's helper is the seam it would be built on either way.
