package cn.anling.inftool.page

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.ImageFormat
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.dialogs.compose.util.encodeToByteArray
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource

@Composable
context(model: HomeModel, _: GameRecordsModel, _: LocalizationContext)
fun Home(
    onMinimum: () -> Unit,
    onQuit: () -> Unit,
    windowDraggableArea: @Composable (@Composable () -> Unit) -> Unit,
) {
    AppTheme(false) {
        Surface(
            shape = RoundedCornerShape(24.dp),
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
                    windowDraggableArea {
                        TopBar(
                            hazeState = hazeState,
                            onMinimum = onMinimum,
                            onQuit = onQuit,
                        )
                    }
                    ContentContainer(
                        hazeState = hazeState,
                        destination = model.destination,
                        onChangeDestination = { model.destination = it },
                    )
                }
                AnimatedVisibility(
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
                            .background(Color.Black.copy(alpha = 0.32F)),
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
                                ModulePotentialImageContent(
                                    onOpenPreview = { model.displayingB30Preview = true },
                                    onGenerate = { type ->
                                        scope.launch(Dispatchers.IO) {
                                            model.waiting = true
                                            val contents = model.settingModel.potentialAlgorithm.contents(
                                                highScores = gameRecordsModel.highScoreRecords,
                                                histories = gameRecordsModel.historyRecords,
                                            )
                                            renderComposable(
                                                onRendered = { graphicsLayer ->
                                                    runBlocking {
                                                        val imageBitmap = graphicsLayer.toImageBitmap()
                                                        when (type) {
                                                            File -> FileKit.openFileSaver(
                                                                suggestedName = "Potential",
                                                                defaultExtension = ".jpg",
                                                                allowedExtensions = setOf(".jpg", ".jpeg"),
                                                            )?.let { file ->
                                                                val byteArray = imageBitmap.encodeToByteArray(
                                                                    format = ImageFormat.JPEG,
                                                                    quality = model.modulePotentialImageModel.quality,
                                                                )
                                                                file.write(byteArray)
                                                                println("Potential generated to " + file.absolutePath())
                                                            }

                                                            Clipboard -> {
                                                                copyImageToClipboard(imageBitmap)
                                                                println("Potential generated to clipboard")
                                                            }
                                                        }
                                                    }
                                                }
                                            ) {
                                                PotentialImageContent(
                                                    scale = 1.0F,
                                                    useNeoCard = model.modulePotentialImageModel.useNeoCard,
                                                    username = model.settingModel.username,
                                                    potential = gameRecordsModel.potential,
                                                    algorithm = model.settingModel.potentialAlgorithm,
                                                    contents = contents,
                                                )
                                            }
                                            model.waiting = false
                                        }
                                    },
                                )
                            }

                            ModuleDestination.AchievementsImage -> context(model.moduleAchievementsImageModel) {
                                ModuleAchievementsImageContent(
                                    onOpenPreview = { model.displayingAchievementsPreview = true },
                                    onGenerate = { type ->
                                        scope.launch(Dispatchers.IO) {
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
                                            renderComposable(
                                                onRendered = { graphicsLayer ->
                                                    runBlocking {
                                                        val imageBitmap = graphicsLayer.toImageBitmap()
                                                        when (type) {
                                                            File -> withContext(Dispatchers.Main) {
                                                                FileKit.openFileSaver(
                                                                    suggestedName = "Achievements",
                                                                    defaultExtension = ".jpg",
                                                                    allowedExtensions = setOf(".jpg", ".jpeg"),
                                                                )?.let { file ->
                                                                    val byteArray = imageBitmap.encodeToByteArray(
                                                                        format = ImageFormat.JPEG,
                                                                        quality = model.moduleAchievementsImageModel.quality,
                                                                    )
                                                                    file.write(byteArray)
                                                                    println("Achievements generated to " + file.absolutePath())
                                                                }
                                                            }

                                                            Clipboard -> {
                                                                copyImageToClipboard(imageBitmap)
                                                                println("Achievements generated to clipboard")
                                                            }
                                                        }
                                                    }
                                                }
                                            ) {
                                                AchievementsImageContent(
                                                    scale = 1.0F,
                                                    useNeoCard = model.moduleAchievementsImageModel.useNeoCard,
                                                    username = model.settingModel.username,
                                                    potential = gameRecordsModel.potential,
                                                    algorithm = model.settingModel.potentialAlgorithm,
                                                    highScores = gameRecordsModel.highScoreRecords,
                                                    useExperimentalLevel = model.settingModel.useExperimentalLevel,
                                                    min = model.moduleAchievementsImageModel.minLevelLimit,
                                                    max = model.moduleAchievementsImageModel.maxLevelLimit,
                                                    difficulties = difficulties,
                                                )
                                            }
                                            model.waiting = false
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