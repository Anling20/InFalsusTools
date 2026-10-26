package cn.anling.inftool

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import cn.anling.inftool.localization.LocalizationContext
import cn.anling.inftool.localization.defaultLocalizationContainers
import cn.anling.inftool.model.GameRecordsModel
import cn.anling.inftool.model.HomeModel
import cn.anling.inftool.page.Home
import cn.anling.inftool.setting.createPlatformSettings
import cn.anling.inftool.theme.AppTheme
import dev.nucleusframework.application.DecoratedWindow
import dev.nucleusframework.application.nucleusApplication
import dev.nucleusframework.taskbarprogress.tao.rememberTaoTaskbarProgress
import dev.nucleusframework.updater.NucleusUpdater
import dev.nucleusframework.updater.UpdateResult
import dev.nucleusframework.updater.provider.GitHubProvider
import dev.nucleusframework.window.DecoratedWindowDefaults
import dev.nucleusframework.window.NucleusDecoratedWindowTheme
import dev.nucleusframework.window.TitleBar
import dev.nucleusframework.window.WindowBackground
import io.github.vinceglb.filekit.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import java.io.File
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import java.text.DecimalFormat
import kotlin.io.path.absolutePathString
import kotlin.time.Instant

val updater = NucleusUpdater {
    provider = GitHubProvider("Anling20", "InFalsusTools")
    channel = "alpha"
    allowPrerelease = true
}

private suspend fun redirectOutput() {
    val logFile = getConfigDirectory() / "logs/latest.txt"
    if (!logFile.parent()!!.exists()) {
        logFile.parent()!!.createDirectories()
    }
    if (logFile.exists()) {
        val createdTime = logFile.createdAt()!!.toLocalDateTime(TimeZone.currentSystemDefault()).format(
            LocalDateTime.Format {
                year()
                monthNumber()
                day()
                char('_')
                hour()
                minute()
                second()
            }
        )
        logFile.atomicMove(getConfigDirectory() / "logs/$createdTime.txt")
    }
    val stream = PrintStream(logFile.file.outputStream(), true)
    System.setOut(stream)
    System.setErr(stream)
}

