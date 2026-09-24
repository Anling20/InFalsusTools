package cn.anling.inftool.command

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.obj
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.use

data class CommandContext(
    val username: String,
    val save: File,
)

class DispatcherCommand : SuspendingCliktCommand("\"In Falsus Tools\"") {
    val username: String by option(help = "显示的用户名").default("Player")
    val save: File? by option(help = ".sav 存档文件的完整路径").file()

    override suspend fun run() {
        currentContext.obj = CommandContext(
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
            }
        )
    }
}