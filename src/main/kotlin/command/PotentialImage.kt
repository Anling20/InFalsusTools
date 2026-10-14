package cn.anling.inftool.command

import androidx.compose.ui.graphics.toComposeImageBitmap
import cn.anling.inftool.*
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
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import io.github.vinceglb.filekit.PlatformFile
import org.jetbrains.skia.EncodedImageFormat
import java.io.File

class PotentialImageCommand : SuspendingCliktCommand("potential") {
    val copyToClipboard: Boolean by option(help = "复制图片到剪贴板").flag()
    val out: File? by option("-o", "--out", help = "输出图片路径").file()
    val quality: Int by option(help = "图片质量").int().default(100)

    private val commandContext by requireObject<CommandContext>()

    override fun help(context: Context) = "生成 Best 30 图片"

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
        val contents = algorithm.contents(highScores, histories)

        context(LocalizationContext(Language.English, defaultLocalizationContainers)) {
            val img = renderComposable {
                PotentialImageContent(
                    scale = 1.0F,
                    useNeoCard = false,
                    username = username,
                    potential = potential,
                    algorithm = algorithm,
                    contents = contents,
                )
            }

            if (copyToClipboard) {
                copyImageToClipboard(img.toComposeImageBitmap())
                println("Potential generated to clipboard")
            }
            out?.let { out ->
                img.encodeToData(
                    format = EncodedImageFormat.JPEG,
                    quality = quality,
                )?.let { data ->
                    out.writeBytes(data.bytes)
                    println("Potential generated to " + out.absolutePath)
                }
            }
        }
    }
}