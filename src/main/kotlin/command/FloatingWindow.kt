package cn.anling.inftool.command

import androidx.compose.runtime.remember
import androidx.compose.ui.window.application
import cn.anling.inftool.localization.LocalizationContext
import cn.anling.inftool.localization.defaultLocalizationContainers
import cn.anling.inftool.model.GameRecordsModel
import cn.anling.inftool.model.HomeModel
import cn.anling.inftool.setting.createPlatformSettings
import cn.anling.inftool.window.potentialWindow
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.float
import io.github.vinceglb.filekit.readString
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class FloatingWindowCommand : SuspendingCliktCommand("FloatingWindow") {
    val scale: Float by option(help = "悬浮窗缩放").float().default(1.0F)

    private val commandContext by requireObject<CommandContext>()

    override fun help(context: Context) = "启动潜力值悬浮窗"

    override suspend fun run() {
        val (username, save, experimentalLevel) = commandContext

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
                            useExperimentalLevel = model.settingModel.useExperimentalLevel,
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
                potentialWindow(
                    onCloseRequest = ::exitApplication,
                    scale = scale,
                    username = model.settingModel.username,
                    potential = gameRecordsModel.potential,
                    saveFile = model.settingModel.saveFile,
                    algorithm = model.settingModel.potentialAlgorithm,
                    useExperimentalLevel = model.settingModel.useExperimentalLevel,
                    addressContainer = runBlocking {
                        Json.decodeFromString(model.modulePotentialWindowModel.addressFile.readString())
                    }
                )
            }
        }
    }
}