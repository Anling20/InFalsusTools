package cn.anling.inftool.page

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.anling.infalsustools.generated.resources.*
import cn.anling.inftool.theme.AppTheme
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.blur.material3.Material3
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import org.jetbrains.compose.resources.painterResource

@Composable
fun Home(
    onMinimum: () -> Unit,
    onQuit: () -> Unit,
    windowDraggableArea: @Composable (@Composable () -> Unit) -> Unit,
) {
    var destination by remember { mutableStateOf<HomeDestination>(HomeDestination.Home) }
    AppTheme(false) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize(),
        ) {
            Box {
                val hazeState = rememberHazeState()
                Image(
                    painter = painterResource(Res.drawable.home_background),
                    contentDescription = null,
                    alignment = Alignment.BottomCenter,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(hazeState),
                )
                Column(modifier = Modifier.fillMaxSize()) {
                    windowDraggableArea {
                        TopBar(
                            hazeState = hazeState,
                            onMinimum = onMinimum,
                            onQuit = onQuit,
                        )
                    }
                    ContentContainer(
                        hazeState = hazeState,
                        destination = destination,
                        onChangeDestination = { destination = it }
                    )
                }
            }
        }
    }
}

@Composable
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
            text = "In Falsus Tools",
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
private fun ContentContainer(
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
            destination = destination,
        )
    }
}

@Composable
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
private fun HomeSideBarContent(
    hazeState: HazeState,
    destination: HomeDestination,
    onChangeDestination: (HomeDestination) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .hazeBlur(
                input = HazeInput.Sources(hazeState),
                style = HazeBlurStyle.Material3 {
                    blurRadius(8.dp)
                }
            )
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6F))
            .padding(horizontal = 16.dp, vertical = 24.dp),
    ) {
        Icon(
            painter = painterResource(Res.drawable.home),
            contentDescription = null,
            tint = animateColorAsState(
                when (destination == HomeDestination.Home) {
                    true -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            ).value,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onChangeDestination(HomeDestination.Home) },
        )
        Icon(
            painter = painterResource(Res.drawable.module),
            contentDescription = null,
            tint = animateColorAsState(
                when (destination is HomeDestination.Module) {
                    true -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            ).value,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onChangeDestination(HomeDestination.Module(ModuleDestination.PotentialWindow)) },
        )
        Icon(
            painter = painterResource(Res.drawable.setting),
            contentDescription = null,
            tint = animateColorAsState(
                when (destination == HomeDestination.Setting) {
                    true -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            ).value,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onChangeDestination(HomeDestination.Setting) },
        )
        Icon(
            painter = painterResource(Res.drawable.more),
            contentDescription = null,
            tint = animateColorAsState(
                when (destination == HomeDestination.More) {
                    true -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            ).value,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onChangeDestination(HomeDestination.More) },
        )
    }
}

@Composable
private fun ModuleSideBarContent(
    hazeState: HazeState,
    onBack: () -> Unit,
    destination: ModuleDestination,
    onChangeDestination: (ModuleDestination) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
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
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onBack),
        ) {
            Icon(
                painter = painterResource(Res.drawable.back),
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = "Back",
                color = MaterialTheme.colorScheme.outline,
                fontSize = 14.sp,
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val potentialWindowSource = remember { MutableInteractionSource() }
            val best30ImageSource = remember { MutableInteractionSource() }
            val achievementsImageSource = remember { MutableInteractionSource() }
            val playHistoriesSource = remember { MutableInteractionSource() }
            Text(
                text = "Potential window",
                color = animateColorAsState(
                    when (destination == ModuleDestination.PotentialWindow || potentialWindowSource.collectIsHoveredAsState().value) {
                        true -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                ).value,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                modifier = Modifier.clickable(
                    interactionSource = potentialWindowSource,
                    indication = null,
                    onClick = { onChangeDestination(ModuleDestination.PotentialWindow) },
                ),
            )
            Text(
                text = "Best30 image",
                color = animateColorAsState(
                    when (destination == ModuleDestination.Best30Image || best30ImageSource.collectIsHoveredAsState().value) {
                        true -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                ).value,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                modifier = Modifier.clickable(
                    interactionSource = best30ImageSource,
                    indication = null,
                    onClick = { onChangeDestination(ModuleDestination.Best30Image) },
                ),
            )
            Text(
                text = "Achievements image",
                color = animateColorAsState(
                    when (destination == ModuleDestination.AchievementsImage || achievementsImageSource.collectIsHoveredAsState().value) {
                        true -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                ).value,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                modifier = Modifier.clickable(
                    interactionSource = achievementsImageSource,
                    indication = null,
                    onClick = { onChangeDestination(ModuleDestination.AchievementsImage) },
                ),
            )
            Text(
                text = "Play histories",
                color = animateColorAsState(
                    when (destination == ModuleDestination.PlayHistories || playHistoriesSource.collectIsHoveredAsState().value) {
                        true -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                ).value,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                modifier = Modifier.clickable(
                    interactionSource = playHistoriesSource,
                    indication = null,
                    onClick = { onChangeDestination(ModuleDestination.PlayHistories) },
                ),
            )
        }
    }
}

@Composable
private fun Content(
    hazeState: HazeState,
    destination: HomeDestination,
) {
    AnimatedVisibility(
        visible = destination != HomeDestination.Home,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
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

        }
    }
}