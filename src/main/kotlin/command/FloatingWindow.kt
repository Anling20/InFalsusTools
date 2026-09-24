package cn.anling.inftool.command

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cn.anling.inftool.FloatingWindow
import cn.anling.inftool.parser.Parser
import cn.anling.inftool.potential
import cn.anling.inftool.process
import cn.anling.inftool.readBytesWhenAvailable
import cn.anling.inftool.toDiveRecord
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.double
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.FileSystems
import java.nio.file.StandardWatchEventKinds
import kotlin.time.Duration.Companion.seconds

class FloatingWindowCommand : SuspendingCliktCommand("FloatingWindow") {
    val scale: Double by option(help = "悬浮窗缩放").double().default(1.0)

    private val commandContext by requireObject<CommandContext>()

    override fun help(context: Context) = "启动潜力值悬浮窗"

    override suspend fun run() {
        val (username, save) = commandContext
        val data = Parser(save.readBytesWhenAvailable()).parse().map { it.toDiveRecord().process() }

        application {
            val windowState = rememberWindowState(
                position = WindowPosition(Alignment.TopCenter),
                size = DpSize(
                    width = (768 * scale).dp,
                    height = (128 * scale).dp,
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
                        launch {
                            val watchService = FileSystems.getDefault().newWatchService()
                            val pathKey = save.toPath().parent.register(
                                watchService,
                                StandardWatchEventKinds.ENTRY_MODIFY,
                            )

                            while (true) {
                                val watchKey = watchService.take()

                                for (event in watchKey.pollEvents()) {
                                    val oldData = newData

                                    newData = Parser(save.readBytesWhenAvailable()).parse()
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

                        launch {
                            while (true) {
                                window.toFront()
                                delay(1.seconds)
                            }
                        }
                    }
                }

                FloatingWindow(
                    scale = scale,
                    username = username,
                    potential = potential,
                    additionPotential = additionPotential,
                    playPotential = playPotential,
                )
            }
        }
    }
}