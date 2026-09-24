package cn.anling.inftool

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.anling.infalsustools.generated.resources.Res
import cn.anling.infalsustools.generated.resources.allDrawableResources
import cn.anling.inftool.ClearType.DiveCleared
import cn.anling.inftool.ClearType.DiveFailed
import cn.anling.inftool.ClearType.FullLink
import cn.anling.inftool.ClearType.PerfectDive
import cn.anling.inftool.Difficult.EVO
import cn.anling.inftool.Difficult.FBD
import cn.anling.inftool.Difficult.MIN
import cn.anling.inftool.Difficult.ULT
import org.jetbrains.compose.resources.painterResource
import java.text.DecimalFormat
import kotlin.text.appendLine

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