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
import cn.anling.inftool.remote_source.DataSource
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
import cn.anling.inftool.remote_source.VersionUpdateSource
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

        val dataSourceCandidates = listOf(
            DataSource.GitHub(client),
            DataSource.Gitee(client),
            DataSource.GitCode(client),
            DataSource.Cache,
        )

        private var _dataSource by settings.string("Settings.dataSource", "builtin:github").toComposeStated()
        var dataSource: DataSource
            get() {
                val (type, content) = _dataSource.split(":", limit = 2)
                return runCatching {
                    when (type) {
                        "builtin" -> when (content) {
                            "github" -> dataSourceCandidates.first { it is DataSource.GitHub }
                            "gitcode" -> dataSourceCandidates.first { it is DataSource.GitCode }
                            "gitee" -> dataSourceCandidates.first { it is DataSource.Gitee }
                            "cache" -> dataSourceCandidates.first { it is DataSource.Cache }
                            else -> error("Unknown builtin data source content: $content")
                        }

                        else -> error("Unknown data source type: $_dataSource")
                    }
                }.getOrElse { throwable ->
                    throwable.printStackTrace()
                    _dataSource = "builtin:github"
                    dataSourceCandidates.first { it is DataSource.GitHub }
                }
            }
            set(value) {
                _dataSource = when (value) {
                    is DataSource.GitHub -> "builtin:github"
                    is DataSource.GitCode -> "builtin:gitcode"
                    is DataSource.Gitee -> "builtin:gitee"
                    DataSource.Cache -> "builtin:cache"
                }
            }

        val versionUpdateSourceCandidates = listOf(
            VersionUpdateSource.GitHub,
            VersionUpdateSource.GitCode,
        )

        private var _versionUpdateSource by settings.string(
            key = "Setting.versionUpdateSource",
            defaultValue = "builtin:github",
        ).toComposeStated()
        var versionUpdateSource: VersionUpdateSource
            get() {
                val (type, content) = _versionUpdateSource.split(":", limit = 2)
                return runCatching {
                    when (type) {
                        "builtin" -> when (content) {
                            "github" -> versionUpdateSourceCandidates.first { it is VersionUpdateSource.GitHub }
                            "gitcode" -> versionUpdateSourceCandidates.first { it is VersionUpdateSource.GitCode }
                            else -> error("Unknown builtin version update source content: $content")
                        }

                        else -> error("Unknown version update source type: $_versionUpdateSource")
                    }
                }.getOrElse { throwable ->
                    throwable.printStackTrace()
                    _versionUpdateSource = "builtin:github"
                    versionUpdateSourceCandidates.first { it is VersionUpdateSource.GitHub }
                }
            }
            set(value) {
                _versionUpdateSource = when (value) {
                    is VersionUpdateSource.GitHub -> "builtin:github"
                    is VersionUpdateSource.GitCode -> "builtin:gitcode"
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
            val constantTableJson = dataSource.open("constants.json").getOrThrow()
            constantTable = Json.decodeFromString(constantTableJson)

            val rankingJson = dataSource.open("ranking.json").getOrThrow()
            ranking = Json.decodeFromString(rankingJson)

            val addressManifestJson = dataSource.open("address/manifest.json").getOrThrow()
            val addressManifest = Json.decodeFromString<Manifest>(addressManifestJson)

            val addressMapJson = addressManifest.files.associateWith { file ->
                this.dataSource.open("address/$file").getOrThrow()
            }
            addressMap = addressMapJson.mapValues { (_, file) ->
                Json.decodeFromString<AddressContainer>(file)
            }

            when (address == "init") {
                true -> address = addressManifest.default
                else -> addressContainer = addressMap[address]!!
            }

            if (dataSource !is DataSource.Cache) {
                val cache = dataSourceCandidates.first { it is DataSource.Cache } as DataSource.Cache
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
                text = i18n(viewModel.dataSource.i18nKey),
                label = i18n("setting.data_source"),
            ) { onExpandedChange ->
                viewModel.dataSourceCandidates.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(i18n(item.i18nKey)) },
                        onClick = {
                            onExpandedChange(false)
                            viewModel.dataSource = item
                            scope.launch {
                                homeModel.waiting = true
                                launch {
                                    homeModel.snackbarHostState.showSnackbar(i18n("setting.snackbar.data_updating"))
                                }
                                viewModel.updateData()
                                    .onSuccess {
                                        onUpdateGameRecords()
                                        launch {
                                            homeModel.snackbarHostState.showSnackbar(i18n("setting.snackbar.data_update_finished"))
                                        }
                                    }
                                    .onFailure {
                                        it.printStackTrace()
                                        launch {
                                            homeModel.snackbarHostState.showSnackbar("${i18n("setting.snackbar.data_update_failed")}: ${it.message}")
                                        }
                                    }
                                homeModel.waiting = false
                            }
                        },
                    )
                }
            }
            DropdownMenu(
                text = i18n(viewModel.versionUpdateSource.i18nKey),
                label = i18n("setting.version_update_source"),
            ) { onExpandedChange ->
                viewModel.versionUpdateSourceCandidates.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(i18n(item.i18nKey)) },
                        onClick = {
                            onExpandedChange(false)
                            viewModel.versionUpdateSource = item
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