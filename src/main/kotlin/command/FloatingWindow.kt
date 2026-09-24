package cn.anling.inftool.command

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cn.anling.inftool.*
import cn.anling.inftool.parser.Parser
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.double
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.nio.file.FileSystems
import java.nio.file.StandardWatchEventKinds
import kotlin.time.Duration.Companion.milliseconds
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
            var windowVisible by remember { mutableStateOf(isFocusingGame()) }
            Window(
                onCloseRequest = ::exitApplication,
                state = windowState,
                visible = windowVisible,
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
                var visible by remember { mutableStateOf(windowVisible) }

                LaunchedEffect(Unit) {
                    launch(Dispatchers.IO) {
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

                    launch(Dispatchers.IO) {
                        while (true) {
                            val newVisible = isFocusingGame()
                            if (windowVisible != newVisible) {
                                visible = newVisible
                                window.isAlwaysOnTop = true
                                window.toFront()
                                delay(300.milliseconds)
                                windowVisible = newVisible
                            }
                            delay(200.milliseconds)
                        }
                    }
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(tween()) + fadeIn(tween()),
                    exit = slideOutVertically(tween()) + fadeOut(tween()),
                ) {
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
}

expect fun isFocusingGame(): Boolean