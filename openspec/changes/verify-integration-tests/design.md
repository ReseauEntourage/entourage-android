## Context

The application uses Espresso and AndroidJUnitRunner for integration testing. Tests are located in `app/src/androidTest` and cover various flows (Onboarding, Login, Home, etc.). Many tests are categorized into sub-packages like `beforeLogin`, `afterLogin`, and `unchecked`. The `unchecked` package contains a large volume of tests (e.g., `OnboardingTest.kt`, `HomeExpertTest.kt`) that may be outdated or flaky.

## Goals / Non-Goals

**Goals:**
- Audit the `app/src/androidTest` directory to identify active, flaky, and broken tests.
- Successfully execute the integration test suite on a connected device/emulator.
- Fix configuration issues in test infrastructure (Dagger/Hilt, API mocking).
- Restore the `unchecked` tests to a passing state or formally deprecate/delete them if they are no longer relevant.

**Non-Goals:**
- Writing new integration tests for features not currently covered.
- Performance optimization of the test execution pipeline.
- Major architectural changes to the testing framework.

## Decisions

- **Decision 1: Baseline Execution via IDE**
  - **Rationale:** Run tests directly from the Android Studio IDE to allow for faster iteration and easier debugging of individual test failures.
  - **Alternatives:** Using `./gradlew connectedDebugAndroidTest` for a cleaner but slower build system baseline.
- **Decision 2: Systematic Audit of `unchecked` Tests**
  - **Rationale:** These tests likely provide high value but have been "hidden" due to instability. Each will be reviewed for relevance to the current UI.
  - **Alternatives:** Ignoring them, but this leaves a large portion of the app unverified.

## Risks / Trade-offs

- **[Risk]** Test instability due to asynchronous operations (animations, network calls). → **Mitigation:** Ensure proper use of `IdlingResource` and `Disable Animations` on the test device.
- **[Risk]** Inconsistent test data/state. → **Mitigation:** Verify that each test properly resets its state (e.g., clearing preferences, database) before running.
