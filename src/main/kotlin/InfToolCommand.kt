package cn.anling.inftool

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.renderComposeScene
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cn.anling.inftool.parser.Parser
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.file
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import kotlin.io.path.absolutePathString
import kotlin.time.Duration.Companion.seconds

class InfToolCommand : SuspendingCliktCommand() {
    val username: String by option(help = "B50 及潜力值悬浮窗显示的用户名").default("Player")
    val save: File? by option(help = ".sav 存档文件的完整路径").file()
    val out: File by option(help = "输出图片路径").file().default(File("output.jpg"))
    val floatingWindow: Boolean by option(help = "启动潜力值悬浮窗").flag()
    val floatingWindowScale: Double by option(help = "悬浮窗缩放").double().default(1.0)

    override suspend fun run() {
        val saveFile = save ?: run {
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

        val data = Parser(saveFile.readBytesWhenAvailable()).parse().map { it.toDiveRecord().process() }

        when (floatingWindow) {
            true -> application {
                val windowState = rememberWindowState(
                    position = WindowPosition(Alignment.TopCenter),
                    size = DpSize(
                        width = (768 * floatingWindowScale).dp,
                        height = (128 * floatingWindowScale).dp,
                    ),
                )
                Window(
                    onCloseRequest = ::exitApplication,
                    state = windowState,
                    undecorated = true,
                    transparent = true,
                    resizable = false,
                    focusable = false,
                    alwaysOnTop = true,
                ) {
                    var newData by remember { mutableStateOf(data) }
                    var additionPotential by remember { mutableStateOf<Double?>(null) }
                    var playPotential by remember { mutableStateOf<Double?>(null) }

                    var potential by remember { mutableDoubleStateOf(newData.potential()) }

                    LaunchedEffect(Unit) {
                        withContext(Dispatchers.IO) {
                            val watchService = FileSystems.getDefault().newWatchService()
                            val pathKey = saveFile.toPath().parent.register(
                                watchService,
                                StandardWatchEventKinds.ENTRY_MODIFY,
                            )

                            while (true) {
                                val watchKey = watchService.take()

                                for (event in watchKey.pollEvents()) {
                                    val oldData = newData

                                    newData = Parser(saveFile.readBytesWhenAvailable()).parse()
                                        .map { it.toDiveRecord().process() }
                                    val oldPtt = oldData.potential()
                                    val newPtt = newData.potential()
                                    if (newPtt > oldPtt) {
                                        potential = newPtt
                                        additionPotential = newPtt - oldPtt
                                        delay(5.seconds)
                                        additionPotential = null
                                    }
                                }

                                if (!watchKey.reset()) {
                                    watchKey.cancel()
                                    watchService.close()
                                    break
                                }
                            }

                            pathKey.cancel()
                        }
                    }

                    PotentialApp(
                        scale = floatingWindowScale,
                        username = username,
                        potential = potential,
                        additionPotential = additionPotential,
                        playPotential = playPotential,
                    )
                }
            }

            else -> renderComposeScene(1, 1) {
                App(
                    onSizeChanged = { size ->
                        val img = renderComposeScene(size.width, size.height) {
                            App(
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
}