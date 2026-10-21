package cn.anling.inftool.command

import cn.anling.inftool.*
import cn.anling.inftool.Difficult.*
import cn.anling.inftool.localization.LocalizationContext
import cn.anling.inftool.localization.defaultLocalizationContainers
import cn.anling.inftool.model.GameRecordsModel
import cn.anling.inftool.model.HomeModel
import cn.anling.inftool.setting.createPlatformSettings
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import io.github.vinceglb.filekit.ImageFormat
import io.github.vinceglb.filekit.dialogs.compose.util.encodeToByteArray
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class AchievementsImageCommand : SuspendingCliktCommand("achievements") {
    val copyToClipboard: Boolean by option(help = "复制图片到剪贴板").flag()
    val out: File? by option("-o", "--out", help = "输出图片路径").file()
    val quality: Int by option(help = "图片质量").int().default(100)
    val min: Double? by option(help = "限制最小定数").double()
    val max: Double? by option(help = "限制最大定数").double()
    val difficulties: List<Difficult> by option("--difficult", help = "限制难度(MIN/EVO/ULT/FBD)")
        .choice("MIN" to MIN, "EVO" to EVO, "ULT" to ULT, "FBD" to FBD, ignoreCase = true)
        .multiple()

    private val commandContext by requireObject<CommandContext>()

    override fun help(context: Context) = "生成铺面达成情况"

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
                        println("Achievements generated to clipboard")
                    }

                    out?.let { out ->
                        val byteArray = imageBitmap.encodeToByteArray(
                            format = ImageFormat.JPEG,
                            quality = quality,
                        )
                        out.writeBytes(byteArray)
                        println("Achievements generated to ${out.absolutePath}")
                    }
                }
            ) {
                AchievementsImageContent(
                    useNeoCard = false,
                    username = model.settingModel.username,
                    potential = gameRecordsModel.potential,
                    algorithm = model.settingModel.potentialAlgorithm,
                    highScores = gameRecordsModel.historyRecords.findHighScores(),
                    levelMap = model.settingModel.levelMap,
                    min = min,
                    max = max,
                    difficulties = difficulties,
                )
            }
        }
    }
}