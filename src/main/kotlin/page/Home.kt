package cn.anling.inftool.page

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.anling.infalsustools.generated.resources.*
import cn.anling.inftool.*
import cn.anling.inftool.localization.LocalizationContext
import cn.anling.inftool.localization.i18n
import cn.anling.inftool.model.GameRecordsModel
import cn.anling.inftool.model.HomeModel
import cn.anling.inftool.parser.ImageGenerationType.Clipboard
import cn.anling.inftool.parser.ImageGenerationType.File
import cn.anling.inftool.theme.AppTheme
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.blur.material3.Material3
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import dev.nucleusframework.window.windowDragArea
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.ImageFormat
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.dialogs.compose.util.encodeToByteArray
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.compose.resources.painterResource

@Composable
context(model: HomeModel, _: GameRecordsModel, _: LocalizationContext)
fun Home(
    onMinimum: () -> Unit,
    onQuit: () -> Unit,
    updateInfo: UpdateInfo?,
) = AppTheme(false) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize(),
    ) {
        Box {
            val hazeState = rememberHazeState()
            val waitingHazeState = rememberHazeState()
            Image(
                painter = painterResource(Res.drawable.home_background),
                contentDescription = null,
                alignment = Alignment.BottomCenter,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(hazeState)
                    .hazeSource(waitingHazeState),
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(waitingHazeState),
            ) {
                TopBar(
                    hazeState = hazeState,
                    onMinimum = onMinimum,
                    onQuit = onQuit,
                )
                Box {
                    ContentContainer(
                        hazeState = hazeState,
                        destination = model.destination,
                        onChangeDestination = { model.destination = it },
                    )
                    androidx.compose.animation.AnimatedVisibility(
                        visible = model.waiting,
                        enter = fadeIn() + slideInVertically { it },
                        exit = fadeOut() + slideOutVertically { it },
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .hazeBlur(
                                    input = HazeInput.Sources(waitingHazeState),
                                    style = HazeBlurStyle.Material3 {
                                        blurRadius(8.dp)
                                    }
                                )
                                .background(Color.Black.copy(alpha = 0.32F))
                                .clickable(
                                    interactionSource = null,
                                    indication = null,
                                    onClick = {},
                                ),
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.loading),
                                contentDescription = "Loading",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(64.dp)
                                    .rotate(
                                        degrees = rememberInfiniteTransition().animateFloat(
                                            initialValue = 0F,
                                            targetValue = 360F,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(
                                                    durationMillis = 1000,
                                                    easing = LinearEasing,
                                                ),
                                            ),
                                        ).value,
                                    ),
                            )
                        }
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = updateInfo != null,
                        enter = fadeIn() + slideInVertically { it },
                        exit = fadeOut() + slideOutVertically { it },
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .hazeBlur(
                                    input = HazeInput.Sources(waitingHazeState),
                                    style = HazeBlurStyle.Material3 {
                                        blurRadius(8.dp)
                                    }
                                )
                                .background(Color.Black.copy(alpha = 0.32F))
                                .clickable(
                                    interactionSource = null,
                                    indication = null,
                                ) { updateInfo?.onCancel() },
                        ) {
                            AnimatedContent(
                                targetState = updateInfo,
                                modifier = Modifier
                            ) { updateInfo ->
                                when (updateInfo) {
                                    is UpdateInfo.Update -> Column(
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier
                                            .width(600.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable(null, null) {}
                                            .background(MaterialTheme.colorScheme.surface)
                                            .padding(16.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(192.dp)
                                                .clip(RoundedCornerShape(16.dp)),
                                        ) {
                                            Image(
                                                painter = painterResource(Res.drawable.home_background),
                                                contentDescription = null,
                                                alignment = Alignment.BottomCenter,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .align(Alignment.CenterEnd)
                                                    .size(384.dp, 192.dp),
                                            )
                                            Spacer(
                                                modifier = Modifier
                                                    .align(Alignment.CenterStart)
                                                    .size(384.dp, 192.dp)
                                                    .background(
                                                        brush = Brush.horizontalGradient(
                                                            0.5F to MaterialTheme.colorScheme.primaryContainer,
                                                            1.0F to MaterialTheme.colorScheme.primaryContainer.copy(
                                                                alpha = 0.0F
                                                            ),
                                                        )
                                                    ),
                                            )
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(
                                                    space = 12.dp,
                                                    alignment = Alignment.CenterVertically,
                                                ),
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .padding(start = 16.dp),
                                            ) {
                                                Text(
                                                    text = i18n("update.update_available"),
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    fontSize = 24.sp,
                                                    lineHeight = 28.sp,
                                                    maxLines = 1,
                                                )
                                                Text(
                                                    text = updateInfo.version,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    fontSize = 24.sp,
                                                    lineHeight = 28.sp,
                                                    maxLines = 1,
                                                )
                                                Text(
                                                    text = updateInfo.time,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5F),
                                                    fontSize = 18.sp,
                                                    lineHeight = 20.sp,
                                                    maxLines = 1,
                                                )
                                                Text(
                                                    text = updateInfo.size,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5F),
                                                    fontSize = 18.sp,
                                                    lineHeight = 20.sp,
                                                    maxLines = 1,
                                                )
                                            }
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(
                                                12.dp,
                                                alignment = Alignment.End
                                            ),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(100.dp, 40.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                                                    .clickable(onClick = updateInfo.onCancel),
                                            ) {
                                                Text(
                                                    text = i18n("update.later"),
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75F),
                                                    fontSize = 16.sp,
                                                    lineHeight = 20.sp,
                                                    maxLines = 1,
                                                )
                                            }
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(100.dp, 40.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                                    .clickable(onClick = updateInfo.onUpdate),
                                            ) {
                                                Text(
                                                    text = i18n("update.update"),
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    fontSize = 16.sp,
                                                    lineHeight = 20.sp,
                                                    maxLines = 1,
                                                )
                                            }
                                        }
                                    }

                                    is UpdateInfo.Downloading -> Column(
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier
                                            .width(600.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable(null, null) {}
                                            .background(MaterialTheme.colorScheme.surface)
                                            .padding(16.dp),
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Icon(
                                                painter = painterResource(Res.drawable.download),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(48.dp),
                                            )
                                            Text(
                                                text = i18n("update.downloading"),
                                                color = MaterialTheme.colorScheme.onBackground,
                                                fontSize = 24.sp,
                                                lineHeight = 28.sp,
                                                maxLines = 1,
                                            )
                                        }
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            LinearProgressIndicator(
                                                progress = { updateInfo.percent / 100 },
                                                modifier = Modifier.fillMaxWidth(),
                                            )
                                            Row(
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth(),
                                            ) {
                                                Text(
                                                    text = updateInfo.downloaded + " / " + updateInfo.total,
                                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5F),
                                                    fontSize = 16.sp,
                                                    lineHeight = 20.sp,
                                                    maxLines = 1,
                                                )
                                                Text(
                                                    text = "${updateInfo.percent.toInt()}%",
                                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5F),
                                                    fontSize = 16.sp,
                                                    lineHeight = 20.sp,
                                                    maxLines = 1,
                                                )
                                            }
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(
                                                12.dp,
                                                alignment = Alignment.End
                                            ),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(100.dp, 40.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                                                    .clickable(onClick = updateInfo.onCancel),
                                            ) {
                                                Text(
                                                    text = i18n("update.cancel"),
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75F),
                                                    fontSize = 16.sp,
                                                    lineHeight = 20.sp,
                                                    maxLines = 1,
                                                )
                                            }
                                        }
                                    }

                                    null -> Unit
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
context(_: LocalizationContext)
private fun TopBar(
    hazeState: HazeState,
    onMinimum: () -> Unit,
    onQuit: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .windowDragArea()
            .hazeBlur(
                input = HazeInput.Sources(hazeState),
                style = HazeBlurStyle.Material3 {
                    blurRadius(8.dp)
                }
            )
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6F))
            .padding(16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .size(67.5.dp, 24.dp)
                .padding(start = 4.5.dp),
        ) {

        }
        Text(
            text = i18n("title"),
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            maxLines = 1,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.size(67.5.dp, 24.dp),
        ) {
            Icon(
                painter = painterResource(Res.drawable.minimum),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onMinimum),
            )
            Icon(
                painter = painterResource(Res.drawable.quit),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onQuit),
            )
        }
    }
}

