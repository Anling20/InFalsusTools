package cn.anling.inftool.page

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.anling.infalsustools.generated.resources.Res
import cn.anling.infalsustools.generated.resources.back
import cn.anling.infalsustools.generated.resources.home
import cn.anling.infalsustools.generated.resources.module
import cn.anling.infalsustools.generated.resources.more
import cn.anling.infalsustools.generated.resources.setting
import cn.anling.inftool.HomeDestination
import cn.anling.inftool.ModuleDestination
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.blur.material3.Material3
import org.jetbrains.compose.resources.painterResource

@Composable
fun HomeSideBarContent(
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
fun ModuleSideBarContent(
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