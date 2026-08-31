## 1. Audit and Baseline Execution

- [x] 1.1 List all test classes in `app/src/androidTest` and verify they are all correctly discovered by the build system.
- [ ] 1.2 Execute a baseline run of all tests from the IDE and verify the results in the Run window.

## 2. Infrastructure Verification

- [x] 2.1 Verify Dagger/Hilt test configuration (if used) and ensure all external dependencies are correctly mocked.
- [x] 2.2 Verify that system animations are disabled on the target test device to prevent Espresso synchronization issues.

## 3. Stabilization of Unchecked Tests

- [ ] 3.1 Audit `unchecked/OnboardingTest.kt` and verify it either passes or document its failures against the current UI state.
- [ ] 3.2 Audit `unchecked/HomeExpertTest.kt` and verify it either passes or document its failures against the current UI state.
- [ ] 3.3 Systematically review and fix (or deprecate) all other files in the `unchecked` package.

## 4. Final Verification

- [/] 4.1 Fix detected failures in the `beforeLogin` and `afterLogin` packages and verify they pass consistently over multiple runs.
- [ ] 4.2 Execute the full integration test suite and verify that the build is "green" (all active tests passing).
