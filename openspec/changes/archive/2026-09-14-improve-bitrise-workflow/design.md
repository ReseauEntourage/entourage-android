## Context

The Bitrise configuration (`bitrise.yml`) runs on Bitrise Linux stacks (`ubuntu-noble-24.04-bitrise-2025-android` on `g2.linux.large`). It triggers `pr_check` on pull requests, `dev_entourage` on push to `develop`, and `prod_entourage` manually or on release tags.

See `proposal.md` for problem background and motivation.

## Goals / Non-Goals

**Goals:**
- Add `is_skippable: true` to notification steps (`slack`, `post-jira-comment-with-build-details`) in `pr_check` to prevent missing credentials from blocking PR status checks.
- Pin or update step definitions to major versions in Bitrise Step Library (e.g., `git-clone@8`, `slack@3`, `save-gradle-cache@1`, `restore-gradle-cache@1`, `google-play-deploy@5`, `deploy-to-bitrise-io@2`, `set-java-version@1`).
- Ensure consistent environment configuration across workflows (Java 21, Gradle stack options).

**Non-Goals:**
- Changing Bitrise workspace secrets in the Bitrise Web Dashboard (managed outside repo code).
- Modifying project application code or Gradle build logic itself.

## Decisions

### Decision 1: Mark notification steps as `is_skippable: true` in `pr_check` only

**Rationale**: `pr_check` is triggered automatically by GitHub pull requests, where secrets like `SLACK__BOT_API_TOKEN` and `JIRA_API_TOKEN` might not be exposed or valid for forks/external PRs. Making notification steps skippable ensures that code compilation status is what determines PR check pass/fail.

**Alternatives Considered**:
- *Wrapping notifications in conditional bash scripts*: Adds unneeded maintenance overhead compared to native Bitrise `is_skippable: true`.
- *Making notification steps skippable on `dev_entourage` and `prod_entourage`*: Rejected because dev/prod builds run with full secrets and should alert maintainers if notifications fail.

### Decision 2: Pin step versions in Bitrise Step Library format

**Rationale**: Unversioned steps in `bitrise.yml` resolve to the latest version at build execution time, which can introduce unexpected breaking changes when Bitrise updates steps. Pinning to major versions (e.g. `slack@3`, `deploy-to-bitrise-io@2`) ensures stability while allowing minor bug fixes.

## Risks / Trade-offs

- [Risk] Missing notifications on PR builds if tokens are invalid or missing. → Mitigation: Expected behavior; Slack notifications for PR builds are optional feedback, while PR build pass/fail status remains strictly tied to build success.
- [Risk] Outdated step version pinning over time. → Mitigation: Pinning to major versions allows non-breaking step updates while preventing workflow breakage.
