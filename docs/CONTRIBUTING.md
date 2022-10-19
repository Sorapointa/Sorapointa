# Contributing Guideline

[简体中文](CONTRIBUTING.zh-CN.md)

## Code Style

- [Kotlin Official Code Style](https://kotlinlang.org/docs/coding-conventions.html)
- Indent is 4 spaces, the `.editorconfig` in our project will help your IDE to automatically set it
- Star import is allowed
- We require **all PRs to pass the `ktlint` check** before they merge into the active branch
- We recommended you to format your code using `Ktlint` before committing.
  You can install [Ktlint](https://ktlint.github.io/) first then run `format.sh` in project root path.

## Git

### Branch

- **Active branch** refers to the `dev`, the development branch
- All commits or changes that want to merge into the **active branch**
  will be required to submit PR and pass all CI check.

### Push

- If you want to submit your commits or changes into **active branch**,
  you **must submit those through PR** and **pass all CI check**.

### Merge Branch

- Please **don't** pull any upstream updates when you open a new branch (or fork),
  which are used for submitting your changes or commits,
  except those updates are required for your changes or commits.
  Even though, you need to update your branch following [this rule](#incompatible-changes-and-sync-upstream-updates)
- You must turn on the `rebase` option to pull upstream updates
  - It shouldn't occur any conflicts, except you had pulled upstream updates after your committed your own code.
- Merge PR with different methods determined by different situations
  - If the PR contains big changes, we often merge it into active branch with `merge` or `squash`
  - If the PR contains small changes, we often merge it into active branch with `rebase`

### Incompatible Changes and Sync Upstream Updates

- Please **don't** pull any upstream updates when you open a new branch (or fork),
  but if these updates are necessary for you, please follow this process.
  - Create a new branch `xxx_update` from the current latest upstream branch to the local
  - Merge your commits into the `xxx_update` branch
    through `rebase`(if there are no conflicts) or `cherrypick`
  - Resolve all conflicts and fix all compatibility errors
  - Submit PR to make `xxx_update` merge into active branch
- When you have made any incompatibility changes,
  please follow the same process as above and make a new branch, like `xxx_premerge`,
  with all incompatibility issues fixed
  - Note: If there are other PRs or branches that are also affected by your incompatible update,
    set the merge target of the other PRs to `xxx_premerge`, which is equivalent to a staging branch
  - When all affected PRs or branches have been merged into `xxx_premerge`
    and all compatibility issues have been fixed,
    submit a PR to make it merge into the active branch

### Commit

- Write commit messages in English
- Short message template: `type(scope): message`. For example, `feat(network): impl KCP protocol`
  - For `type` field, abbreviated and qualified name are both acceptable.
- Use `#issue_number` to mention related issue for easy tracking

You can try [IDEA Conventional Commits](https://plugins.jetbrains.com/plugin/13389-conventional-commit)
plugin for smart completion.

[![](https://user-images.githubusercontent.com/62297254/196744218-e6bad849-5307-4761-a8b6-baa147c1852b.png)](https://plugins.jetbrains.com/plugin/13389-conventional-commit)

For reference: [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/)

## More Documentations

- [Unit Test](guides/unit-test.md)
  - Please write unit tests to ensure the reliability of the code. 
  - All commits merged into the master branch must pass unit tests.
- Concurrent Safety
  - [Kotlin AtomicFU Guideline](guides/kotlin-atomicfu.md)
  - [Concurrent Safety](guides/concurrency.md)
- [Database Operation Safety](guides/database.md)
- Others: [guides](./guides)