@Composable
context(_: HomeModel, _: GameRecordsModel, _: LocalizationContext)
fun ContentContainer(
    hazeState: HazeState,
    destination: HomeDestination,
    onChangeDestination: (HomeDestination) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        SideBarSelect(
            hazeState = hazeState,
            destination = destination,
            onChangeDestination = onChangeDestination,
        )
        Content(
            hazeState = hazeState,
        )
    }
}

@Composable
context(_: LocalizationContext)
private fun SideBarSelect(
    hazeState: HazeState,
    destination: HomeDestination,
    onChangeDestination: (HomeDestination) -> Unit,
) {
    var latestModuleDestination by remember { mutableStateOf((destination as? HomeDestination.Module)?.child) }
    var latestNotModuleHomeDestination by remember { mutableStateOf(destination) }
    AnimatedContent(destination is HomeDestination.Module) { isModule ->
        when (isModule) {
            true -> ModuleSideBarContent(
                hazeState = hazeState,
                onBack = { onChangeDestination(latestNotModuleHomeDestination) },
                destination = (destination as? HomeDestination.Module)?.child ?: latestModuleDestination!!,
                onChangeDestination = {
                    onChangeDestination(HomeDestination.Module(it))
                    latestModuleDestination = it
                }
            )

            else -> HomeSideBarContent(
                hazeState = hazeState,
                destination = destination,
                onChangeDestination = {
                    onChangeDestination(it)
                    when (it) {
                        is HomeDestination.Module -> latestModuleDestination = it.child
                        else -> latestNotModuleHomeDestination = it
                    }
                },
            )
        }
    }
}

