package cn.anling.inftool.command

import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.renderComposeScene
import cn.anling.inftool.B30
import cn.anling.inftool.copyImageToClipboard
import cn.anling.inftool.localization.Language
import cn.anling.inftool.localization.LocalizationContext
import cn.anling.inftool.localization.defaultLocalizationContainers
import cn.anling.inftool.parser.SaveFileParser
import cn.anling.inftool.process
import cn.anling.inftool.toDiveRecordList
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

class B50Command : SuspendingCliktCommand("b30") {
    val copyToClipboard: Boolean by option(help = "复制图片到剪贴板").flag()
    val out: File? by option("-o", "--out", help = "输出图片路径").file()
    val quality: Int by option(help = "图片质量").int().default(100)

    private val commandContext by requireObject<CommandContext>()

    override fun help(context: Context) = "生成 Best 30 图片"

    override suspend fun run() {
        val (username, save, experimentalLevel) = commandContext
        val data = SaveFileParser.parse(PlatformFile(save)).toDiveRecordList().map { it.process(experimentalLevel) }

        renderComposeScene(1, 1) {
            context(LocalizationContext(Language.English, defaultLocalizationContainers)) {
                B30(
                    darkTheme = false,
                    onSizeChanged = { size ->
                        val img = renderComposeScene(size.width, size.height) {
                            B30(
                                darkTheme = false,
                                onSizeChanged = {},
                                useNeoCard = false,
                                username = username,
                                highScores = data,
                            )
                        }
                        if (copyToClipboard) {
                            copyImageToClipboard(img.toComposeImageBitmap())
                            println("B30 generated to clipboard")
                        }
                        out?.let { out ->
                            img.encodeToData(
                                format = EncodedImageFormat.JPEG,
                                quality = quality,
                            )?.let { data ->
                                out.writeBytes(data.bytes)
                                println("B30 generated to " + out.absolutePath)
                            }
                        }
                    },
                    useNeoCard = false,
                    username = username,
                    highScores = data,
                )
            }
        }
    }
}