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
import cn.anling.inftool.localization.LocalizationContext
import cn.anling.inftool.model.GameRecordsModel
import cn.anling.inftool.model.SettingModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private const val animationTime = 50

@Composable
context(model: SettingModel, gameRecordsModel: GameRecordsModel, _: LocalizationContext)
fun potentialWindow(
    onCloseRequest: () -> Unit,
    scale: Float,
    addressContainer: AddressContainer,
    onlyDisplayInResultsScene: Boolean,
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

        var playingGame by remember { mutableStateOf(false) }
        var focusingGame by remember { mutableStateOf(false) }
        var cursorCovered by remember { mutableStateOf(false) }
        var inResultsScene by remember { mutableStateOf(false) }
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
                    focusingGame = getFocusingWindowPID() in listGamesPID()
                    val inResultsSceneVisible = when (onlyDisplayInResultsScene) {
                        true -> inResultsScene
                        else -> true
                    }
                    val newVisible = focusingGame && !playingGame && !cursorCovered && inResultsSceneVisible
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

            // Scene detect
            launch(Dispatchers.IO) {
                val onGameSceneEntered = suspend {
                    playingGame = true
                    if (visible) {
                        visible = false
                        window.isAlwaysOnTop = true
                        window.toFront()
                        delay(animationTime.milliseconds)
                        windowVisible = false
                    }
                }
                val onGameSceneExited = suspend {
                    playingGame = false
                    val inResultsSceneVisible = when (onlyDisplayInResultsScene) {
                        true -> inResultsScene
                        else -> true
                    }
                    val newVisible = focusingGame && !cursorCovered && inResultsSceneVisible
                    if (visible != newVisible) {
                        windowVisible = true
                        visible = true
                        window.isAlwaysOnTop = true
                        window.toFront()
                    }



                    val oldHistories = gameRecordsModel.historyRecords.toList()
                    val oldPtt = model.potentialAlgorithm.calcPotential(
                        highScores = gameRecordsModel.highScoreRecords,
                        histories = gameRecordsModel.historyRecords,
                    )

                    gameRecordsModel.update(
                        saveFile = model.saveFile,
                        algorithm = model.potentialAlgorithm,
                        levelMap = when (model.useExperimentalLevel) {
                            true -> model.experimentalLevels
                            else -> model.vanillaLevels
                        },
                    )

                    val newPtt = model.potentialAlgorithm.calcPotential(
                        highScores = gameRecordsModel.highScoreRecords,
                        histories = gameRecordsModel.historyRecords,
                    )
                    val deltaPtt = newPtt.floor(3) - oldPtt.floor(3)

                    if (gameRecordsModel.historyRecords.lastOrNull() != oldHistories.lastOrNull() || deltaPtt > 0.0) {
                        val recent = gameRecordsModel.historyRecords.last()
                        playResult = PlayResult(
                            level = recent.level,
                            potential = recent.potential,
                            additionPotential = deltaPtt
                        )
                        println("Play result: ${recent.potential}, potential change: $oldPtt -> $newPtt, delta: $deltaPtt")
                    }
                }
                val onResultsSceneEntered = suspend {
                    inResultsScene = true
                    playingGame = false
                    val newVisible = focusingGame && !cursorCovered
                    if (visible != newVisible) {
                        windowVisible = true
                        visible = true
                        window.isAlwaysOnTop = true
                        window.toFront()
                    }
                }
                val onResultsSceneExited = suspend {
                    inResultsScene = false
                    if (playResult != null) {
                        playResult = null
                    }
                }
                val processMemoryMap = mutableMapOf<Long, ProcessMemory>()
                var latestFocusingGame: Long? = null
                var latestInGameScene = false
                var latestInResultsScene = false
                while (true) {
                    processMemoryMap.iterator().let { iterator ->
                        while (iterator.hasNext()) {
                            val (pid, processMemory) = iterator.next()

                            if (!processMemory.isAlive()) {
                                iterator.remove()
                                processMemory.close()

                                if (latestFocusingGame == pid) {
                                    if (latestInGameScene) {
                                        onGameSceneExited()
                                    }

                                    if (latestInResultsScene) {
                                        onResultsSceneExited()
                                    }
                                }
                            }
                        }
                    }
                    val gamePIDs = listGamesPID()
                    gamePIDs.filterNot {
                        processMemoryMap.keys.contains(it)
                    }.forEach { pid ->
                        val processMemory = ProcessMemory.tryOpen(pid, addressContainer)
                        if (processMemory != null) {
                            processMemoryMap[pid] = processMemory
                        }
                    }

                    val focusingWindowPID = getFocusingWindowPID()
                    processMemoryMap[focusingWindowPID]?.let { processMemory ->
                        runCatching {
                            latestFocusingGame = focusingWindowPID
                            val currentGameScene = processMemory.readGameSceneInstance()
                            when {
                                !latestInGameScene && currentGameScene != 0L -> {
                                    latestInGameScene = true
                                    onGameSceneEntered()
                                }

                                latestInGameScene && currentGameScene == 0L -> {
                                    latestInGameScene = false
                                    onGameSceneExited()
                                }
                            }

                            val currentResultsScene = processMemory.readResultsSceneInstance()
                            when {
                                !latestInResultsScene && currentResultsScene != 0L -> {
                                    latestInResultsScene = true
                                    onResultsSceneEntered()
                                }

                                latestInResultsScene && currentResultsScene == 0L -> {
                                    latestInResultsScene = false
                                    onResultsSceneExited()
                                }
                            }
                        }.onFailure { e ->
                            println("${e::class.simpleName}: ${e.message}")
                            processMemory.close()
                            processMemoryMap.remove(focusingWindowPID)
                        }
                    }
                    delay(pollInterval)
                }
            }

            // Cursor overlay detect
            launch(Dispatchers.IO) {
                while (true) {
                    val cursorPos = getCursorPosition()
                    val (windowPosX, windowPosY) = Pair(window.x, window.y)
                    val (windowSizeWidth, windowSizeHeight) = Pair(window.size.width, window.size.height)
                    val cursorCoveredX = cursorPos?.first in windowPosX..(windowPosX + windowSizeWidth)
                    val cursorCoveredY = cursorPos?.second in windowPosY..(windowPosY + windowSizeHeight)
                    cursorCovered = cursorCoveredX && cursorCoveredY
                    val inResultsSceneVisible = when (onlyDisplayInResultsScene) {
                        true -> inResultsScene
                        else -> true
                    }
                    val newVisible = focusingGame && !playingGame && !cursorCovered && inResultsSceneVisible
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
                username = model.username,
                potential = gameRecordsModel.potential.floor(3),
                playResult = playResult,
            )
        }
    }
}