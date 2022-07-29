# Core 模块

[English](README.zh-CN.md)

## 命令系统

命令系统基于 [**Yac**](https://githubfast.com/Colerar/Yac) ([**clikt**](https://ajalt.github.io/clikt/) 的变体)，
用法基本和 **clikt** 相同。

主要组成有: `Command`, `CommandSender`, `CommandManager`

- `Command`: 命令对象，具有 `run` 函数和由 **Yac** 提供的命令解析功能
- `CommandSender`: 命令发送者
- `CommandManager`: 命令管理器，提供并发、线程安全的命令注册和调用

### 创建

```kotlin
class ExampleCommand(
  private val sender: CommandSender,
) : Command(sender, ExampleCommand) {
  companion object : Entry(
    name = "example",
    help = "示例指令",
    alias = listOf("示例")
  )

  val option by option("--option").flag()

  override suspend fun run() {
    sender.sendMessage("你好！选项：$option")
  }
}
```

请注意这里的小技巧，命令的属性用 `Entry` 对象存储，我们可以将其定义为伴生对象(companion object)，以便于用类名直接获取。

关于命令解析，请参见：[Clikt 文档](https://ajalt.github.io/clikt/)

### 注册

注册单个命令：

```kotlin
val command = CommandNode(ExampleCommand) { sender -> ExampleCommand(sender) }
CommandManager.registerCommand(command)
```

`CommandNode`的第一个参数是 `CommandEntry`，之前我们将其定义为伴生对象；
第二个参数是一个高阶函数，用于构造新的命令实例。

注册命令清单:

```kotlin
val commands = listOf(
  CommandNode(ExampleCommand) { sender -> ExampleCommand(sender) },
  CommandNode(Foo) { sender -> Foo(sender) },
  CommandNode(Bar) { sender -> Bar(sender) },
  CommandNode(Help) { sender -> Help(sender) },
)
CommandManager.registerCommands(commands)
```

### 调用

多数情况下你不需要手动调用命令，core 会帮你调用。

但在有些情况，你可能想在代码里手动调用命令，或在玩家触发某些事件时调用。

```kotlin
// 只是一个示例，代码可能并不可用，因为相关模块尚未完成
// 上下文中的 this: Player
CommandManager.invokeCommand(this.asSender(), ExampleCommand.name)
```
