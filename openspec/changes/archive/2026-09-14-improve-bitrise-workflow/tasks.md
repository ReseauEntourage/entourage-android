## 1. Update Bitrise PR Check Workflow

- [x] 1.1 Add `is_skippable: true` to the `slack` and `post-jira-comment-with-build-details` steps in the `pr_check` workflow in `bitrise.yml` and verify YAML syntax is valid.

## 2. Pin and Modernize Bitrise Step Versions

- [x] 2.1 Update step definitions across all workflows in `bitrise.yml` to specify explicit or major version tags (e.g. `slack@3`, `save-gradle-cache@1`, `restore-gradle-cache@1`, `deploy-to-bitrise-io@2`, `google-play-deploy@5`, `git-tag@1`, `set-java-version@1`) and verify step references match Bitrise step library conventions.

## 3. Validate Configuration

- [x] 3.1 Inspect `bitrise.yml` structure and run YAML validation to confirm no syntax errors exist.
