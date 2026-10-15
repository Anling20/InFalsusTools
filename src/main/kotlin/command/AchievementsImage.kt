package cn.anling.inftool.command

import cn.anling.inftool.*
import cn.anling.inftool.Difficult.*
import cn.anling.inftool.localization.Language
import cn.anling.inftool.localization.LocalizationContext
import cn.anling.inftool.localization.defaultLocalizationContainers
import cn.anling.inftool.parser.SaveFileParser
import cn.anling.inftool.potential_algorithm.PotentialAlgorithm
import cn.anling.inftool.potential_algorithm.inFalsusTools
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
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.util.encodeToByteArray
import java.io.File

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
        val algorithm = PotentialAlgorithm.inFalsusTools
        val data = SaveFileParser.parse(PlatformFile(save))
        val highScores = data.toDiveRecordList().map { record ->
            record.process(
                highScores = data.highScores.map { it.toDiveRecord() },
                histories = data.history.map { it.toDiveRecord() },
                algorithm = algorithm,
                useExperimentalLevel = useExperimentalLevel,
            )
        }
        val histories = data.history.map { record ->
            record.toDiveRecord().process(
                highScores = data.highScores.map { it.toDiveRecord() },
                histories = data.history.map { it.toDiveRecord() },
                algorithm = algorithm,
                useExperimentalLevel = useExperimentalLevel,
            )
        }
        val potential = algorithm.calcPotential(highScores, histories)

        context(LocalizationContext(Language.English, defaultLocalizationContainers)) {
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
                    username = username,
                    potential = potential,
                    algorithm = algorithm,
                    highScores = highScores,
                    useExperimentalLevel = useExperimentalLevel,
                    min = min,
                    max = max,
                    difficulties = difficulties,
                )
            }
        }
    }
}