package cn.anling.inftool

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.anling.infalsustools.generated.resources.Res
import cn.anling.infalsustools.generated.resources.banner_story
import cn.anling.infalsustools.generated.resources.head_story
import cn.anling.inftool.ClearType.None
import cn.anling.inftool.Difficult.*
import cn.anling.inftool.song.Songs
import cn.anling.inftool.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import java.text.DecimalFormat

private data class AhvData(
    val song: Song,
    val difficult: Difficult,
    val diveRecord: ProcessedDiveRecord?,
)

@Composable
fun Ahv(
    darkTheme: Boolean,
    onSizeChanged: (IntSize) -> Unit,
    username: String,
    data: List<ProcessedDiveRecord>,
    experimentalLevel: Boolean,
    min: Double?,
    max: Double?,
    difficulties: List<Difficult>,
) {
    AppTheme(darkTheme) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
                .verticalScroll(rememberScrollState()),
        ) {
            val potential = remember(Unit) { data.potential() }
            val groupedData = Songs.entries.flatMap { song ->
                listOf(
                    AhvData(
                        song = song,
                        difficult = MIN,
                        diveRecord = data.find { it.song == song && it.difficult == MIN }
                    ),
                    AhvData(
                        song = song,
                        difficult = EVO,
                        diveRecord = data.find { it.song == song && it.difficult == EVO }
                    ),
                    AhvData(
                        song = song,
                        difficult = ULT,
                        diveRecord = data.find { it.song == song && it.difficult == ULT }
                    ),
                    AhvData(
                        song = song,
                        difficult = FBD,
                        diveRecord = data.find { it.song == song && it.difficult == FBD }
                    ),
                )
            }.filter {
                difficulties.isEmpty() || it.difficult in difficulties
            }.groupBy { ahvData ->
                when (ahvData.difficult) {
                    MIN -> ahvData.song.minimal
                    EVO -> ahvData.song.evolved
                    ULT -> ahvData.song.ultimate
                    FBD -> ahvData.song.forbidden
                }.let {
                    when (experimentalLevel) {
                        true -> it
                        else -> it.toInt().toDouble()
                    }
                }
            }.filterKeys {
                min == null || it >= min
            }.filterKeys {
                max == null || it <= max
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .wrapContentWidth()
                    .onSizeChanged(onSizeChanged)
                    .padding(24.dp),
            ) {
                // Head
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(Res.drawable.head_story),
                        contentDescription = "Head image",
                        modifier = Modifier.size(256.dp),
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = username,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 48.sp,
                            maxLines = 1,
                        )
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            val formater = remember(Unit) {
                                DecimalFormat("0.000")
                            }
                            Image(
                                painter = painterResource(Res.drawable.banner_story),
                                contentDescription = "Banner image",
                                modifier = Modifier.size(256.dp, 64.dp),
                            )
                            Text(
                                text = formater.format(potential),
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 24.sp,
                                maxLines = 1,
                            )
                        }
                    }
                }

                // Content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(64.dp),
                ) {
                    groupedData.keys.sortedDescending().forEachIndexed { index, potential ->
                        if (index > 0) {
                            HorizontalDivider(
                                thickness = 2.dp,
                                color = Color.Transparent,
                                modifier = Modifier
                                    .width(1500.dp)
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.0F),
                                                MaterialTheme.colorScheme.onBackground.copy(alpha = 1.0F),
                                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.0F),
                                            )
                                        ),
                                    ),
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                        ) {
                            val formater = remember(Unit) { DecimalFormat("0.0") }
                            Text(
                                text = formater.format(potential),
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 64.sp,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(200.dp),
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(24.dp),
                                verticalArrangement = Arrangement.spacedBy(24.dp),
                                maxItemsInEachRow = 5,
                                modifier = Modifier.width(2256.dp),
                            ) {
                                groupedData[potential]!!.sortedByDescending {
                                    it.diveRecord?.potential ?: 0.0
                                }.forEach { (song, difficult, diveRecord) ->
                                    SongCard(
                                        clearType = diveRecord?.clearType ?: None,
                                        grading = diveRecord?.grading,
                                        difficult = difficult,
                                        level = potential,
                                        name = song.title,
                                        position = null,
                                        image = song.name,
                                        score = diveRecord?.score ?: 0,
                                        potential = diveRecord?.potential ?: 0.0,
                                        exactPlus = diveRecord?.count?.exactPlus,
                                        exact = diveRecord?.count?.exact,
                                        near = diveRecord?.count?.near,
                                        `break` = diveRecord?.count?.`break`,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}