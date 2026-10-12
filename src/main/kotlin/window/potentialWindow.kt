package cn.anling.inftool.window

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import cn.anling.inftool.*
import cn.anling.inftool.command.getCursorPosition
import cn.anling.inftool.command.getFocusingGameTranslation
import cn.anling.inftool.command.isFocusingGame
import cn.anling.inftool.localization.LocalizationContext
import cn.anling.inftool.model.GameRecordsModel
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private const val animationTime = 50

@Composable
context(model: GameRecordsModel, _: LocalizationContext)
fun potentialWindow(
    onCloseRequest: () -> Unit,
    username: String,
    saveFile: PlatformFile,
    useExperimentalLevel: Boolean,
    scale: Float,
    pollInterval: Duration = 500.milliseconds,
) {
    val windowState = rememberWindowState(
        position = WindowPosition(Alignment.TopCenter),
        size = DpSize(
            width = (768 * scale).dp,
            height = (128 * scale).dp,
        ),
    )
    var windowVisible by remember { mutableStateOf(false) }
    Window(
        onCloseRequest = onCloseRequest,
        state = windowState,
        visible = windowVisible,
        undecorated = true,
        transparent = true,
        resizable = false,
        focusable = false,
        alwaysOnTop = true,
    ) {
        var playResult by remember { mutableStateOf<PlayResult?>(null) }

        var potential by remember { mutableDoubleStateOf(model.highScoreRecords.potential().floor(3)) }
        var playingGame by remember { mutableStateOf(false) }
        var focusingGame by remember { mutableStateOf(false) }
        var cursorCovered by remember { mutableStateOf(false) }
        var visible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            // Window position change
            launch(Dispatchers.IO) {
                while (true) {
                    if (visible) {
                        val transform = window.graphicsConfiguration.defaultTransform
                        val (position, size) = getFocusingGameTranslation() ?: continue
                        val x = (position.first / transform.scaleX).toInt()
                        val y = (position.second / transform.scaleY).toInt()
                        val width = (size.first / transform.scaleX).toInt()
                        val centeredX = x + (width / 2) - (window.size.width / 2)
                        window.setLocation(centeredX, y)
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
                            delay(animationTime.milliseconds)
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
                            delay(animationTime.milliseconds)
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
                    },
                    onResultsStarted = {
                        val oldPtt = model.highScoreRecords.toList().potential()

                        model.update(saveFile, useExperimentalLevel)

                        val newPtt = model.highScoreRecords.potential()
                        potential = newPtt.floor(3)
                        val deltaPtt = newPtt.floor(3) - oldPtt.floor(3)

                        val recent = model.historyRecords.last()
                        playResult = PlayResult(
                            level = recent.level,
                            potential = recent.potential,
                            additionPotential = deltaPtt
                        )
                        println("Play result: ${recent.potential}, potential change: $oldPtt -> $newPtt, delta: $deltaPtt")
                    },
                    onResultsExited = {
                        if (playResult != null) {
                            playResult = null
                        }
                    },
                    pollInterval = pollInterval,
                ).start()
            }

            // Mouse overlay detect
            launch(Dispatchers.IO) {
                while (true) {
                    val cursorPos = getCursorPosition()
                    val (windowPosX, windowPosY) = Pair(window.x, window.y)
                    val (windowSizeWidth, windowSizeHeight) = Pair(window.size.width, window.size.height)
                    val cursorCoveredX = cursorPos?.first in windowPosX..(windowPosX + windowSizeWidth)
                    val cursorCoveredY = cursorPos?.second in windowPosY..(windowPosY + windowSizeHeight)
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
                            delay(animationTime.milliseconds)
                            windowVisible = false
                        }
                    }
                    delay(100.milliseconds)
                }
            }
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(tween(animationTime)) + fadeIn(tween(animationTime)),
            exit = slideOutVertically(tween(animationTime)) + fadeOut(tween(animationTime)),
        ) {
            FloatingWindow(
                scale = scale,
                username = username,
                potential = potential,
                playResult = playResult,
            )
        }
    }
}