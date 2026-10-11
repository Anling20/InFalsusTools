package cn.anling.inftool.localization

val defaultLocalizationContainers = mapOf(
    LocalizationContainer.english,
    LocalizationContainer.simplifiedChinese,
)

class LocalizationContainer(vararg texts: Pair<String, String>) {
    private val map = texts.toMap()

    operator fun get(key: String) = map[key]

    companion object
}

class LocalizationContext(
    var language: Language,
    private val containers: Map<Language, LocalizationContainer>,
) {
    operator fun get(key: String): String {
        return containers[language]?.get(key)
            ?: containers[Language.English]?.get(key)
            ?: key
    }
}

context(context: LocalizationContext)
fun i18n(key: String) = context[key]

context(context: LocalizationContext)
var currentLanguage
    get() = context.language
    set(value) {
        context.language = value
    }