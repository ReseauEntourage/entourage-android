## Purpose

Defines CI/CD workflow behavior on Bitrise for pull requests, branch builds, and production releases, ensuring builds are reliable, fast, and resilient to third-party integration failures.

## ADDED Requirements

### Requirement: Non-critical steps in pull request checks must not fail the overall build

The `pr_check` Bitrise workflow SHALL execute non-critical integration steps (such as Slack notifications and Jira issue commenting) in a skippable mode (`is_skippable: true`), ensuring that failures in credential availability or API availability for these steps do not mark the pull request build check as failed when compilation succeeds.

#### Scenario: Pull request build succeeds when Slack or Jira notifications fail
- **WHEN** a pull request triggers the `pr_check` workflow and Gradle compilation succeeds, but Slack or Jira steps encounter missing tokens or API errors
- **THEN** the Bitrise `pr_check` workflow completes successfully and reports a green status check on the pull request

### Requirement: Bitrise workflow steps must use pinned or major-versioned definitions

All workflow steps in `bitrise.yml` SHALL use explicit version tags (or modern major-versioned step identifiers) to prevent build breaks from unpinned floating step updates.

#### Scenario: Bitrise workflow execution with pinned step versions
- **WHEN** any Bitrise workflow (`pr_check`, `dev_entourage`, `prod_entourage`, `prepare_workflow`) is executed
- **THEN** Bitrise resolves and executes explicitly specified step versions without falling back to unpinned dynamic step revisions

### Requirement: Gradle build caching must be enabled across Bitrise workflows

Bitrise workflows executing Gradle tasks SHALL restore and save Gradle dependency and build caches using key fallback strategies to minimize build execution times.

#### Scenario: Subsequent build reuses cached dependencies
- **WHEN** a workflow executes after a previous build has cached Gradle dependencies
- **THEN** `restore-gradle-cache` restores cached files and reduces overall build execution time
