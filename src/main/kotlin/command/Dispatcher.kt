package cn.anling.inftool.command

import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cn.anling.inftool.page.Home
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.obj
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
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
            val (username, save, experimentalLevel) = commandContext
            application {
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
                    resizable = false,
                ) {
                    Home(
                        onMinimum = { windowState.isMinimized = true },
                        onQuit = ::exitApplication,
                        windowDraggableArea = { WindowDraggableArea(content = it) },
                    )
                }
            }
        }
    }
}