suspend fun main(args: Array<String>) {
    redirectOutput()
    FileKit.init("cn.anling.inftool")

    nucleusApplication(args) {
        val settings = createPlatformSettings()

        runCatching {
            if (settings.getIntOrNull("Setting.potentialAlgorithm") != null) {
                settings.remove("Setting.potentialAlgorithm")
            }
        }

        val model = remember(Unit) {
            HomeModel(
                settings = settings,
                initialSaveFile = run {
                    val root = Path.of(System.getProperty("user.home"), "AppData", "LocalLow", "lowiro", "infalsus")
                    Files.list(root).use { steamIdDirs ->
                        steamIdDirs
                            .filter { Files.isDirectory(it) }
                            .map { it.resolve("release/savestate_V3.sav") }
                            .filter { Files.isRegularFile(it) }
                            .toList()
                            .first()
                            .absolutePathString()
                    }
                },
            )
        }
        val gameRecordsModel = remember(Unit) {
            GameRecordsModel().apply {
                runBlocking {
                    update(
                        saveFile = model.settingModel.saveFile,
                        algorithm = model.settingModel.potentialAlgorithm,
                        levelMap = model.settingModel.constantTable,
                    )
                }
            }
        }
        val localizationContext = remember(Unit) {
            LocalizationContext(
                language = model.settingModel.language,
                containers = defaultLocalizationContainers,
            )
        }
        context(model, gameRecordsModel, localizationContext) {
            val windowState = rememberWindowState(
                size = DpSize(
                    width = 1280.dp,
                    height = 900.dp,
                ),
            )
            DecoratedWindow(
                onCloseRequest = ::exitApplication,
                state = windowState,
                title = "In Falsus Tools",
            ) {
                val scope = rememberCoroutineScope()
                var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
                val taskbar = rememberTaoTaskbarProgress()
                WindowBackground(MaterialTheme.colorScheme.background)
                Home(
                    onMinimum = { windowState.isMinimized = true },
                    onQuit = ::exitApplication,
                    updateInfo = updateInfo,
                )

                LaunchedEffect(Unit) {
                    if (updater.isUpdateSupported()) {
                        when (val result = updater.checkForUpdates()) {
                            is UpdateResult.Available -> {
                                println("Update available: ${result.info.version}")
                                updateInfo = UpdateInfo.Update(
                                    version = "v" + result.info.version,
                                    size = DecimalFormat("0.00 MiB")
                                        .format(result.info.currentFile.size / 1024.0 / 1024.0),
                                    time = Instant
                                        .parse(result.info.releaseDate)
                                        .toLocalDateTime(TimeZone.currentSystemDefault())
                                        .format(
                                            LocalDateTime.Format {
                                                year()
                                                char('-')
                                                monthNumber()
                                                char('-')
                                                day()
                                                char(' ')
                                                hour()
                                                char(':')
                                                minute()
                                                char(':')
                                                second()
                                            }
                                        ),
                                    onUpdate = {
                                        scope.launch {
                                            taskbar?.showProgress(0.0)
                                            val downloadingInfo =UpdateInfo.Downloading(
                                                downloaded = "0.00 MiB",
                                                total = DecimalFormat("0.00 MiB")
                                                    .format(result.info.currentFile.size / 1024.0 / 1024.0),
                                                progress = 0F,
                                                onCancel = {
                                                    this.cancel()
                                                    updateInfo = null
                                                },
                                            )
                                            updateInfo = downloadingInfo
                                            var installer: File? = null
                                            updater.downloadUpdate(result.info).collect { progress ->
                                                taskbar?.setProgress(progress.percent)
                                                downloadingInfo.downloaded = DecimalFormat("0.00 MiB")
                                                    .format(progress.bytesDownloaded / 1024.0 / 1024.0) + " MiB"
                                                downloadingInfo.progress = progress.percent.toFloat()
                                                progress.file?.let { installer = it }
                                            }
                                            taskbar?.hideProgress()
                                            installer?.let(updater::installAndRestart)
                                        }
                                    },
                                    onCancel = { updateInfo = null },
                                )

                            }

                            UpdateResult.NotAvailable -> println("Up to date")
                            is UpdateResult.Error -> println("Error: ${result.exception.message}")
                        }
                    }
                }
            }

            if (model.startedPotentialWindow) {
                context(model.settingModel) {
                    potentialWindow(
                        onCloseRequest = { model.startedPotentialWindow = false },
                        scale = model.modulePotentialWindowModel.scale,
                        addressContainer = model.modulePotentialWindowModel.addressContainer,
                        onlyDisplayInResultsScene = model.modulePotentialWindowModel.onlyDisplayInResultsScene,
                    )
                }
            }

            NucleusDecoratedWindowTheme(titleBarStyle = DecoratedWindowDefaults.lightTitleBarStyle()) {
                if (model.displayingB30Preview) {
                    DecoratedWindow(
                        onCloseRequest = { model.displayingB30Preview = false },
                        state = rememberWindowState(),
                        title = "Potential Preview",
                    ) {
                        WindowBackground(MaterialTheme.colorScheme.background)
                        TitleBar {
                            Text("Potential Preview", Modifier.align(Alignment.CenterHorizontally))
                        }
                        PotentialImage(
                            useNeoCard = model.modulePotentialImageModel.useNeoCard,
                            username = model.settingModel.username,
                            potential = gameRecordsModel.potential,
                            algorithm = model.settingModel.potentialAlgorithm,
                            contents = model.settingModel.potentialAlgorithm.contents(
                                histories = gameRecordsModel.historyRecords,
                                constantTable = model.settingModel.constantTable,
                            ),
                        )
                    }
                }

                if (model.displayingAchievementsPreview) {
                    DecoratedWindow(
                        onCloseRequest = { model.displayingAchievementsPreview = false },
                        state = rememberWindowState(),
                        title = "Achievements Preview",
                    ) {
                        WindowBackground(MaterialTheme.colorScheme.background)
                        TitleBar {
                            Text("Achievements Preview", Modifier.align(Alignment.CenterHorizontally))
                        }
                        val difficulties = buildList {
                            if (model.moduleAchievementsImageModel.minDifficultSelect) {
                                add(Difficult.MIN)
                            }
                            if (model.moduleAchievementsImageModel.evoDifficultSelect) {
                                add(Difficult.EVO)
                            }
                            if (model.moduleAchievementsImageModel.ultDifficultSelect) {
                                add(Difficult.ULT)
                            }
                            if (model.moduleAchievementsImageModel.fbdDifficultSelect) {
                                add(Difficult.FBD)
                            }
                        }
                        AchievementsImage(
                            useNeoCard = model.moduleAchievementsImageModel.useNeoCard,
                            username = model.settingModel.username,
                            potential = gameRecordsModel.potential,
                            algorithm = model.settingModel.potentialAlgorithm,
                            highScores = gameRecordsModel.historyRecords.findHighScores(),
                            levelMap = model.settingModel.constantTable,
                            min = model.moduleAchievementsImageModel.minLevelLimit,
                            max = model.moduleAchievementsImageModel.maxLevelLimit,
                            difficulties = difficulties,
                        )
                    }
                }
            }

            val renderRequestList = remember { mutableStateListOf<RenderRequest>() }

            LaunchedEffect(Unit) {
                model.renderRequestChannel.consumeEach { next ->
                    renderRequestList += next
                    next.finished.await()
                    renderRequestList -= next
                }
            }

            renderRequestList.forEach { renderRequest ->
                DecoratedWindow(
                    onCloseRequest = {},
                    visible = false,
                ) {
                    val graphicsLayer = rememberGraphicsLayer()
                    val rendered = remember(renderRequest) { CompletableDeferred<Unit>() }
                    Box(
                        modifier = Modifier
                            .wrapContentSize(unbounded = true)
                            .drawWithContent {
                                graphicsLayer.record {
                                    this@drawWithContent.drawContent()
                                }
                                drawLayer(graphicsLayer)
                                rendered.complete(Unit)
                            },
                    ) {
                        CompositionLocalProvider(LocalDensity provides Density(1F, 1F)) {
                            AppTheme(false) {
                                renderRequest.content()
                            }
                        }
                    }

                    LaunchedEffect(renderRequest) {
                        try {
                            rendered.await()
                            renderRequest.onRendered(graphicsLayer.toImageBitmap())
                        } finally {
                            renderRequest.finished.complete(Unit)
                        }
                    }
                }
            }
        }
    }
}