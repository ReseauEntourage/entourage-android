## Why

The Bitrise CI/CD pipeline (`bitrise.yml`) handles pull request checks, dev builds, and production releases. Currently, steps in `pr_check` lack `is_skippable: true` for non-critical notification steps (such as Slack and Jira integration), causing builds to fail when API tokens or secrets are missing. Additionally, several steps use unpinned step versions, cache step configurations are using older mechanisms, and build environment options can be optimized for build speed and reliability. Modernizing `bitrise.yml` ensures robust CI execution, prevents non-critical step failures from blocking PR approvals, and improves build cache efficiency.

## What Changes

- **Fault Tolerance for Notifications**: Mark non-critical post-build steps (`slack`, `post-jira-comment-with-build-details`) in `pr_check` with `is_skippable: true` so credential issues do not fail the PR build status.
- **Pin & Update Bitrise Step Versions**: Update step references to explicit versions (or latest stable majors) to avoid unexpected breaking changes from floating step definitions.
- **Modernize Caching**: Ensure Gradle caching steps (`restore-gradle-cache`, `save-gradle-cache`) are properly configured with key fallback paths and optimal cache keys.
- **Workflow Environment & Stack Consistency**: Clean up step inputs across `pr_check`, `dev_entourage`, and `prod_entourage` to use consistent Java 21 toolchains, proper Android SDK dependency installs, and clean filter options.

## Capabilities

### New Capabilities
- `bitrise-workflow`: Defines requirements for Bitrise CI/CD workflows including PR validation, development builds, production releases, caching strategy, and fault-tolerant notifications.

### Modified Capabilities
(none)

## Impact

- **Affected files**: `bitrise.yml`
- **Affected systems**: Bitrise CI/CD build pipelines for pull requests, development builds (`dev_entourage`), and production releases (`prod_entourage`).
