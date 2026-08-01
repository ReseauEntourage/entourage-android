---
name: ent:changelog-tag
description: Generate a changelog entry for a git tag in this repo. Analyses commits and code structure changes between the given tag and the previous one, then prepends the entry to CHANGELOG.md.
argument-hint: [tag-name]
allowed-tools: Bash, Read, Edit, Grep, Glob
---

Generate a changelog entry for the git tag `$ARGUMENTS` in this repository (Android app) and prepend it to `CHANGELOG.md`.

## Steps

### 1. Resolve tags

- If `$ARGUMENTS` is empty, use the most recent tag: `git tag --list 'Version_*' | sort -V | tail -1`
- The target tag is `TARGET_TAG=$ARGUMENTS` (or the resolved latest tag).
- Find the previous tag: the tag that immediately precedes `TARGET_TAG` when sorted with `sort -V` (within the `Version_*` set).
- Get the tag date: `git show <tag> --format="%ai" -s`

### 2. Collect commits

Run:
```
git log <PREV_TAG>..<TARGET_TAG> --pretty=format:"- %s (%h)"
```
Filter out bare merge commits (lines matching `^- Merge remote-tracking branch` or `^- Merge branch`) unless they carry meaningful information.

### 3. Collect file-level diff stats

Run:
```
git diff <PREV_TAG>..<TARGET_TAG> --stat
```
Group the changed files by service/directory to understand which parts of the repo were affected.

### 4. Analyse structural changes

For each service or area that shows significant changes, inspect the actual diff to understand *what* changed structurally:

- **New files added** — what are they, what do they do?
- **Files deleted** — what feature or module was removed?
- **Large modifications** — what was refactored or extended?

Use targeted diff reads like:
```
git diff <PREV_TAG>..<TARGET_TAG> -- <path> | head -120
```
Focus on:
- New Lambda functions or handlers
- Deleted use-cases or modules
- New shared libraries or utilities
- Schema/migration changes
- Package manager changes (lockfiles, workspace files)
- Tooling scripts added or removed
- Security fixes

Do NOT reproduce the full diff — summarise the intent and structural effect in plain prose.

### 5. Write the changelog entry

Format the entry as follows:

```markdown
## <TARGET_TAG> — <YYYY-MM-DD>

### Commits
<commit list>

### Code structure & content changes

<one paragraph or bullet section per affected service/area, describing what changed structurally and why it matters>

---
```

### 6. Prepend to CHANGELOG.md

- Read the current `CHANGELOG.md`.
- Insert the new entry immediately after the `# Changelog` heading line.
- Write the file back using the Edit tool.
- Confirm the update to the user.
