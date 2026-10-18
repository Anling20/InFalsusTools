package cn.anling.inftool.command

import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cn.anling.inftool.AchievementsImage
import cn.anling.inftool.Difficult
import cn.anling.inftool.PotentialImage
import cn.anling.inftool.localization.LocalizationContext
import cn.anling.inftool.localization.defaultLocalizationContainers
import cn.anling.inftool.model.GameRecordsModel
import cn.anling.inftool.model.HomeModel
import cn.anling.inftool.page.Home
import cn.anling.inftool.setting.createPlatformSettings
import cn.anling.inftool.window.potentialWindow
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.obj
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import io.github.vinceglb.filekit.readString
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString

data class CommandContext(
    val username: String,
    val save: File,
    val experimentalLevel: Boolean,
)

class DispatcherCommand : SuspendingCliktCommand("\"In Falsus Tools\"") {
    val username: String by option(help = "显示的用户名").default("Player")
    val save: File? by option(help = ".sav 存档文件的完整路径").file()
    val experimentalLevel: Boolean by option(help = "使用实验性的精确定数").flag()

    override val invokeWithoutSubcommand = true

    override suspend fun run() {
        val commandContext = CommandContext(
            username = username,
            save = save ?: run {
                val userHome = System.getProperty("user.home")
                val root = Path.of(userHome, "AppData", "LocalLow", "lowiro", "infalsus")
                Files.list(root).use { steamIdDirs ->
                    steamIdDirs
                        .filter { Files.isDirectory(it) }
                        .map { it.resolve("release/savestate_V3.sav") }
                        .filter { Files.isRegularFile(it) }
                        .toList()
                        .first()
                        .absolutePathString()
                }.let { File(it) }
            },
            experimentalLevel = experimentalLevel,
        )
        currentContext.obj = commandContext

        if (currentContext.invokedSubcommand == null) {
            application {
                val model = remember(Unit) {
                    HomeModel(
                        settings = createPlatformSettings(),
                        initialSaveFile = run {
                            val userHome = System.getProperty("user.home")
                            val root = Path.of(userHome, "AppData", "LocalLow", "lowiro", "infalsus")
                            Files.list(root).use { steamIdDirs ->
                                steamIdDirs
                                    .filter { Files.isDirectory(it) }
                                    .map { it.resolve("release/savestate_V3.sav") }
                                    .filter { Files.isRegularFile(it) }
                                    .toList()
                                    .first()
                                    .absolutePathString()
                            }
                        },
                    )
                }
                val gameRecordsModel = remember(Unit) {
                    GameRecordsModel().apply {
                        runBlocking {
                            update(
                                saveFile = model.settingModel.saveFile,
                                algorithm = model.settingModel.potentialAlgorithm,
                                levelMap = when (model.settingModel.useExperimentalLevel) {
                                    true -> model.settingModel.experimentalLevels
                                    else -> model.settingModel.vanillaLevels
                                },
                            )
                        }
                    }
                }
                val localizationContext = remember(Unit) {
                    LocalizationContext(
                        language = model.settingModel.language,
                        containers = defaultLocalizationContainers,
                    )
                }
                context(model, gameRecordsModel, localizationContext) {
                    val windowState = rememberWindowState(
                        size = DpSize(
                            width = 1280.dp,
                            height = 900.dp,
                        ),
                    )
                    Window(
                        onCloseRequest = ::exitApplication,
                        state = windowState,
                        undecorated = true,
                        transparent = true,
                    ) {
                        Home(
                            onMinimum = { windowState.isMinimized = true },
                            onQuit = ::exitApplication,
                            windowDraggableArea = { WindowDraggableArea(content = it) },
                        )
                    }

                    if (model.startedPotentialWindow) {
                        potentialWindow(
                            onCloseRequest = { model.startedPotentialWindow = false },
                            scale = model.modulePotentialWindowModel.scale,
                            username = model.settingModel.username,
                            potential = gameRecordsModel.potential,
                            saveFile = model.settingModel.saveFile,
                            algorithm = model.settingModel.potentialAlgorithm,
                            levelMap = when (model.settingModel.useExperimentalLevel) {
                                true -> model.settingModel.experimentalLevels
                                else -> model.settingModel.vanillaLevels
                            },
                            addressContainer = runBlocking {
                                Json.decodeFromString(model.modulePotentialWindowModel.addressFile.readString())
                            },
                            onlyDisplayInResultsScene = model.modulePotentialWindowModel.onlyDisplayInResultsScene,
                        )
                    }

                    if (model.displayingB30Preview) {
                        Window(
                            onCloseRequest = { model.displayingB30Preview = false },
                            state = rememberWindowState(),
                            title = "Best30 Preview",
                        ) {
                            PotentialImage(
                                useNeoCard = model.modulePotentialImageModel.useNeoCard,
                                username = model.settingModel.username,
                                potential = gameRecordsModel.potential,
                                algorithm = model.settingModel.potentialAlgorithm,
                                contents = model.settingModel.potentialAlgorithm.contents(
                                    highScores = gameRecordsModel.highScoreRecords,
                                    histories = gameRecordsModel.historyRecords,
                                ),
                            )
                        }
                    }

                    if (model.displayingAchievementsPreview) {
                        Window(
                            onCloseRequest = { model.displayingAchievementsPreview = false },
                            state = rememberWindowState(),
                            title = "Achievements Preview",
                        ) {
                            val difficulties = buildList {
                                if (model.moduleAchievementsImageModel.minDifficultSelect) {
                                    add(Difficult.MIN)
                                }
                                if (model.moduleAchievementsImageModel.evoDifficultSelect) {
                                    add(Difficult.EVO)
                                }
                                if (model.moduleAchievementsImageModel.ultDifficultSelect) {
                                    add(Difficult.ULT)
                                }
                                if (model.moduleAchievementsImageModel.fbdDifficultSelect) {
                                    add(Difficult.FBD)
                                }
                            }
                            AchievementsImage(
                                useNeoCard = model.moduleAchievementsImageModel.useNeoCard,
                                username = model.settingModel.username,
                                potential = gameRecordsModel.potential,
                                algorithm = model.settingModel.potentialAlgorithm,
                                highScores = gameRecordsModel.highScoreRecords,
                                levelMap = when (model.settingModel.useExperimentalLevel) {
                                    true -> model.settingModel.experimentalLevels
                                    else -> model.settingModel.vanillaLevels
                                },
                                min = model.moduleAchievementsImageModel.minLevelLimit,
                                max = model.moduleAchievementsImageModel.maxLevelLimit,
                                difficulties = difficulties,
                            )
                        }
                    }
                }
            }
        }
    }
}