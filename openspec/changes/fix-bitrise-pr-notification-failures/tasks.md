## 1. bitrise.yml changes

- [ ] 1.1 In the `pr_check` workflow's `slack` step, add `is_skippable: true` and verify the step block still parses as valid YAML (e.g. `python -c "import yaml,sys; yaml.safe_load(open('bitrise.yml'))"` or equivalent)
- [ ] 1.2 In the `pr_check` workflow's `post-jira-comment-with-build-details` step, add `is_skippable: true` and verify the step block still parses as valid YAML
- [ ] 1.3 Confirm the `dev_entourage` and `prod_entourage` workflows' `slack`/Jira-related steps are unchanged (diff `bitrise.yml` against the base branch and check only the two `pr_check` steps changed)

## 2. Verification

- [ ] 2.1 Trigger (or ask a maintainer to trigger) a real pull request build on Bitrise and confirm: if the `slack` or `post-jira-comment-with-build-details` step fails, the `pr_check` workflow and the GitHub PR status check still report success as long as `gradle-runner` succeeded
- [ ] 2.2 Confirm a PR with an intentionally broken `compileEntouragePreprodSources` task still reports the `pr_check` / GitHub status check as failed
- [ ] 2.3 Note in the PR description that `SLACK__BOT_API_TOKEN` and `JIRA_API_TOKEN` exposure-to-PR-builds is a separate, out-of-repo Bitrise dashboard change the user will apply directly
