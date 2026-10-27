package cn.anling.inftool.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import cn.anling.infalsustools.generated.resources.Res
import cn.anling.infalsustools.generated.resources.folder_open
import cn.anling.inftool.component.ClickableTextField
import cn.anling.inftool.component.DropdownMenu
import cn.anling.inftool.localization.Language
import cn.anling.inftool.localization.LocalizationContext
import cn.anling.inftool.localization.currentLanguage
import cn.anling.inftool.localization.i18n
import cn.anling.inftool.potential_algorithm.InFalsusToolsPotentialAlgorithm
import cn.anling.inftool.potential_algorithm.PotentialAlgorithm
import cn.anling.inftool.readStringSync
import cn.anling.inftool.setting.toComposeStated
import cn.anling.inftool.song.BuiltinConstantTable
import cn.anling.inftool.song.ConstantTable
import cn.anling.inftool.song.ExternalConstantTable
import cn.anling.inftool.unfocusOnPressEnter
import com.russhwolf.settings.Settings
import com.russhwolf.settings.int
import com.russhwolf.settings.nullableString
import com.russhwolf.settings.string
import io.github.vinceglb.filekit.*
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.painterResource

object SettingPage {
    class ViewModel(
        settings: Settings,
        initialSaveFile: String,
    ) {
        var username by settings.string("Setting.username", "Player").toComposeStated()

        private var _saveFile by settings.string("Setting.saveFile", initialSaveFile).toComposeStated()
        var saveFile: PlatformFile
            get() = PlatformFile(_saveFile)
            set(value) {
                _saveFile = value.absolutePath()
            }

        var constantTableString by settings.string("Setting.constantTable", "builtin:vanilla").toComposeStated()
        val constantTable: ConstantTable
            get() {
                val (type, content) = constantTableString.split(":", limit = 2)
                return runCatching {
                    when (type) {
                        "builtin" -> when (content) {
                            "vanilla" -> BuiltinConstantTable.Vanilla
                            "experimental" -> BuiltinConstantTable.Experimental
                            else -> error("Unknown builtin constant table: $content")
                        }

                        "file" -> {
                            val file = PlatformFile(content)
                            when (file.exists() && file.isRegularFile()) {
                                true -> ExternalConstantTable.File(Json.decodeFromString(file.readStringSync()))
                                else -> error("File not found or is directory: $content")
                            }
                        }

                        else -> error("Unknown constant table type: $constantTableString")
                    }
                }.getOrElse { throwable ->
                    println(throwable.message)
                    throwable.printStackTrace()
                    constantTableString = "builtin:vanilla"
                    BuiltinConstantTable.Vanilla
                }
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

        private var _rankingFile by settings.nullableString("Settings.rankingFile").toComposeStated()
        var rankingFile: PlatformFile?
            get() = _rankingFile?.let { PlatformFile(it) }
            set(value) {
                _rankingFile = value?.absolutePath()
            }

        private var languageIndex by settings.int("Setting.language", 1).toComposeStated()
        var language: Language
            get() = Language.entries[languageIndex]
            set(value) {
                languageIndex = value.ordinal
            }
    }

    @Composable
    context(viewModel: ViewModel, _: LocalizationContext)
    fun Page(
        onUpdateGameRecords: suspend () -> Unit,
    ) = Content(
        viewModel = viewModel,
        onUpdateGameRecords = onUpdateGameRecords,
    )

    @Composable
    context(_: LocalizationContext)
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
                text = when (viewModel.constantTable) {
                    BuiltinConstantTable.Vanilla -> i18n("setting.constant_table.vanilla")
                    BuiltinConstantTable.Experimental -> i18n("setting.constant_table.experimental")
                    is ExternalConstantTable -> buildString {
                        val (type, content) = viewModel.constantTableString.split(":", limit = 2)
                        append(i18n("setting.constant_table.$type"))
                        append(": ")
                        append(content)
                    }
                },
                label = i18n("setting.constant_table"),
            ) { onExpandedChange ->
                DropdownMenuItem(
                    text = { Text(i18n("setting.constant_table.vanilla")) },
                    onClick = {
                        onExpandedChange(false)
                        viewModel.constantTableString = "builtin:vanilla"
                    },
                )
                DropdownMenuItem(
                    text = { Text(i18n("setting.constant_table.experimental")) },
                    onClick = {
                        onExpandedChange(false)
                        viewModel.constantTableString = "builtin:experimental"
                    },
                )
                DropdownMenuItem(
                    text = { Text(i18n("setting.constant_table.file")) },
                    onClick = {
                        onExpandedChange(false)
                        scope.launch {
                            val file = FileKit.openFilePicker(
                                type = FileKitType.File("json"),
                                mode = FileKitMode.Single,
                            )
                            if (file != null) {
                                viewModel.constantTableString = "file:${file.absolutePath()}"
                                withContext(Dispatchers.IO) {
                                    onUpdateGameRecords()
                                }
                            }
                        }
                    },
                )
            }
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
            ClickableTextField(
                value = viewModel.rankingFile?.absolutePath() ?: "",
                label = { Text(i18n("setting.ranking_file")) },
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
                        type = FileKitType.File("json"),
                        mode = FileKitMode.Single,
                        directory = viewModel.rankingFile?.parent()?.let {
                            when (it.exists() && it.isDirectory()) {
                                true -> it
                                else -> null
                            }
                        },
                    )
                    if (file != null) {
                        viewModel.rankingFile = file
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
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