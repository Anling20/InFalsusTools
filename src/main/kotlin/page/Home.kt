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
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.renderComposeScene
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.anling.infalsustools.generated.resources.*
import cn.anling.inftool.*
import cn.anling.inftool.localization.LocalizationContext
import cn.anling.inftool.localization.i18n
import cn.anling.inftool.model.HomeModel
import cn.anling.inftool.parser.ImageGenerationType.Clipboard
import cn.anling.inftool.parser.ImageGenerationType.File
import cn.anling.inftool.parser.SaveFileParser
import cn.anling.inftool.theme.AppTheme
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.blur.material3.Material3
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.skia.EncodedImageFormat

@Composable
context(model: HomeModel, _: LocalizationContext)
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
context(_: HomeModel, _: LocalizationContext)
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
context(model: HomeModel, _: LocalizationContext)
private fun Content(hazeState: HazeState) {
    val scope = remember { CoroutineScope(Dispatchers.IO) }
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
                            ModuleDestination.PotentialWindow -> context(model.potentialWindowModel) {
                                ModulePotentialWindowContent(
                                    started = model.startedPotentialWindow,
                                    onSwitchStarted = { model.startedPotentialWindow = !model.startedPotentialWindow },
                                )
                            }

                            ModuleDestination.Best30Image -> context(model.best30ImageModel) {
                                ModuleBest30ImageContent(
                                    onOpenPreview = { model.displayingB30Preview = true },
                                    onGenerate = { type ->
                                        scope.launch {
                                            model.waiting = true
                                            val data = SaveFileParser.parse(model.settingModel.saveFile)
                                                .toDiveRecordList()
                                                .map { it.process(model.settingModel.useExperimentalLevel) }
                                            renderComposeScene(1, 1) {
                                                B30(
                                                    darkTheme = false,
                                                    onSizeChanged = { size ->
                                                        val img = renderComposeScene(size.width, size.height) {
                                                            B30(
                                                                darkTheme = false,
                                                                onSizeChanged = {},
                                                                username = model.settingModel.username,
                                                                data = data,
                                                            )
                                                        }
                                                        when (type) {
                                                            File -> scope.launch {
                                                                FileKit.openFileSaver(
                                                                    suggestedName = "B30",
                                                                    defaultExtension = ".jpg",
                                                                    allowedExtensions = setOf(".jpg", ".jpeg"),
                                                                )?.let { file ->
                                                                    img.encodeToData(
                                                                        format = EncodedImageFormat.JPEG,
                                                                        quality = model.best30ImageModel.quality,
                                                                    )?.let { data ->
                                                                        file.write(data.bytes)
                                                                        println("B30 generated to " + file.absolutePath())
                                                                    }
                                                                }
                                                            }

                                                            Clipboard -> {
                                                                copyImageToClipboard(img.toComposeImageBitmap())
                                                                println("B30 generated to clipboard")
                                                            }
                                                        }
                                                    },
                                                    username = model.settingModel.username,
                                                    data = data,
                                                )
                                            }
                                            model.waiting = false
                                        }
                                    },
                                )
                            }

                            ModuleDestination.AchievementsImage -> context(model.achievementsImageModel) {
                                ModuleAchievementsImageContent(
                                    onOpenPreview = { model.displayingAchievementsPreview = true },
                                    onGenerate = { type ->
                                        scope.launch {
                                            model.waiting = true
                                            val data = SaveFileParser.parse(model.settingModel.saveFile)
                                                .toDiveRecordList()
                                                .map { it.process(model.settingModel.useExperimentalLevel) }
                                            val difficulties = buildList {
                                                if (model.achievementsImageModel.minDifficultSelect) {
                                                    add(Difficult.MIN)
                                                }
                                                if (model.achievementsImageModel.evoDifficultSelect) {
                                                    add(Difficult.EVO)
                                                }
                                                if (model.achievementsImageModel.ultDifficultSelect) {
                                                    add(Difficult.ULT)
                                                }
                                                if (model.achievementsImageModel.fbdDifficultSelect) {
                                                    add(Difficult.FBD)
                                                }
                                            }
                                            renderComposeScene(1, 1) {
                                                Ahv(
                                                    darkTheme = false,
                                                    onSizeChanged = { size ->
                                                        val img = renderComposeScene(size.width, size.height) {
                                                            Ahv(
                                                                darkTheme = false,
                                                                onSizeChanged = {},
                                                                username = model.settingModel.username,
                                                                data = data,
                                                                experimentalLevel = model.settingModel.useExperimentalLevel,
                                                                min = model.achievementsImageModel.minLevelLimit,
                                                                max = model.achievementsImageModel.maxLevelLimit,
                                                                difficulties = difficulties,
                                                            )
                                                        }
                                                        when (type) {
                                                            File -> scope.launch {
                                                                FileKit.openFileSaver(
                                                                    suggestedName = "AHV",
                                                                    defaultExtension = ".jpg",
                                                                    allowedExtensions = setOf(".jpg", ".jpeg"),
                                                                )?.let { file ->
                                                                    img.encodeToData(
                                                                        format = EncodedImageFormat.JPEG,
                                                                        quality = model.achievementsImageModel.quality,
                                                                    )?.let { data ->
                                                                        file.write(data.bytes)
                                                                        println("Ahv generated to " + file.absolutePath())
                                                                    }
                                                                }
                                                            }

                                                            Clipboard -> {
                                                                copyImageToClipboard(img.toComposeImageBitmap())
                                                                println("Ahv generated to clipboard")
                                                            }
                                                        }
                                                    },
                                                    username = model.settingModel.username,
                                                    data = data,
                                                    experimentalLevel = model.settingModel.useExperimentalLevel,
                                                    min = model.achievementsImageModel.minLevelLimit,
                                                    max = model.achievementsImageModel.maxLevelLimit,
                                                    difficulties = difficulties,
                                                )
                                            }
                                            model.waiting = false
                                        }
                                    },
                                )
                            }

                            ModuleDestination.PlayHistories -> Unit
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