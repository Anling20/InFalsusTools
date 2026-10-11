package cn.anling.inftool.command

import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.renderComposeScene
import cn.anling.inftool.Ahv
import cn.anling.inftool.Difficult
import cn.anling.inftool.Difficult.*
import cn.anling.inftool.copyImageToClipboard
import cn.anling.inftool.parser.SaveFileParser
import cn.anling.inftool.process
import cn.anling.inftool.toDiveRecordList
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
import io.github.vinceglb.filekit.PlatformFile
import org.jetbrains.skia.EncodedImageFormat
import java.io.File

class AhvCommand : SuspendingCliktCommand("ahv") {
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
        val (username, save, experimentalLevel) = commandContext
        val data = SaveFileParser.parse(PlatformFile(save)).toDiveRecordList().map { it.process(experimentalLevel) }

        renderComposeScene(1, 1) {
            Ahv(
                darkTheme = false,
                onSizeChanged = { size ->
                    val img = renderComposeScene(size.width, size.height) {
                        Ahv(
                            darkTheme = false,
                            onSizeChanged = {},
                            username = username,
                            data = data,
                            experimentalLevel = experimentalLevel,
                            min = min,
                            max = max,
                            difficulties = difficulties,
                        )
                    }
                    if (copyToClipboard) {
                        copyImageToClipboard(img.toComposeImageBitmap())
                        println("Ahv generated to clipboard")
                    }
                    out?.let { out ->
                        img.encodeToData(
                            format = EncodedImageFormat.JPEG,
                            quality = quality,
                        )?.let { data ->
                            out.writeBytes(data.bytes)
                            println("Ahv generated to " + out.absolutePath)
                        }
                    }
                },
                username = username,
                data = data,
                experimentalLevel = experimentalLevel,
                min = min,
                max = max,
                difficulties = difficulties,
            )
        }
    }
}