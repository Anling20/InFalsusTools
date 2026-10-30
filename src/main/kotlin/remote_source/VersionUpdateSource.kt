package cn.anling.inftool.remote_source

sealed class VersionUpdateSource(val i18nKey: String) {
    data object GitHub : VersionUpdateSource("version_update_source.github")
    data object GitCode : VersionUpdateSource("version_update_source.gitcode")
}