package cn.anling.inftool.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import cn.anling.infalsustools.generated.resources.Res
import cn.anling.infalsustools.generated.resources.folder_open
import cn.anling.inftool.GitHub
import cn.anling.inftool.Manifest
import cn.anling.inftool.address.AddressContainer
import cn.anling.inftool.component.ClickableTextField
import cn.anling.inftool.component.DropdownMenu
import cn.anling.inftool.localization.Language
import cn.anling.inftool.localization.LocalizationContext
import cn.anling.inftool.localization.currentLanguage
import cn.anling.inftool.localization.i18n
import cn.anling.inftool.model.HomeModel
import cn.anling.inftool.potential_algorithm.InFalsusToolsPotentialAlgorithm
import cn.anling.inftool.potential_algorithm.PotentialAlgorithm
import cn.anling.inftool.ranking.Ranking
import cn.anling.inftool.setting.toComposeStated
import cn.anling.inftool.song.ConstantTable
import cn.anling.inftool.unfocusOnPressEnter
import com.russhwolf.settings.Settings
import com.russhwolf.settings.int
import com.russhwolf.settings.string
import io.github.vinceglb.filekit.*
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.ktor.client.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.painterResource

object SettingPage {
    class ViewModel(
        settings: Settings,
        initialSaveFile: String,
        client: HttpClient,
    ) {
        var username by settings.string("Setting.username", "Player").toComposeStated()

        private var _saveFile by settings.string("Setting.saveFile", initialSaveFile).toComposeStated()
        var saveFile: PlatformFile
            get() = PlatformFile(_saveFile)
            set(value) {
                _saveFile = value.absolutePath()
            }

        var potentialAlgorithmString by settings.string(
            key = "Setting.potentialAlgorithm",
            defaultValue = "builtin:" + InFalsusToolsPotentialAlgorithm.id,
        ).toComposeStated()
        val potentialAlgorithm: PotentialAlgorithm
            get() {
                val (type, content) = potentialAlgorithmString.split(":", limit = 2)
                return runCatching {
                    when (type) {
                        "builtin" -> PotentialAlgorithm.builtin.find { it.id == content }
                            ?: error("Unknown builtin potential algorithm: $content")

                        else -> error("Unknown potential algorithm type: $potentialAlgorithmString")
                    }
                }.getOrElse { throwable ->
                    println(throwable.message)
                    throwable.printStackTrace()
                    potentialAlgorithmString = "builtin:" + InFalsusToolsPotentialAlgorithm.id
                    InFalsusToolsPotentialAlgorithm
                }
            }

        val githubCandidates = listOf(
            GitHub.GitHubProxy(client),
            GitHub.Direct(client),
            GitHub.Cache,
        )

        private var _github by settings.string("Settings.github", "builtin:github_proxy").toComposeStated()
        var github: GitHub
            get() {
                val (type, content) = _github.split(":", limit = 2)
                return runCatching {
                    when (type) {
                        "builtin" -> when (content) {
                            "github_proxy" -> githubCandidates.first { it is GitHub.GitHubProxy }
                            "direct" -> githubCandidates.first { it is GitHub.Direct }
                            "cache" -> githubCandidates.first { it is GitHub.Cache }
                            else -> error("Unknown builtin github content: $content")
                        }

                        else -> error("Unknown github type: $potentialAlgorithmString")
                    }
                }.getOrElse { throwable ->
                    println(throwable.message)
                    throwable.printStackTrace()
                    _github = "builtin:github_proxy"
                    githubCandidates.first { it is GitHub.Cache }
                }
            }
            set(value) {
                _github = when (value) {
                    is GitHub.GitHubProxy -> "builtin:github_proxy"
                    is GitHub.Direct -> "builtin:direct"
                    GitHub.Cache -> "builtin:cache"
                }
            }

        var constantTable by mutableStateOf<ConstantTable>(emptyMap())
            private set

        var ranking by mutableStateOf<Ranking>(emptyMap())
            private set

        private var _addressString by settings.string("Setting.address", "init").toComposeStated()
        var addressMap by mutableStateOf<Map<String, AddressContainer>>(emptyMap())
        var addressContainer by mutableStateOf(AddressContainer.empty)
            private set
        var address: String
            get() = _addressString
            set(value) {
                _addressString = value
                addressContainer = addressMap[address]!!
            }

        private var languageIndex by settings.int("Setting.language", 1).toComposeStated()
        var language: Language
            get() = Language.entries[languageIndex]
            set(value) {
                languageIndex = value.ordinal
            }

        suspend fun updateData() = runCatching {
            val constantTableJson = github.open("constants.json").getOrThrow()
            constantTable = Json.decodeFromString(constantTableJson)

            val rankingJson = github.open("ranking.json").getOrThrow()
            ranking = Json.decodeFromString(rankingJson)

            val addressManifestJson = github.open("address/manifest.json").getOrThrow()
            val addressManifest = Json.decodeFromString<Manifest>(addressManifestJson)

            val addressMapJson = addressManifest.files.associateWith { file ->
                github.open("address/$file").getOrThrow()
            }
            addressMap = addressMapJson.mapValues { (_, file) ->
                Json.decodeFromString<AddressContainer>(file)
            }

            if (address == "init") {
                address = addressManifest.default
            }

            if (github !is GitHub.Cache) {
                val cache = githubCandidates.first { it is GitHub.Cache } as GitHub.Cache
                cache.save("constants.json", constantTableJson)
                cache.save("ranking.json", rankingJson)
                cache.save("address/manifest.json", addressManifestJson)
                addressMapJson.forEach { (file, json) ->
                    cache.save("address/$file", json)
                }
            }
        }
    }

