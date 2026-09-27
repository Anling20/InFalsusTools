package cn.anling.inftool.command

import androidx.compose.ui.renderComposeScene
import cn.anling.inftool.B50
import cn.anling.inftool.parser.SaveFileParser
import cn.anling.inftool.process
import cn.anling.inftool.toDiveRecordList
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import java.io.File

class B50Command : SuspendingCliktCommand("b50") {
    val out: File by option("-o", "--out", help = "输出图片路径").file().default(File("b50.jpg"))

    private val commandContext by requireObject<CommandContext>()

    override fun help(context: Context) = "生成 Best 50 图片"

    override suspend fun run() {
        val (username, save, experimentalLevel) = commandContext
        val data = SaveFileParser.parse(save.toPath()).toDiveRecordList().map { it.process(experimentalLevel) }

        renderComposeScene(1, 1) {
            B50(
                darkTheme = false,
                onSizeChanged = { size ->
                    val img = renderComposeScene(size.width, size.height) {
                        B50(
                            darkTheme = false,
                            onSizeChanged = {},
                            username = username,
                            data = data,
                        )
                    }
                    img.encodeToData()?.let { data ->
                        out.writeBytes(data.bytes)
                        println("B50 generated to " + out.absolutePath)
                    }
                },
                username = username,
                data = data,
            )
        }
    }
}