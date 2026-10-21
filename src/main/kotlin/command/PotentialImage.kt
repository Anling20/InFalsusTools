package cn.anling.inftool.command

import cn.anling.inftool.PotentialImageContent
import cn.anling.inftool.copyImageToClipboard
import cn.anling.inftool.localization.LocalizationContext
import cn.anling.inftool.localization.defaultLocalizationContainers
import cn.anling.inftool.model.GameRecordsModel
import cn.anling.inftool.model.HomeModel
import cn.anling.inftool.renderComposable
import cn.anling.inftool.setting.createPlatformSettings
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import io.github.vinceglb.filekit.ImageFormat
import io.github.vinceglb.filekit.dialogs.compose.util.encodeToByteArray
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class PotentialImageCommand : SuspendingCliktCommand("potential") {
    val copyToClipboard: Boolean by option(help = "复制图片到剪贴板").flag()
    val out: File? by option("-o", "--out", help = "输出图片路径").file()
    val quality: Int by option(help = "图片质量").int().default(100)

    private val commandContext by requireObject<CommandContext>()

    override fun help(context: Context) = "生成 Best 30 图片"

    override suspend fun run() {
        val (username, save, useExperimentalLevel) = commandContext

        val model = HomeModel(
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
        val gameRecordsModel = GameRecordsModel().apply {
            update(
                saveFile = model.settingModel.saveFile,
                algorithm = model.settingModel.potentialAlgorithm,
                levelMap = model.settingModel.levelMap,
            )
        }
        val localizationContext = LocalizationContext(
            language = model.settingModel.language,
            containers = defaultLocalizationContainers,
        )

        context(model, gameRecordsModel, localizationContext) {
            renderComposable(
                onRendered = { imageBitmap ->
                    if (copyToClipboard) {
                        copyImageToClipboard(imageBitmap)
                        println("Potential generated to clipboard")
                    }
                    out?.let { out ->
                        val byteArray = imageBitmap.encodeToByteArray(
                            format = ImageFormat.JPEG,
                            quality = quality,
                        )
                        out.writeBytes(byteArray)
                        println("Potential generated to ${out.absolutePath}")
                    }
                }
            ) {
                PotentialImageContent(
                    useNeoCard = false,
                    username = model.settingModel.username,
                    potential = gameRecordsModel.potential,
                    algorithm = model.settingModel.potentialAlgorithm,
                    contents = model.settingModel.potentialAlgorithm.contents(
                        histories = gameRecordsModel.historyRecords,
                        levelMap = model.settingModel.levelMap,
                    ),
                )
            }
        }
    }
}