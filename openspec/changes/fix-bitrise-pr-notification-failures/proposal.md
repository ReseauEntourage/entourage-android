## Why

The `pr_check` Bitrise workflow reports pull requests as failed even when the Gradle compile step succeeds, because the `slack` notification step errors out (`All of Integration ID, API Token and WebhookURL are empty`). `SLACK__BOT_API_TOKEN` (and, most likely, `JIRA_API_TOKEN`) are not currently exposed to pull-request-triggered builds on Bitrise, so the step has no credentials to use. This misleads reviewers into thinking a PR's build/compile is broken when it isn't. The Bitrise secret exposure will be fixed separately in the Bitrise dashboard (out of repo scope), but the workflow itself should not let a notification failure fail the whole PR check, now or if this happens again in the future.

## What Changes

- In the `pr_check` workflow of `bitrise.yml`, mark the `slack` step as `is_skippable: true` so a Slack notification failure (missing/invalid credentials, Slack API issues, etc.) no longer fails the overall PR check build status.
- Mark the `post-jira-comment-with-build-details` step in `pr_check` as `is_skippable: true` for the same reason — it depends on `JIRA_API_TOKEN`, which is exposed the same way as the Slack token and can fail independently of the actual code build.
- No change to `dev_entourage` or `prod_entourage` workflows — those run on `push`/manual triggers where secrets are already exposed, and a failed notification there should keep surfacing as a build issue.
- Out of repo scope (tracked separately, done by the user in the Bitrise dashboard): expose `SLACK__BOT_API_TOKEN` and `JIRA_API_TOKEN` to pull-request-triggered builds so the notifications actually post for PRs, not just stop blocking them.

## Capabilities

### New Capabilities
- `bitrise-pr-check`: Defines the required behavior of the `pr_check` Bitrise workflow — specifically, that the reported PR check status must reflect the Gradle build/compile outcome, and must not be failed by unrelated notification step errors (Slack, Jira).

### Modified Capabilities
(none — this is the first spec for this workflow)

## Impact

- **Affected file**: `bitrise.yml` (`pr_check` workflow only — `slack` and `post-jira-comment-with-build-details` steps).
- **Affected systems**: Bitrise CI pull-request status checks reported to GitHub PRs on `ReseauEntourage/entourage-android`.
- **Not changed by this repo change**: Bitrise workspace secret exposure settings for `SLACK__BOT_API_TOKEN` / `JIRA_API_TOKEN` on PR-triggered builds — the user will configure this directly in the Bitrise dashboard.
