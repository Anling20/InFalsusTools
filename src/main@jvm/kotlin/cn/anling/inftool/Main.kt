package cn.anling.inftool

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import cn.anling.inftool.localization.i18n
import cn.anling.inftool.model.GameRecordsModel
import cn.anling.inftool.model.HomeModel
import cn.anling.inftool.page.Home
import cn.anling.inftool.ranking.generatePlayJson
import cn.anling.inftool.setting.createPlatformSettings
import cn.anling.inftool.theme.AppTheme
import dev.nucleusframework.application.DecoratedWindow
import dev.nucleusframework.application.nucleusApplication
import dev.nucleusframework.nativehttp.NativeHttpClient.withNativeSsl
import dev.nucleusframework.taskbarprogress.tao.TaoTaskbarProgressScope
import dev.nucleusframework.taskbarprogress.tao.rememberTaoTaskbarProgress
import dev.nucleusframework.updater.NucleusUpdater
import dev.nucleusframework.updater.UpdateResult
import dev.nucleusframework.updater.provider.GitHubProvider
import dev.nucleusframework.window.DecoratedWindowDefaults
import dev.nucleusframework.window.NucleusDecoratedWindowTheme
import dev.nucleusframework.window.TitleBar
import dev.nucleusframework.window.WindowBackground
import io.github.vinceglb.filekit.*
import io.github.vinceglb.filekit.dialogs.openFileSaver
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import java.awt.Desktop
import java.io.File
import java.io.PrintStream
import java.net.ProxySelector
import java.net.URI
import java.net.http.HttpClient
import java.nio.file.Files
import java.nio.file.Path
import java.text.DecimalFormat
import kotlin.io.path.absolutePathString
import kotlin.time.Instant

private suspend fun redirectOutput() {
    val logFile = getConfigDirectory() / "logs/latest.txt"
    if (!logFile.parent()!!.exists()) {
        logFile.parent()!!.createDirectories()
    }
    if (logFile.exists()) {
        val createdTime = logFile.lastModified().toLocalDateTime(TimeZone.currentSystemDefault()).format(
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
                    context(model.settingModel) {
                        update()
                    }
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
                    version = updater.currentVersion,
                    onUploadData = {
                        scope.launch {
                            FileKit.openFileSaver(
                                suggestedName = "play",
                                defaultExtension = ".json",
                            )?.let { file ->
                                file.writeString(
                                    generatePlayJson(
                                        userName = model.settingModel.username,
                                        highScores = gameRecordsModel.historyRecords.findHighScores(),
                                    )
                                )
                                Desktop.getDesktop().browse(URI("https://f.kdocs.cn/g/y4CL2PIS"))
                            }
                        }
                    },
                    onCheckUpdate = {
                        scope.launch {
                            model.waiting = true
                            checkForUpdates(
                                scope = scope,
                                onChangeUpdateInfo = { updateInfo = it },
                                snackbarHostState = model.snackbarHostState,
                                taskbar = taskbar,
                            )
                            model.waiting = false
                        }
                    },
                    onHistoryVersions = {
                        Desktop.getDesktop().browse(URI("https://github.com/Anling20/InFalsusTools/releases"))
                    },
                    onOpenDataFolder = {
                        Desktop.getDesktop().open(getConfigDirectory().file)
                    },
                    onOpenGitHub = {
                        Desktop.getDesktop().browse(URI("https://github.com/Anling20/InFalsusTools"))
                    },
                )

                LaunchedEffect(Unit) {
                    checkForUpdates(
                        scope = scope,
                        onChangeUpdateInfo = { updateInfo = it },
                        snackbarHostState = model.snackbarHostState,
                        taskbar = taskbar,
                    )
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
                    PotentialImagePreviewWindow()
                }

                if (model.displayingAchievementsPreview) {
                    AchievementsImagePreviewWindow()
                }
            }

            Render(model.renderRequestChannel)
        }
    }
}

