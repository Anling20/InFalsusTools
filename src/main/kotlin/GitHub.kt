package cn.anling.inftool

import io.github.vinceglb.filekit.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

sealed class GitHub(val i18nKey: String) {
    abstract suspend fun open(file: String): Result<String>

    class GitHubProxy(private val client: HttpClient) : GitHub("github.github_proxy") {
        override suspend fun open(file: String): Result<String> = runCatching {
            client
                .get("https://ghfast.top/https://raw.githubusercontent.com/Anling20/InFalsusTools_Data/refs/heads/main/$file")
                .bodyAsText()
        }
    }

    class Direct(private val client: HttpClient) : GitHub("github.direct") {
        override suspend fun open(file: String): Result<String> = runCatching {
            client
                .get("https://raw.githubusercontent.com/Anling20/InFalsusTools_Data/refs/heads/main/$file")
                .bodyAsText()
        }
    }

    object Cache : GitHub("github.cache") {
        override suspend fun open(file: String): Result<String> = runCatching {
            (getConfigDirectory() / ".cache/github/$file").readString()
        }

        suspend fun save(file: String, content: String) {
            val platformFile = getConfigDirectory() / ".cache/github" / file
            val parent = platformFile.parent()
            if (parent?.exists() == false) {
                parent.createDirectories()
            }
            platformFile.writeString(content)
        }
    }
}