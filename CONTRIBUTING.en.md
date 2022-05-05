# Contributing Guideline

## Code Style

- [Kotlin Official Code Style](https://kotlinlang.org/docs/coding-conventions.html)
- 4 Spaces Indent
- Star import is allowed

Recommended to format code using `Ktlint` before committing. You can install Ktlint first then run `format.sh` in
project root path.

## Git

### Commit

- Write commit messages in English
- Short message template: `type(scope): message`. For example, `feat(network): impl KCP protocol`
  - For `type` field, abbreviated and qualified name are both acceptable.
- Use `#issue_number` to mention related issue for easy tracking

  You can use IDEA Git Message Plugin to generate messages automatically.

  [![](https://user-images.githubusercontent.com/25319400/165979933-7481d332-9171-4ee1-8d37-078187f152a0.png)](https://plugins.jetbrains.com/plugin/13477-git-commit-message-helper)

  For reference: [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/)

### Push

All commits involving code should create a pull request.

If you have `write` permission, please work in a new branch.

Or, you can fork the main repo.

For reviewers:

- Never merge if you can rebase
- Squash if there are a lot of commits

### Unit Test (Recommended)

Please write unit tests to ensure the reliability of the code.

If your code fails the unit test, you will not pass code review.