val updater = NucleusUpdater {
    provider = GitHubProvider("Anling20", "InFalsusTools")
    channel = "alpha"
    allowPrerelease = true
    httpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .withNativeSsl()
        .proxy(ProxySelector.getDefault())
        .build()
}

context(_: LocalizationContext)
private suspend fun checkForUpdates(
    scope: CoroutineScope,
    onChangeUpdateInfo: (UpdateInfo?) -> Unit,
    snackbarHostState: SnackbarHostState,
    taskbar: TaoTaskbarProgressScope?,
) = when (val result = updater.checkForUpdates()) {
    is UpdateResult.Available -> {
        println("Update available: ${result.info.version}, currentFile: ${result.info.currentFile}")
        onChangeUpdateInfo(
            UpdateInfo.Update(
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
                        println("Update started")
                        taskbar?.showProgress(0.0)
                        var downloadingInfo: UpdateInfo.Downloading? = null
                        var installer: File? = null
                        updater.downloadUpdate(result.info).collect { progress ->
                            if (downloadingInfo == null) {
                                downloadingInfo = UpdateInfo.Downloading(
                                    total = DecimalFormat("0.00 MiB")
                                        .format(progress.totalBytes / 1024.0 / 1024.0),
                                    onCancel = {
                                        this.cancel()
                                        onChangeUpdateInfo(null)
                                    },
                                )
                                onChangeUpdateInfo(downloadingInfo)
                            }
                            println("Update downloading: $progress")
                            taskbar?.setProgress(progress.percent)
                            downloadingInfo.downloaded = DecimalFormat("0.00 MiB")
                                .format(progress.bytesDownloaded / 1024.0 / 1024.0)
                            downloadingInfo.percent = progress.percent.toFloat()
                            progress.file?.let { installer = it }
                        }
                        taskbar?.hideProgress()
                        installer?.let(updater::installAndRestart)
                    }
                },
                onCancel = {
                    println("Update canceled")
                    taskbar?.hideProgress()
                    onChangeUpdateInfo(null)
                },
            )
        )
    }

    UpdateResult.NotAvailable -> scope.launch {
        println("Update not available")
        snackbarHostState.showSnackbar(i18n("update.snackbar.update_not_available"))
    }

    is UpdateResult.Error -> scope.launch {
        println("Update error: ${result.exception.message}")
        result.exception.printStackTrace()
        snackbarHostState.showSnackbar("${i18n("update.snackbar.update_error")}: ${result.exception.message}")
    }
}

@Composable
context(model: HomeModel, gameRecordsModel: GameRecordsModel, _: LocalizationContext)
private fun PotentialImagePreviewWindow() {
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

@Composable
context(model: HomeModel, gameRecordsModel: GameRecordsModel, _: LocalizationContext)
private fun AchievementsImagePreviewWindow() {
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

@Composable
private fun Render(renderRequestChannel: Channel<RenderRequest>) {
    DecoratedWindow(
        onCloseRequest = {},
        state = rememberWindowState(size = DpSize(1.dp, 1.dp)),
        resizable = false,
        alwaysOnTop = true,
        undecorated = true,
        hiddenFromDock = true,
        transparent = true,
        clickThrough = true,
    ) {
        var currentRenderRequest by remember { mutableStateOf<RenderRequest?>(null) }
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                renderRequestChannel.consumeEach { next ->
                    currentRenderRequest?.finished?.await()
                    currentRenderRequest = next
                }
            }
        }
        Box(modifier = Modifier.alpha(0F)) {
            currentRenderRequest?.let { renderRequest ->
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
                    rendered.await()
                    withFrameNanos { }
                    val job = renderRequest.onRendered(graphicsLayer.toImageBitmap())
                    job.invokeOnCompletion {
                        renderRequest.finished.complete(Unit)
                        currentRenderRequest = null
                        System.gc()
                    }
                }
            }
        }
    }
}