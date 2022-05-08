# Core Module

## Command System

The command system uses [**Yac**](https://githubfast.com/Colerar/Yac) (A migrate of **clikt**) as the core, and the core
usage is basically the same as
**clikt** ([refer to the documentation](https://ajalt.github.io/clikt/)).

### Add a new command

If you want to implement a new command, create an `object` that extends `SorapointaCommand` and register it by
calling `CommandManager.registerCommand` where appropriate (_\[WIP\] Program & Plugin Initialization_).

The object extends `SorapointaCommand` need to specify a `name` and provide appropriate `help` and `alias`.

The default type (that is, permission) is `ADMIN`, which means that the command needs to be used only if the `type`
of `sender` is `ADMIN` or above.

SorapointaCommand provides a property sender inside, and the rest are set as For parameters, etc., please refer to the
clikt documentation.

Command should use [i18n](../sorapointa-i18n/README.md) reasonably.

If you need a demo. Please read the [HelpCommand.kt](src/main/kotlin/org/sorapointa/command/defaults/HelpCommand.kt)
