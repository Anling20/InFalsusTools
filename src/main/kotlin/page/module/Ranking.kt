package cn.anling.inftool.page.module

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.anling.infalsustools.generated.resources.Res
import cn.anling.infalsustools.generated.resources.allDrawableResources
import cn.anling.infalsustools.generated.resources.back
import cn.anling.infalsustools.generated.resources.chevron_right
import cn.anling.inftool.Colors
import cn.anling.inftool.localization.LocalizationContext
import cn.anling.inftool.localization.i18n
import cn.anling.inftool.or
import cn.anling.inftool.ranking.Difficult
import cn.anling.inftool.ranking.Difficult.*
import cn.anling.inftool.ranking.Ranking
import cn.anling.inftool.ranking.SongPlay
import cn.anling.inftool.song.Song
import cn.anling.inftool.song.Songs
import org.jetbrains.compose.resources.painterResource
import java.text.DecimalFormat
import java.util.Locale.getDefault

object RankingPage {
    private val scoreFormater = DecimalFormat("000,000,000")

    class ViewModel {
        val searchBarState = TextFieldState("")

        val songs = Songs.entries
            .flatMap {
                listOf(
                    it to Minimal,
                    it to Evolved,
                    it to Ultimate,
                    it to Forbidden,
                )
            }
            .sortedWith(compareBy<Pair<Song, Difficult>> { it.first.id }.thenBy { it.second.ordinal })
    }

    @Composable
    context(_: LocalizationContext)
    fun Page(
        viewModel: ViewModel,
        onUploadData: () -> Unit,
        selfName: String,
        ranking: Ranking,
    ) {
        Content(
            viewModel = viewModel,
            onUploadData = onUploadData,
            selfName = selfName,
            ranking = ranking,
        )
    }

    @Composable
    context(_: LocalizationContext)
    private fun Content(
        viewModel: ViewModel,
        onUploadData: () -> Unit,
        selfName: String,
        ranking: Ranking,
    ) {
        var selection by remember { mutableStateOf<Pair<Song, Difficult>?>(null) }
        AnimatedContent(selection) { innerSelection ->
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                when (innerSelection) {
                    null -> Selection(
                        viewModel = viewModel,
                        onUploadData = onUploadData,
                        onSelect = { song, difficult -> selection = song to difficult },
                    )

                    else -> {
                        val song = innerSelection.first
                        val difficult = innerSelection.second
                        SongRanking(
                            song = song,
                            difficult = difficult,
                            onBack = { selection = null },
                            selfName = selfName,
                            records = ranking[song.id]?.get(difficult) ?: emptyList(),
                        )
                    }
                }
            }
        }
    }

    @Composable
    context(_: LocalizationContext)
    private fun Selection(
        viewModel: ViewModel,
        onUploadData: () -> Unit,
        onSelect: (Song, Difficult) -> Unit,
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                state = viewModel.searchBarState,
                label = { Text(i18n("module.ranking.search")) },
                lineLimits = TextFieldLineLimits.SingleLine,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = TextFieldDefaults.colors().focusedContainerColor.copy(alpha = 0.6F),
                    unfocusedContainerColor = TextFieldDefaults.colors().unfocusedContainerColor.copy(alpha = 0.6F),
                ),
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5F))
                    .clickable(onClick = onUploadData)
                    .padding(16.dp)
            ) {
                Text(
                    text = i18n("module.ranking.upload_your_data"),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                )
            }
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(
                viewModel.songs.filter { (song, _) ->
                    val search = viewModel.searchBarState.text.toString().lowercase(getDefault())
                    or(
                        search in song.id.toString(),
                        search in song.name,
                        search in i18n("song_title.${song.name}").lowercase(getDefault()),
                    )
                }
            ) { (song, difficult) ->
                SongCard(
                    song = song,
                    difficult = difficult,
                    onSelect = { onSelect(song, difficult) },
                )
            }
        }
    }

    @Composable
    context(_: LocalizationContext)
    private fun SongCard(
        song: Song,
        difficult: Difficult,
        onSelect: () -> Unit,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(144.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = when (difficult) {
                        Minimal -> Colors.Ranking.SongCard.Background.minimal
                        Evolved -> Colors.Ranking.SongCard.Background.evolved
                        Ultimate -> Colors.Ranking.SongCard.Background.ultimate
                        Forbidden -> Colors.Ranking.SongCard.Background.forbidden
                    }
                )
                .padding(8.dp),
        ) {
            val resource = Res.allDrawableResources[song.name]
            resource?.let {
                Image(
                    painter = painterResource(it),
                    contentDescription = "Song Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(128.dp)
                        .clip(RoundedCornerShape(16.dp)),
                )
            } ?: run {
                Surface(
                    color = Color.White,
                    modifier = Modifier
                        .size(128.dp)
                        .clip(RoundedCornerShape(16.dp)),
                ) {}
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1F),
            ) {
                Text(
                    text = "${song.id} - ${i18n("song_title.${song.name}")}",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = difficult.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                )
            }
            Box(modifier = Modifier.size(80.dp, 48.dp)) {
                Icon(
                    painter = painterResource(Res.drawable.chevron_right),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onSelect),
                )
            }
        }
    }

    @Composable
    context(_: LocalizationContext)
    private fun SongRanking(
        song: Song,
        difficult: Difficult,
        onBack: () -> Unit,
        selfName: String,
        records: List<SongPlay>,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            val resource = Res.allDrawableResources[song.name]
            resource?.let {
                Image(
                    painter = painterResource(it),
                    contentDescription = "Song Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(128.dp)
                        .clip(RoundedCornerShape(16.dp)),
                )
            } ?: run {
                Surface(
                    color = Color.White,
                    modifier = Modifier
                        .size(128.dp)
                        .clip(RoundedCornerShape(16.dp)),
                ) {}
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1F),
            ) {
                Text(
                    text = "${song.id} - ${i18n("song_title.${song.name}")}",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = difficult.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
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
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = i18n("sidebar.module.back"),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                )
            }
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            itemsIndexed(records.sortedByDescending { it.score }) { index, record ->
                RecordCard(
                    record = record,
                    position = index + 1,
                    isSelf = record.user == selfName,
                )
            }
        }
    }

    @Composable
    private fun RecordCard(
        record: SongPlay,
        position: Int,
        isSelf: Boolean,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    when (isSelf) {
                        true -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)
                        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5F)
                    }
                )
                .padding(16.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1F),
            ) {
                Text(
                    text = record.user,
                    color = when (isSelf) {
                        true -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = scoreFormater.format(record.score),
                    color = when (isSelf) {
                        true -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                )
            }
            Text(
                text = "#$position",
                color = when (isSelf) {
                    true -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontSize = 20.sp,
                lineHeight = 24.sp,
            )
        }
    }
}