# 贡献指南

[English](CONTRIBUTING.en.md)

## 代码风格

- Kotlin 官方代码风格 - [中文站参考](https://www.kotlincn.net/docs/reference/coding-conventions.html)
- 缩进 4 空格
- 可以使用 `*` 导入

建议在 commit 前使用 `Ktlint` 格式化代码。可安装 Ktlint 后运行根目录 `format.sh`。

## Git 规范

### Commit 规范

- commit 消息用英语
- 短消息格式满足：`类型(作用域): 消息`，例如 `feat(network): impl KCP protocol`
  - `类型` 字段无论用缩写或全称都可行。
- 用 `#issue 编号` 提及相关的 issue，便于跟踪

  可以使用 IDEA Git Message 插件自动生成

  [![](https://user-images.githubusercontent.com/25319400/165979933-7481d332-9171-4ee1-8d37-078187f152a0.png)](https://plugins.jetbrains.com/plugin/13477-git-commit-message-helper)

  另可参考：[Commit Message 和 Change Log 编写指南](https://www.ruanyifeng.com/blog/2016/01/commit_message_change_log.html)

### Push 规范

所有涉及代码的 commit 都应该交 PR。

有 write 权限的可在主分支另开 branch，无则 fork 即可。

- 在能 rebase 的时候绝不要用 merge
- commit 过多时考虑 squash

可参考：[Git 使用规范流程](https://www.ruanyifeng.com/blog/2015/08/git-use-process.html)

## 单元测试(建议)

尽量写单测保证代码可靠性。

同时如果你的代码使得单测失败，是无法通过 code review 的。

## 其他…

请查看 [docs](docs) 
