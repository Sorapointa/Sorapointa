# I18n Module

## Included Config

Following config must be load at program start, like `I18Config.reload()`:

- I18nConfig

## Usage

### Register

- `I18nManager` is thread-safe, all operation can be invoked asynchronously.
- If duplicates occur, the new pack will overwrite the old.

```kotlin
// Basic language pack registration, just pass an instance of LanguagePack
fun registerLanguage(languagePack: LanguagePack)

// Register language pack from file
suspend fun registerLanguage(languageFile: File)
```

Core can load file from resource

### Render

Extension method `String.i18n()` is available.

```kotlin
fun String.i18n(vararg args: Any?, locale: Locale? = null): String
```

The receiver is the dot-styled key for localized strings, e.g. `samples.hello`.

Use in code:

```kotlin
"samples.hello".i18n()
```

Add key and value to language pack json:

```json
{
  "locale": "en",
  "strings": {
    "samples.hello": "Hello!"
  }
}
```

If you want to add variables:

Code:

```kotlin
"samples.hello.var".i18n(time, name)
```

Json:

```json
{
  "locale": "en",
  "strings": {
    "samples.hello": "Good {0}! {1}!"
  }
}
```

**Note: Order matters.** `i18n()` method replace holders by number index.

With specified locale:

```kotlin
"samples.hello.specified".i18n(locale = Locale("en"))
```
