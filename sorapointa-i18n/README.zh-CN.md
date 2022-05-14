# I18n 模块

## 包含的配置

下列配置应在程序开始时加载，例如 `I18Config.reload()`:

- I18nConfig

## 用法

### 注册

- `I18nManager` 是线程安全的，所有的操作都可以被异步调用
- 如果语言代码重复，新的会覆盖旧的

```kotlin
// 基础的语言包需要输入 LanguagePack
fun registerLanguage(languagePack: LanguagePack)

// 从文件注册语言包
suspend fun registerLanguage(languageFile: File)

// 从文件夹注册语言包
suspend fun registerLanguagesDirectory(
  directory: File,
  match: Regex = languageFileRegex,
  depth: Int = 1,
)
```

### 渲染

可以使用拓展方法 `String.i18n()`。

```kotlin
fun String.i18n(vararg args: Any?, locale: Locale? = null): String
```

接收者是点风格的 key，例如 `samples.hello`。

在代码：

```kotlin
"samples.hello".i18n()
```

添加字符串到语言包 JSON:

```json
{
  "locale": "zh-CN",
  "strings": {
    "samples.hello": "你好!"
  }
}
```

在字符串中添加变量：

代码：

```kotlin
"samples.hello.var".i18n(time, name)
```

JSON:

```json
{
  "locale": "zh-CN",
  "strings": {
    "samples.hello": "{0}好! {1}!"
  }
}
```

**注意: 位置敏感。** `i18n()` 方法按顺序替换占位符。

指定语言:

```kotlin
"samples.hello.specified".i18n(locale = Locale("zh-CN"))
```
