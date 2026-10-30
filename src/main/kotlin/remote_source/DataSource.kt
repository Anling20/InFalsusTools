package cn.anling.inftool.remote_source

import cn.anling.inftool.getConfigDirectory
import io.github.vinceglb.filekit.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

sealed class DataSource(val i18nKey: String) {
    abstract suspend fun open(file: String): Result<String>

    class GitHub(private val client: HttpClient) : DataSource("data_source.github") {
        override suspend fun open(file: String): Result<String> = runCatching {
            client
                .get("https://raw.githubusercontent.com/Anling20/InFalsusTools_Data/refs/heads/main/$file")
                .bodyAsText()
        }
    }

    class GitCode(private val client: HttpClient) : DataSource("data_source.gitcode") {
        override suspend fun open(file: String): Result<String> = runCatching {
            client
                .get("https://raw.gitcode.com/Anling20/InFalsusTools_Data/raw/main/$file")
                .bodyAsText()
        }
    }

    class Gitee(private val client: HttpClient) : DataSource("data_source.gitee") {
        override suspend fun open(file: String): Result<String> = runCatching {
            client
                .get("https://raw.giteeusercontent.com/ANMSakura/InFalsusTools_Data/raw/main/$file")
                .bodyAsText()
        }
    }

    object Cache : DataSource("data_source.cache") {
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