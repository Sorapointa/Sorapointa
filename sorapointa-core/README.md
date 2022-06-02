# Core Module

[简体中文](README.zh-CN.md)

## Command System

The command system is based on [**Yac**](https://githubfast.com/Colerar/Yac)
(A variety of [**clikt**](https://ajalt.github.io/clikt/)),
usage is basically the same as **clikt**.

The main components are: `Command`, `CommandSender`, `CommandManager`

- `Command`: The command object, with `run` function and parser provided by **Yac**
- `CommandSender`: The sender who invoke the command
- `CommandManager`: A manager to register, invoke `Command` asynchronously and thread-safely.

### Create

```kotlin
class ExampleCommand(
  private val sender: CommandSender,
) : Command(sender, ExampleCommand) {
  companion object : Entry(
    name = "example",
    help = "An example command",
    alias = listOf("alias")
  )

  val option by option("--option").flag()

  override suspend fun run() {
    sender.sendMessage("Hello Command! Option: $option")
  }
}
```

There is a little trick, properties of command are store in `Entry` object,
so we can declare a companion object.
Then we can access it with class name directly.

For more information, see [Clikt Documentation](https://ajalt.github.io/clikt/).

### Register

Register a command:

```kotlin
val command = CommandNode(ExampleCommand) { sender -> ExampleCommand(sender) }
CommandManager.registerCommand(command)
```

The first parameter of `CommandNode` is `CommandEntry`, the companion object we declared before.
The second parameter is a high-order function, used to construct a command instance.

Register a list of commands:

```kotlin
val commands = listOf(
  CommandNode(ExampleCommand) { sender -> ExampleCommand(sender) },
  CommandNode(Foo) { sender -> Foo(sender) },
  CommandNode(Bar) { sender -> Bar(sender) },
  CommandNode(Help) { sender -> Help(sender) },
)
CommandManager.registerCommands(commands)
```

### Invoke

At most of the time, you do not need to invoke command manually, core will invoke for you.

But in some cases, you may want to call command manually in your code, 
or invoke command when player trigger event.

```kotlin
// just an example for invoking command, 
// related module still in progress,
// so it may be not applicable now

// current this: Player
CommandManager.invokeCommand(this.asSender(), ExampleCommand.name)
```
