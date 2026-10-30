package cn.anling.inftool

import dev.nucleusframework.core.runtime.Platform
import dev.nucleusframework.updater.exception.NetworkException
import dev.nucleusframework.updater.provider.UpdateProvider
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.http.HttpClient

class GitCodeProvider(
    private val owner: String,
    private val repo: String,
    private val token: String? = null,
) : UpdateProvider {
    private val client = io.ktor.client.HttpClient(CIO)
    private var apiBaseUrl = "https://api.gitcode.com/api/v5"
    private val baseUrl = "https://gitcode.com"

    override fun getUpdateMetadataUrl(
        channel: String,
        platform: Platform,
    ): String {
        val fileName = metadataFileName(channel, platform)
        val latestRelease = runBlocking {
            client.get("$apiBaseUrl/repos/$owner/$repo/releases/latest").body<Release>()
        }
        return latestRelease.assets.find { it.name == fileName }!!.browserDownloadUrl
    }

    override fun resolveMetadataUrl(
        channel: String,
        platform: Platform,
        httpClient: HttpClient,
    ): String {
        val fileName = metadataFileName(channel, platform)
        if (channel.equals(LATEST_CHANNEL, ignoreCase = true)) {
            val latestRelease = runBlocking {
                client.get("$apiBaseUrl/repos/$owner/$repo/releases/latest").body<Release>()
            }
            return latestRelease.assets.find { it.name == fileName }!!.browserDownloadUrl
        }
        val tag = runBlocking {
            findLatestPrereleaseTag(channel)
        }
        val release = runBlocking {
            client.get("$apiBaseUrl/repos/$owner/$repo/releases/tags/$tag").body<Release>()
        }
        return release.assets.find { it.name == fileName }!!.browserDownloadUrl
    }

    override fun getDownloadUrl(
        fileName: String,
        version: String,
    ): String = "$baseUrl/$owner/$repo/releases/download/v$version/$fileName"

    override fun authHeaders(): Map<String, String> =
        if (token != null) {
            mapOf("access_token" to token)
        } else {
            emptyMap()
        }

    private fun metadataFileName(
        channel: String,
        platform: Platform,
    ): String {
        val suffix = platformSuffix(platform)
        return if (suffix.isEmpty()) "$channel.yml" else "$channel-$suffix.yml"
    }

    private suspend fun findLatestPrereleaseTag(channel: String): String {
        val response = client.get("$apiBaseUrl/repos/$owner/$repo/releases") {
            if (token != null) {
                parameter("access_token", token)
            }
            parameter("per_page", PER_PAGE)
        }
        if (!response.status.isSuccess()) {
            val rateLimited = response.status == HttpStatusCode.TooManyRequests
            val detail =
                if (rateLimited) {
                    "rate limit exceeded — configure a token to raise the limit"
                } else {
                    "HTTP ${response.status.value}"
                }
            throw NetworkException("GitHub API failed while listing releases for $owner/$repo: $detail")
        }
        val releases = response.body<List<Release>>()
        val match =
            releases.firstOrNull { release ->
                release.prerelease && tagMatchesChannel(release.tagName, channel)
            } ?: throw NoSuchElementException(
                "No release found for channel '$channel' within the most recent $PER_PAGE releases. " +
                        "Publish a fresh release on this channel.",
            )
        return match.tagName
    }

    private fun tagMatchesChannel(
        tag: String,
        channel: String,
    ): Boolean {
        val suffix = tag.substringAfter('-', missingDelimiterValue = "")
        return suffix.startsWith(channel, ignoreCase = true)
    }

    private fun platformSuffix(platform: Platform): String =
        when (platform) {
            Platform.Windows -> ""
            Platform.MacOS -> "mac"
            Platform.Linux -> "linux"
            Platform.Unknown -> ""
        }

    @Serializable
    private data class Release(
        @SerialName("tag_name")
        val tagName: String,
        val prerelease: Boolean,
        val assets: List<Asset>
    ) {
        @Serializable
        data class Asset(
            @SerialName("browser_download_url")
            val browserDownloadUrl: String,
            val name: String,
            val type: String,
            val id: Int? = null,
            val size: Int? = null,
            val digest: String? = null,
        )
    }

    private companion object {
        const val LATEST_CHANNEL = "latest"
        const val PER_PAGE = 100
    }
}