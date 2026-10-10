package cn.anling.inftool.command

import androidx.compose.ui.window.application
import cn.anling.inftool.WindowTranslation
import cn.anling.inftool.window.potentialWindow
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.float

class FloatingWindowCommand : SuspendingCliktCommand("FloatingWindow") {
    val scale: Float by option(help = "悬浮窗缩放").float().default(1.0F)

    private val commandContext by requireObject<CommandContext>()

    override fun help(context: Context) = "启动潜力值悬浮窗"

    override suspend fun run() {
        val (username, save, experimentalLevel) = commandContext

        application {
            potentialWindow(
                onCloseRequest = ::exitApplication,
                username = username,
                saveFile = save,
                useExperimentalLevel = experimentalLevel,
                scale = scale,
            )
        }
    }
}

expect fun isFocusingGame(): Boolean
expect fun getCursorPosition(): Pair<Int, Int>?
expect fun getFocusingGameTranslation(): WindowTranslation?