@Composable
context(model: HomeModel, gameRecordsModel: GameRecordsModel, _: LocalizationContext)
private fun Content(hazeState: HazeState) {
    val scope = rememberCoroutineScope()
    AnimatedVisibility(
        visible = model.destination != HomeDestination.Home,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .hazeBlur(
                    input = HazeInput.Sources(hazeState),
                    style = HazeBlurStyle.Material3 {
                        blurRadius(8.dp)
                    },
                )
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.15F))
                .padding(24.dp),
        ) {
            AnimatedContent(model.destination) { destination ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    when (destination) {
                        HomeDestination.Home -> Unit
                        is HomeDestination.Module -> when (destination.child) {
                            ModuleDestination.PotentialWindow -> context(model.modulePotentialWindowModel) {
                                ModulePotentialWindowContent(
                                    started = model.startedPotentialWindow,
                                    onSwitchStarted = { model.startedPotentialWindow = !model.startedPotentialWindow },
                                )
                            }

                            ModuleDestination.PotentialImage -> context(model.modulePotentialImageModel) {
                                val lock = remember { Mutex() }
                                ModulePotentialImageContent(
                                    onOpenPreview = { model.displayingB30Preview = true },
                                    onGenerate = { type ->
                                        if (lock.isLocked) return@ModulePotentialImageContent
                                        scope.launch(Dispatchers.IO) {
                                            lock.withLock {
                                                model.waiting = true
                                                val contents = model.settingModel.potentialAlgorithm.contents(
                                                    histories = gameRecordsModel.historyRecords,
                                                    constantTable = model.settingModel.constantTable,
                                                )
                                                model.renderRequestChannel.send(
                                                    RenderRequest(
                                                        onRendered = { imageBitmap ->
                                                            scope.launch(Dispatchers.IO) {
                                                                runCatching {
                                                                    when (type) {
                                                                        File -> FileKit.openFileSaver(
                                                                            suggestedName = "Potential",
                                                                            defaultExtension = ".jpg",
                                                                            allowedExtensions = setOf(".jpg", ".jpeg"),
                                                                        )?.let { file ->
                                                                            val byteArray =
                                                                                imageBitmap.encodeToByteArray(
                                                                                    format = ImageFormat.JPEG,
                                                                                    quality = model.modulePotentialImageModel.quality,
                                                                                )
                                                                            file.write(byteArray)
                                                                            println("Potential generated to ${file.absolutePath()}")
                                                                        }

                                                                        Clipboard -> {
                                                                            copyImageToClipboard(imageBitmap)
                                                                            println("Potential generated to clipboard")
                                                                        }
                                                                    }
                                                                }.onFailure {
                                                                    println("Error: ${it.message}")
                                                                    it.printStackTrace()
                                                                }
                                                                model.waiting = false
                                                            }
                                                        },
                                                    ) {
                                                        PotentialImageContent(
                                                            useNeoCard = model.modulePotentialImageModel.useNeoCard,
                                                            username = model.settingModel.username,
                                                            potential = gameRecordsModel.potential,
                                                            algorithm = model.settingModel.potentialAlgorithm,
                                                            contents = contents,
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    },
                                )
                            }

                            ModuleDestination.AchievementsImage -> context(model.moduleAchievementsImageModel) {
                                val lock = remember { Mutex() }
                                ModuleAchievementsImageContent(
                                    onOpenPreview = { model.displayingAchievementsPreview = true },
                                    onGenerate = { type ->
                                        if (lock.isLocked) return@ModuleAchievementsImageContent
                                        scope.launch(Dispatchers.IO) {
                                            lock.withLock {
                                                model.waiting = true
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
                                                model.renderRequestChannel.send(
                                                    RenderRequest(
                                                        onRendered = { imageBitmap ->
                                                            scope.launch(Dispatchers.IO) {
                                                                runCatching {
                                                                    when (type) {
                                                                        File -> FileKit.openFileSaver(
                                                                            suggestedName = "Achievements",
                                                                            defaultExtension = ".jpg",
                                                                            allowedExtensions = setOf(
                                                                                ".jpg",
                                                                                ".jpeg"
                                                                            ),
                                                                        )?.let { file ->
                                                                            val byteArray =
                                                                                imageBitmap.encodeToByteArray(
                                                                                    format = ImageFormat.JPEG,
                                                                                    quality = model.moduleAchievementsImageModel.quality,
                                                                                )
                                                                            file.write(byteArray)
                                                                            println("Achievements generated to ${file.absolutePath()}")
                                                                        }

                                                                        Clipboard -> {
                                                                            copyImageToClipboard(imageBitmap)
                                                                            println("Achievements generated to clipboard")
                                                                        }
                                                                    }
                                                                }.onFailure {
                                                                    println("Error: ${it.message}")
                                                                    it.printStackTrace()
                                                                }
                                                                model.waiting = false
                                                            }
                                                        },
                                                    ) {
                                                        AchievementsImageContent(
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
                                                )
                                            }
                                        }
                                    },
                                )
                            }

                            ModuleDestination.PlayHistories -> context(model.modulePlayHistoriesModel) {
                                ModulePlayHistoriesContent(
                                    records = gameRecordsModel.historyRecords,
                                )
                            }
                        }

                        HomeDestination.Terminal -> Unit

                        HomeDestination.Setting -> context(model.settingModel) {
                            SettingContent()
                        }

                        HomeDestination.More -> Unit
                    }
                }
            }
        }
    }
}