    @Composable
    context(_: HomeModel, _: LocalizationContext)
    fun Page(
        viewModel: ViewModel,
        onUpdateGameRecords: suspend () -> Unit,
    ) = Content(
        viewModel = viewModel,
        onUpdateGameRecords = onUpdateGameRecords,
    )

    @Composable
    context(homeModel: HomeModel, _: LocalizationContext)
    private fun Content(
        scope: CoroutineScope = rememberCoroutineScope(),
        focusManager: FocusManager = LocalFocusManager.current,
        viewModel: ViewModel,
        onUpdateGameRecords: suspend () -> Unit,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            val state = rememberTextFieldState(viewModel.username)
            TextField(
                state = state,
                label = { Text(i18n("setting.username")) },
                lineLimits = TextFieldLineLimits.SingleLine,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = TextFieldDefaults.colors().focusedContainerColor.copy(alpha = 0.6F),
                    unfocusedContainerColor = TextFieldDefaults.colors().unfocusedContainerColor.copy(alpha = 0.6F),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .unfocusOnPressEnter(focusManager)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            viewModel.username = state.text.toString()
                        }
                    },
            )
            ClickableTextField(
                value = viewModel.saveFile.absolutePath(),
                label = { Text(i18n("setting.save_file")) },
                trailingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.folder_open),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = TextFieldDefaults.colors().focusedContainerColor.copy(alpha = 0.6F),
                    unfocusedContainerColor = TextFieldDefaults.colors().unfocusedContainerColor.copy(alpha = 0.6F),
                ),
                onClick = {
                    val file = FileKit.openFilePicker(
                        type = FileKitType.File("sav"),
                        mode = FileKitMode.Single,
                        directory = viewModel.saveFile.parent()?.let {
                            when (it.exists() && it.isDirectory()) {
                                true -> it
                                else -> null
                            }
                        },
                    )
                    if (file != null) {
                        viewModel.saveFile = file
                        scope.launch(Dispatchers.IO) {
                            onUpdateGameRecords()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            DropdownMenu(
                text = viewModel.potentialAlgorithm.name,
                label = i18n("setting.potential_algorithm"),
            ) { onExpandedChange ->
                PotentialAlgorithm.builtin.forEach { algorithm ->
                    DropdownMenuItem(
                        text = { Text(algorithm.name) },
                        onClick = {
                            onExpandedChange(false)
                            viewModel.potentialAlgorithmString = "builtin:" + algorithm.id
                            scope.launch(Dispatchers.IO) {
                                onUpdateGameRecords()
                            }
                        },
                    )
                }
            }
            DropdownMenu(
                text = viewModel.address.dropLast(".json".length),
                label = i18n("setting.memory_address_version"),
            ) { onExpandedChange ->
                viewModel.addressMap.forEach { (file, _) ->
                    DropdownMenuItem(
                        text = { Text(file.dropLast(".json".length)) },
                        onClick = {
                            onExpandedChange(false)
                            viewModel.address = file
                        },
                    )
                }
            }
            DropdownMenu(
                text = i18n(viewModel.github.i18nKey),
                label = i18n("setting.github"),
            ) { onExpandedChange ->
                viewModel.githubCandidates.forEach { github ->
                    DropdownMenuItem(
                        text = { Text(i18n(github.i18nKey)) },
                        onClick = {
                            onExpandedChange(false)
                            viewModel.github = github
                            scope.launch {
                                homeModel.waiting = true
                                launch {
                                    homeModel.snackbarHostState.showSnackbar("数据更新中...")
                                }
                                viewModel.updateData()
                                    .onSuccess {
                                        onUpdateGameRecords()
                                        launch {
                                            homeModel.snackbarHostState.showSnackbar("数据更新完成")
                                        }
                                    }
                                    .onFailure {
                                        it.printStackTrace()
                                        launch {
                                            homeModel.snackbarHostState.showSnackbar("数据更新失败: ${it.message}")
                                        }
                                    }
                                homeModel.waiting = false
                            }
                        },
                    )
                }
            }
            DropdownMenu(
                text = viewModel.language.displayText,
                label = i18n("setting.language"),
            ) { onExpandedChange ->
                Language.entries.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.displayText) },
                        onClick = {
                            onExpandedChange(false)
                            viewModel.language = item
                            currentLanguage = item
                        },
                    )
                }
            }
        }
    }
}