package cn.anling.inftool

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.anling.infalsustools.generated.resources.Res
import cn.anling.infalsustools.generated.resources.allDrawableResources
import cn.anling.infalsustools.generated.resources.banner_story
import cn.anling.infalsustools.generated.resources.head_story
import cn.anling.inftool.ClearType.*
import cn.anling.inftool.Difficult.*
import cn.anling.inftool.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import java.text.DecimalFormat

@Composable
fun App(
    onSizeChanged: (IntSize) -> Unit,
    username: String,
    data: List<ProcessedDiveRecord>,
) {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
                .verticalScroll(rememberScrollState()),
        ) {
            val processedB50 = remember(Unit) {
                data.sortedByDescending {
                    it.potential
                }.take(50)
            }

            val potential = remember(Unit) {
                val b10ptt = processedB50.take(10).map { it.potential }
                val b50ptt = processedB50.map { it.potential }
                (b10ptt.sum() + b50ptt.sum()) / 60.0
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
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    repeat(10) { x ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                        ) {
                            repeat(5) { y ->
                                val index = x * 5 + y
                                val data = processedB50.getOrNull(index)
                                // 物量有误, 待修
                                SongCard(
                                    clearType = data?.clearType ?: DiveFailed,
                                    difficult = data?.difficult ?: ULT,
                                    level = data?.level ?: 0.0,
                                    name = data?.song?.name ?: "",
                                    position = index + 1,
                                    image = data?.song?.id,
                                    score = data?.score ?: 0,
                                    potential = data?.potential ?: 0.0,
                                    exactPlus = null,
                                    exact = null,
                                    near = when (data?.clearType) {
                                        DiveFailed -> null
                                        DiveCleared -> null
                                        FullLink -> null
                                        PerfectDive -> 0
                                        null -> null
                                    },
                                    `break` = when (data?.clearType) {
                                        DiveFailed -> null
                                        DiveCleared -> null
                                        FullLink -> 0
                                        PerfectDive -> 0
                                        null -> null
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SongCard(
    clearType: ClearType,
    difficult: Difficult,
    level: Double,
    name: String,
    position: Int,
    image: String?,
    score: Int,
    potential: Double,
    exactPlus: Int?,
    exact: Int?,
    near: Int?,
    `break`: Int?,
) {
    Surface(
        color = when (clearType) {
            PerfectDive -> Colors.SongCard.Background.perfectDive
            FullLink -> Colors.SongCard.Background.fullLink
            DiveCleared -> Colors.SongCard.Background.diveCleared
            DiveFailed -> Colors.SongCard.Background.diveFailed
        },
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .size(432.dp, 308.dp)
            .dropShadow(
                shape = RoundedCornerShape(24.dp),
                shadow = Shadow(
                    radius = 4.dp,
                    spread = 0.dp,
                    offset = DpOffset(1.dp, 1.dp),
                    alpha = 0.5F,
                )
            ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(8.dp),
        ) {
            // Head
            Row {
                val formater = remember(Unit) {
                    DecimalFormat("[0.0]")
                }
                Text(
                    text = formater.format(level),
                    color = when (difficult) {
                        ULT -> Colors.SongCard.Difficult.ult
                        MIN -> Colors.SongCard.Difficult.min
                        EVO -> Colors.SongCard.Difficult.evo
                        FBD -> Colors.SongCard.Difficult.fbd
                    },
                    fontSize = 24.sp,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 24.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1F),
                )
                Spacer(
                    modifier = Modifier.width(32.dp),
                )
                Text(
                    text = "#$position",
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
            }

            // Content
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(256.dp)
                        .dropShadow(
                            shape = RoundedCornerShape(16.dp),
                            shadow = Shadow(
                                radius = 4.dp,
                                spread = 0.dp,
                                offset = DpOffset(1.dp, 1.dp),
                                alpha = 0.5F,
                            )
                        )
                        .clip(RoundedCornerShape(16.dp)),
                ) {
                    val resource = image?.let { Res.allDrawableResources[it] }
                    resource?.let {
                        Image(
                            painter = painterResource(it),
                            contentDescription = "Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(256.dp),
                        )
                    } ?: run {
                        Surface(
                            color = Color.White,
                            modifier = Modifier.size(256.dp),
                        ) {}
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Brush.verticalGradient(listOf(Color(0, 0, 0, 0), Color(0, 0, 0, 255)))),
                    ) {
                        val formater = remember(Unit) { DecimalFormat("000,000,000") }
                        Text(
                            text = formater.format(score),
                            color = Color.White,
                            fontSize = 24.sp,
                            maxLines = 1,
                        )
                    }
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val formater = remember(Unit) { DecimalFormat("00.0000") }
                    Text(
                        text = formater.format(potential),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 36.sp,
                        maxLines = 1,
                    )

                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Colors.SongCard.Judgement.exactPlus)) {
                                append("E:${exactPlus ?: '?'}")
                            }
                            withStyle(SpanStyle(color = Colors.SongCard.Judgement.exact)) {
                                append("(-${exact ?: '?'})")
                            }
                            appendLine()
                            withStyle(SpanStyle(color = Colors.SongCard.Judgement.near)) {
                                appendLine("N:${near ?: '?'}")
                            }
                            withStyle(SpanStyle(color = Colors.SongCard.Judgement.`break`)) {
                                appendLine("B:${`break` ?: '?'}")
                            }
                        },
                        fontSize = 20.sp,
                        lineHeight = 26.sp,
                        maxLines = 3,
                    )
                }
            }
        }
    }
}