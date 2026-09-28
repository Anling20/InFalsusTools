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
import cn.anling.inftool.parser.SaveFileParser
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class FloatingWindowCommand : SuspendingCliktCommand("FloatingWindow") {
    val scale: Double by option(help = "悬浮窗缩放").double().default(1.0)
    val timeout: Int by option(help = "设置结算信息显示时长(seconds)").int().default(7)

    private val commandContext by requireObject<CommandContext>()
    private val pollInterval: Duration = 500.milliseconds

    override fun help(context: Context) = "启动潜力值悬浮窗"

    override suspend fun run() {
        val (username, save, experimentalLevel) = commandContext

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
                var parsedData by remember {
                    runBlocking {
                        mutableStateOf(SaveFileParser.parse(save.toPath()))
                    }
                }
                var data by remember {
                    runBlocking {
                        mutableStateOf(parsedData.toDiveRecordList().map { it.process(experimentalLevel) })
                    }
                }
                var additionPotential by remember { mutableStateOf<Double?>(null) }

                var potential by remember { mutableDoubleStateOf(data.potential()) }
                var playingGame by remember { mutableStateOf(false) }
                var focusingGame by remember { mutableStateOf(windowVisible) }
                var cursorCovered by remember { mutableStateOf(false) }
                var visible by remember { mutableStateOf(windowVisible) }

                LaunchedEffect(Unit) {
                    // Window position change
                    launch(Dispatchers.IO) {
                        while (true) {
                            if (focusingGame) {
                                val (position, size) = getFocusingGameTranslation() ?: continue
                                val (x, y) = position
                                val (width) = size
                                val centeredX = x + (width / 2) - (window.size.width / 2)
                                windowState.position = WindowPosition(centeredX.dp, y.dp)
                            }
                            delay(pollInterval)
                        }
                    }

                    // Focus detect
                    launch(Dispatchers.IO) {
                        while (true) {
                            focusingGame = isFocusingGame()
                            val newVisible = focusingGame && !playingGame && !cursorCovered
                            if (visible != newVisible) {
                                val oldVisible = visible
                                if (!oldVisible) {
                                    windowVisible = true
                                }
                                visible = newVisible
                                window.isAlwaysOnTop = true
                                window.toFront()
                                if (oldVisible) {
                                    delay(300.milliseconds)
                                    windowVisible = false
                                }
                            }
                            delay(pollInterval)
                        }
                    }

                    // Play detect
                    launch(Dispatchers.IO) {
                        GameSceneMonitor(
                            scope = this,
                            onGameStarted = {
                                playingGame = true
                                if (visible) {
                                    visible = false
                                    window.isAlwaysOnTop = true
                                    window.toFront()
                                    delay(300.milliseconds)
                                    windowVisible = false
                                }
                            },
                            onGameExited = {
                                playingGame = false
                                val newVisible = focusingGame && !cursorCovered
                                if (visible != newVisible) {
                                    windowVisible = true
                                    visible = true
                                    window.isAlwaysOnTop = true
                                    window.toFront()
                                }

                                val oldParsedData = parsedData
                                val oldData = data

                                parsedData = SaveFileParser.parse(save.toPath())
                                data = parsedData.toDiveRecordList().map { it.process(experimentalLevel) }

                                if (parsedData.history != oldParsedData.history) {
                                    val oldPtt = oldData.potential()
                                    val newPtt = data.potential()

                                    potential = newPtt
                                    additionPotential = newPtt - oldPtt

                                    delay(timeout.seconds)
                                    additionPotential = null
                                }
                            },
                            pollInterval = pollInterval,
                        ).start()
                    }

                    // Mouse overlay detect
                    launch(Dispatchers.IO) {
                        while (true) {
                            val (cursorPositionX, cursorPositionY) = getCursorPosition()
                            val (windowPositionX, windowPositionY) = Pair(window.x, window.y)
                            val (windowSizeWidth, windowSizeHeight) = Pair(window.size.width, window.size.height)
                            val cursorCoveredX = cursorPositionX in windowPositionX..(windowPositionX + windowSizeWidth)
                            val cursorCoveredY = cursorPositionY in windowPositionY..(windowPositionY + windowSizeHeight)
                            cursorCovered = cursorCoveredX && cursorCoveredY
                            val newVisible = focusingGame && !playingGame && !cursorCovered
                            if (visible != newVisible) {
                                val oldVisible = visible
                                if (!oldVisible) {
                                    windowVisible = true
                                }
                                visible = newVisible
                                window.isAlwaysOnTop = true
                                window.toFront()
                                if (oldVisible) {
                                    delay(300.milliseconds)
                                    windowVisible = false
                                }
                            }
                            delay(pollInterval)
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
                    )
                }
            }
        }
    }
}

expect fun isFocusingGame(): Boolean
expect fun getCursorPosition(): Pair<Int, Int>
expect fun getFocusingGameTranslation(): WindowTranslation?