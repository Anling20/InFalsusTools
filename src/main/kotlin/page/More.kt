package cn.anling.inftool.page

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.anling.infalsustools.generated.resources.Res
import cn.anling.infalsustools.generated.resources.folder_open
import cn.anling.infalsustools.generated.resources.github
import cn.anling.inftool.localization.LocalizationContext
import cn.anling.inftool.localization.i18n
import org.jetbrains.compose.resources.painterResource

object MorePage {
    class ViewModel(
        val version: String,
        val updateChannel: String,
    )

    @Composable
    context(_: LocalizationContext)
    fun Page(
        viewModel: ViewModel,
        onCheckUpdate: () -> Unit,
        onHistoryVersions: () -> Unit,
        onOpenDataFolder: () -> Unit,
        onOpenGitHub: () -> Unit,
    ) {
        Content(
            viewModel = viewModel,
            onCheckUpdate = onCheckUpdate,
            onHistoryVersions = onHistoryVersions,
            onOpenDataFolder = onOpenDataFolder,
            onOpenGitHub = onOpenGitHub,
        )
    }

    @Composable
    context(_: LocalizationContext)
    private fun Content(
        viewModel: ViewModel,
        onCheckUpdate: () -> Unit,
        onHistoryVersions: () -> Unit,
        onOpenDataFolder: () -> Unit,
        onOpenGitHub: () -> Unit,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(64.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            InFalsusTools(
                onCheckUpdate = onCheckUpdate,
                onHistoryVersions = onHistoryVersions,
                onOpenDataFolder = onOpenDataFolder,
                onOpenGitHub = onOpenGitHub,
            )
            About(
                viewModel = viewModel,
            )
        }
    }

    @Composable
    context(_: LocalizationContext)
    private fun InFalsusTools(
        onCheckUpdate: () -> Unit,
        onHistoryVersions: () -> Unit,
        onOpenDataFolder: () -> Unit,
        onOpenGitHub: () -> Unit,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = i18n("title"),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                lineHeight = 28.sp,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(250.dp, 60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5F))
                        .clickable(onClick = onCheckUpdate),
                ) {
                    Text(
                        text = i18n("more.check_update"),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(250.dp, 60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5F))
                        .clickable(onClick = onHistoryVersions),
                ) {
                    Text(
                        text = i18n("more.history_versions"),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                    )
                }
            }
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5F))
                    .clickable(onClick = onOpenDataFolder),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.folder_open),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(36.dp),
                    )
                    Text(
                        text = i18n("more.open_data_folder"),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                    )
                }
            }
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5F))
                    .clickable(onClick = onOpenGitHub),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.github),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(36.dp),
                    )
                    Text(
                        text = i18n("more.open_github"),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                    )
                }
            }
        }
    }

    @Composable
    context(_: LocalizationContext)
    private fun About(
        viewModel: ViewModel,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = i18n("more.about"),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                lineHeight = 28.sp,
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = i18n("more.version"),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                )
                Text(
                    text = viewModel.version,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                )
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = i18n("more.update_channel"),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                )
                Text(
                    text = viewModel.updateChannel,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                )
            }
        }
    }
}