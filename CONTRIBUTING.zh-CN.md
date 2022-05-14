# 贡献指南

[English](CONTRIBUTING.zh-CN.md)

## 代码风格

- Kotlin 官方代码风格 - [中文站参考](https://www.kotlincn.net/docs/reference/coding-conventions.html)
- 缩进 4 空格，项目中的 `.editorconfig` 配置会帮助您的 IDE 自动设置
- 可以使用 `*` 导入
- 我们会要求**所有并入活跃分支**的 PR 通过 `ktlint` 检查
- 我们建议您在提交前使用 `Ktlint` 格式化代码。可安装 [Ktlint](https://ktlint.github.io/) 后运行根目录 `format.sh`。

## Git 规范

### 关于分支

- 一般而言**活跃分支**指的是 `dev` 开发分支
- 在**活跃分支**上进行修改必须通过其他分支提交 PR 并通过所有的 CI 检查


### Push 规范

- 若您希望在**活跃分支**上提交您的修改，则**必须通过其他分支**提交 PR 并**通过所有的 CI 检查**

可参考：[Git 使用规范流程](https://www.ruanyifeng.com/blog/2015/08/git-use-process.html)

### 关于合并

- 在当您开启了一个新分支（或者 `fork`），用于提交您的代码修改时，请在 `fork` 后**不要拉取任何上游的更新**，
  除非这些更新对于你的开发是必须的，如果是必须的，请按照[关于不兼容性修改与同步上游更新](#关于不兼容性修改与同步上游更新)
- 从远程拉取到本地时，必须使用 `rebase`
  - 不应该存在冲突，若存在冲突一般情况下是您在提交修改的同时拉取了上游的更新
- 将 PR 合并时，应该酌情使用不同的合并方式
  - 如大修改，一般考虑 `merge` 或者 `squash`
  - 小修改一般考虑 `rebase`


### 关于不兼容性修改与同步上游更新

- 一般而言，最好不要在 `fork` 后拉取任何上游的更新，但是若这些更新对您来说是必须的请按照以下流程进行更新：
  - 从当前最新的上游分支中新建分支 `xxx_update` 到本地
  - 将您已经在其他分支提交的更改 `rebase`（若无冲突） 或者 `cherrypick` 进入 `xxx_update` 分支
  - 解决所有冲突，并修正所有的兼容性错误
  - 将 `xxx_update` 提交 PR 并入活跃分支
- 当您进行了任何的不兼容性修改，请同样按照上述流程，将修复了所有不兼容性问题的分支作为新分支，如 `xxx_premerge`
  - 注意，若此时同时存在其他 PR 或者分支，并同样受到您的不兼容性更新影响，
    请将其他 PR 的合并目标设置为 `xxx_premerge` 也就是相当于一个暂存分支
  - 当所有受到影响 PR 或者分支，都合并进入了 `xxx_premerge` 并解决了所有兼容性问题后，提交 PR 申请并入活跃分支

### Commit 规范

- `commit` 中的信息请使用英语
- 短消息格式满足：`类型(作用域): 消息`，例如 `feat(network): impl KCP protocol`
  - `类型` 字段无论用缩写或全称都可行。
- 用 `#issue 编号` 提及相关的 issue，便于跟踪

  可以使用 IDEA Git Message 插件自动生成

  [![](https://user-images.githubusercontent.com/25319400/165979933-7481d332-9171-4ee1-8d37-078187f152a0.png)](https://plugins.jetbrains.com/plugin/13477-git-commit-message-helper)

  另可参考：[Commit Message 和 Change Log 编写指南](https://www.ruanyifeng.com/blog/2016/01/commit_message_change_log.html)


## 单元测试(建议)

[关于单元测试](docs/unittest.md)

- 尽量写单测保证代码可靠性。
- 与此同时，如果你提交的 PR 无法通过 CI 中的单元测试，将无法并入活跃分支

## 并发安全

请查看 [AtomicFU 指南](docs/kotlin-atomicfu.zh-CN.md) 以及 [关于并发安全](docs/concurrency.zh-CN.md)

## 数据库安全

请查看 [关于数据库安全](docs/database.zh-CN.md)

## 其他…

请查看 [docs](docs) 

