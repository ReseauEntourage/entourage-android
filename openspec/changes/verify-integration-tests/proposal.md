## Why

The project has a significant number of integration tests in `app/src/androidTest`, but their current health and reliability are unknown. To ensure the stability of the application and prevent regressions, all integration tests need to be verified, flaky tests identified, and the suite confirmed to be passing in the current environment.

## What Changes

- Audit all test files in `app/src/androidTest`.
- Execute the integration test suite to identify failures.
- Fix failing or flaky tests to restore a green build.
- Ensure the test infrastructure (Dagger/Hilt setups, API mocks) is correctly configured for the current project state.

## Capabilities

### New Capabilities
- None (skip_specs: true)

### Modified Capabilities
- None (skip_specs: true)

## Impact

- `app/src/androidTest/**`: Test code will be modified to fix failures or improve reliability.
- CI/CD pipeline (if applicable): More reliable feedback on PRs.
- No impact on production code behavior.
