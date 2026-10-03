package cn.anling.inftool

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.anling.infalsustools.generated.resources.*
import cn.anling.inftool.ClearType.*
import cn.anling.inftool.Difficult.*
import cn.anling.inftool.Grading.*
import org.jetbrains.compose.resources.painterResource
import java.text.DecimalFormat

@Composable
fun SongCard(
    clearType: ClearType,
    grading: Grading?,
    difficult: Difficult,
    level: Double,
    name: String,
    position: Int?,
    image: String?,
    score: Long,
    potential: Double,
    exactPlus: Int?,
    exact: Int?,
    near: Int?,
    `break`: Int?,
) {
    Surface(
        color = Color.Transparent,
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
            ).clip(RoundedCornerShape(24.dp))
            .background(
                brush = when (clearType) {
                    PerfectDive -> Colors.SongCard.Background.perfectDive
                    FullLink -> Colors.SongCard.Background.fullLink
                    DiveCleared -> Colors.SongCard.Background.diveCleared
                    DiveFailed -> Colors.SongCard.Background.diveFailed
                    None -> Colors.SongCard.Background.none
                }
            ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(8.dp),
        ) {
            // Head
            Row {
                val formater = remember(Unit) {
                    DecimalFormat("0.0")
                }
                Text(
                    text = buildString {
                        append('[')
                        append(difficult.name)
                        append(' ')
                        append(formater.format(level))
                        append(']')
                        append(name)
                    },
                    style = LocalTextStyle.current.copy(
                        brush = when (difficult) {
                            MIN -> Colors.SongCard.Difficult.minimal
                            EVO -> Colors.SongCard.Difficult.evolved
                            ULT -> Colors.SongCard.Difficult.ultimate
                            FBD -> Colors.SongCard.Difficult.forbidden
                        }
                    ),
                    fontSize = 24.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1F),
                )
                if (position != null) {
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
                            contentDescription = "Song Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(256.dp),
                        )
                    } ?: run {
                        Surface(
                            color = Color.White,
                            modifier = Modifier.size(256.dp),
                        ) {}
                    }
                    if (grading != null) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Brush.verticalGradient(listOf(Color(0, 0, 0, 0), Color(0, 0, 0, 255)))),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Image(
                                    painter = painterResource(
                                        when (grading) {
                                            EXPlus -> Res.drawable.grade_explus
                                            EX -> Res.drawable.grade_ex
                                            AA -> Res.drawable.grade_aa
                                            A -> Res.drawable.grade_a
                                            B -> Res.drawable.grade_b
                                            C -> Res.drawable.grade_c
                                            D -> Res.drawable.grade_d
                                        }
                                    ),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(64.dp, 32.dp),
                                )
                                val formater = remember(Unit) { DecimalFormat("000,000,000") }
                                Text(
                                    text = formater.format(score),
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    maxLines = 1,
                                )
                            }
                        }
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

                    Column {
                        Row {
                            Box {
                                Text(
                                    text = "E:${exactPlus ?: '?'}",
                                    style = LocalTextStyle.current.copy(
                                        color = Colors.SongCard.Judgement.ExactPlus.outline,
                                        drawStyle = Stroke(1.0F),
                                    ),
                                    fontSize = 20.sp,
                                    lineHeight = 26.sp,
                                    maxLines = 1,
                                )

                                Text(
                                    text = "E:${exactPlus ?: '?'}",
                                    style = LocalTextStyle.current.copy(Colors.SongCard.Judgement.ExactPlus.text),
                                    fontSize = 20.sp,
                                    lineHeight = 26.sp,
                                    maxLines = 1,
                                )
                            }

                            Box {
                                Text(
                                    text = "(-${exact ?: '?'})",
                                    style = LocalTextStyle.current.copy(
                                        color = Colors.SongCard.Judgement.Exact.outline,
                                        drawStyle = Stroke(1.0F),
                                    ),
                                    fontSize = 20.sp,
                                    lineHeight = 26.sp,
                                    maxLines = 1,
                                )

                                Text(
                                    text = "(-${exact ?: '?'})",
                                    style = LocalTextStyle.current.copy(Colors.SongCard.Judgement.Exact.text),
                                    fontSize = 20.sp,
                                    lineHeight = 26.sp,
                                    maxLines = 1,
                                )
                            }
                        }

                        Box {
                            Text(
                                text = "N:${near ?: '?'}",
                                style = LocalTextStyle.current.copy(
                                    color = Colors.SongCard.Judgement.Near.outline,
                                    drawStyle = Stroke(1.0F),
                                ),
                                fontSize = 20.sp,
                                lineHeight = 26.sp,
                                maxLines = 1,
                            )

                            Text(
                                text = "N:${near ?: '?'}",
                                style = LocalTextStyle.current.copy(Colors.SongCard.Judgement.Near.text),
                                fontSize = 20.sp,
                                lineHeight = 26.sp,
                                maxLines = 1,
                            )
                        }

                        Box {
                            Text(
                                text = "B:${`break` ?: '?'}",
                                style = LocalTextStyle.current.copy(
                                    color = Colors.SongCard.Judgement.Break.outline,
                                    drawStyle = Stroke(1.0F),
                                ),
                                fontSize = 20.sp,
                                lineHeight = 26.sp,
                                maxLines = 1,
                            )

                            Text(
                                text = "B:${`break` ?: '?'}",
                                style = LocalTextStyle.current.copy(Colors.SongCard.Judgement.Break.text),
                                fontSize = 20.sp,
                                lineHeight = 26.sp,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}