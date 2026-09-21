---
name: ent:release-tag
description: Release the latest Bitrise_Deploy_XXXX tag: merge it into master, tag master Version_XXXX, and create a GitHub release named vMAJOR.MINOR (e.g. 15013456 -> v15.1).
argument-hint: [--dry-run] [bitrise-tag-name]
allowed-tools: Bash, Read
---

Promote a Bitrise deploy tag to a production release of the Android app.

## Dry run

If the arguments contain `--dry-run`, run steps 1 and 2 (read-only; `git fetch` is allowed) and, for steps 3-6, only **print** the exact commands that would run, with resolved values. Additionally simulate the merge without touching anything: `git merge-tree --write-tree master $BITRISE_TAG` (reports conflicts, if any) and `git log --oneline master..$BITRISE_TAG`. Do not checkout, merge, tag, push, or call `gh release create`. End with "DRY RUN — nothing was changed". The other arguments are parsed after removing `--dry-run`.

## Version code format

`XXXX` is the Android version code, laid out as `MMmmbbbb`-style: the digits before the last 6 are the major, the next 2 digits are the minor, the last 4 are the build number.

- `major = code / 1000000` (integer division)
- `minor = (code / 10000) % 100` (integer, no leading zero)
- Release name: `v<major>.<minor>`

Examples: `15013456` -> `v15.1`, `15005680` -> `v15.0`, `14045570` -> `v14.4`.

## Steps

### 1. Preconditions

- Remember the current branch: `ORIGINAL_BRANCH=$(git rev-parse --abbrev-ref HEAD)`.
- `gh auth status` must succeed.
- `git fetch origin --tags --prune`
- Uncommitted changes are stashed in step 4 (not here), after the user has confirmed. In a dry run, just list them with `git status --porcelain` and say they would be stashed.

### 2. Resolve the Bitrise tag

- If a tag name is given (after removing `--dry-run`), use it as `BITRISE_TAG` (must match `^Bitrise_Deploy_[0-9]+$`).
- Otherwise take the highest one by version code: `git tag --list 'Bitrise_Deploy_*' | sort -t_ -k3 -n | tail -1`
- `CODE` = the numeric suffix. Compute `MAJOR`, `MINOR`, then `VERSION_TAG=Version_$CODE` and `RELEASE_NAME=v$MAJOR.$MINOR`.
- If `VERSION_TAG` already exists (locally or on origin), stop and report — never overwrite a tag.

### 3. Confirm with the user

Show a summary and wait for approval, because the next steps push to origin and publish a release:

- Bitrise tag, commit it points to
- Number of commits it brings into master (`git log --oneline master..$BITRISE_TAG | wc -l`)
- `VERSION_TAG` and `RELEASE_NAME`

### 4. Merge into master

If `git status --porcelain` is not empty, stash first (tracked and untracked files):

```
git stash push --include-untracked -m "release-tag auto-stash"
```

Then:

```
git checkout master
git pull --ff-only origin master
git merge $BITRISE_TAG -m "Merge tag '$BITRISE_TAG' into master"
```

- Fast-forward is fine when possible (git will do it without creating a merge commit; then drop `-m`'s effect naturally).
- On conflicts: stop, leave the state for the user and explain (mention that the auto-stash is still in `git stash list`). Do not resolve silently.

### 5. Tag master

```
git tag $VERSION_TAG master
git push origin master $VERSION_TAG
```

### 6. Create the GitHub release

```
gh release create $VERSION_TAG --title "$RELEASE_NAME" --generate-notes --verify-tag
```

`--generate-notes` builds notes from PRs/commits since the previous release. If the user asks for the repo's CHANGELOG entry instead, use the `ent:changelog-tag` skill's output as `--notes`.

### 7. Report

Print the release URL (`gh release view $VERSION_TAG --json url -q .url`) then restore the user's state: `git checkout $ORIGINAL_BRANCH`, and if a stash was created in step 4, `git stash pop`. If the pop conflicts, stop and tell the user (the stash is kept).

## Rules

- Never force-push, never delete or move existing tags.
- Do not create commits other than the merge commit in step 4.
