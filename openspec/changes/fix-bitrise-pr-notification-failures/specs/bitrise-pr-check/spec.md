## Purpose

Defines how the `pr_check` Bitrise workflow reports pull-request build status to GitHub, so that reviewers can trust a red/green check as reflecting the actual Gradle compile outcome rather than an unrelated notification failure.

## ADDED Requirements

### Requirement: PR check status reflects only the build outcome
The `pr_check` Bitrise workflow's overall build status (as reported to the GitHub pull request check) SHALL reflect only the outcome of the Gradle compile step. Failure of a post-build notification step (Slack, Jira comment) SHALL NOT by itself cause the overall `pr_check` build to be reported as failed.

#### Scenario: Notification step fails due to missing or invalid credentials
- **WHEN** the `slack` or `post-jira-comment-with-build-details` step in `pr_check` fails (for example, because its API token is empty or invalid for a pull-request-triggered build)
- **THEN** the `pr_check` workflow's overall status reported to the GitHub pull request SHALL still be success, provided the Gradle compile step succeeded

#### Scenario: Gradle compile step fails
- **WHEN** the `gradle-runner` step in `pr_check` fails to compile
- **THEN** the `pr_check` workflow's overall status reported to the GitHub pull request SHALL be failure, regardless of whether the notification steps would have succeeded or failed

#### Scenario: Notification steps succeed
- **WHEN** the Gradle compile step succeeds and the `slack` and `post-jira-comment-with-build-details` steps also succeed
- **THEN** the `pr_check` workflow SHALL report success, and the Slack message and Jira comment SHALL be posted as before
