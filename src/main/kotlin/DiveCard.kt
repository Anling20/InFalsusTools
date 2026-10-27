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
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.anling.infalsustools.generated.resources.*
import cn.anling.inftool.ClearType.*
import cn.anling.inftool.Difficult.*
import cn.anling.inftool.Grading.*
import cn.anling.inftool.localization.LocalizationContext
import cn.anling.inftool.localization.i18n
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import java.text.DecimalFormat

@Composable
context(_: LocalizationContext)
fun DiveCard(
    clearType: ClearType,
    difficult: Difficult,
    level: Double,
    titleKey: String,
    position: Int?,
    image: String?,
    score: Long,
    potential: Double,
    grading: Grading?,
    judgement: CountContainer?,
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .size(600.dp, 256.dp)
            .dropShadow(
                shape = RoundedCornerShape(12.dp),
                shadow = Shadow(
                    radius = 8.dp,
                    alpha = 0.3F,
                ),
            )
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = when (clearType) {
                    PerfectDive -> Colors.DiveCard.Normal.Background.perfectDive
                    FullLink -> Colors.DiveCard.Normal.Background.fullLink
                    DiveCleared -> Colors.DiveCard.Normal.Background.diveCleared
                    DiveFailed -> Colors.DiveCard.Normal.Background.diveFailed
                    None -> Colors.DiveCard.Normal.Background.none
                }
            ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(8.dp),
        ) {
            Title(
                difficult = difficult,
                level = level,
                titleKey = titleKey,
                position = position,
            )

            Content(
                image = image,
                score = score,
                potential = potential,
                maxLink = judgement?.maxLink,
                grading = grading,
            )

            JudgementContainer(
                judgement = judgement,
            )
        }
    }
}

@Composable
context(_: LocalizationContext)
private fun Title(
    difficult: Difficult,
    level: Double,
    titleKey: String,
    position: Int?,
) {
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
                append(i18n(titleKey))
            },
            color = when (difficult) {
                MIN -> Colors.SongCard.Difficult.minimal
                EVO -> Colors.SongCard.Difficult.evolved
                ULT -> Colors.SongCard.Difficult.ultimate
                FBD -> Colors.SongCard.Difficult.forbidden
            },
            fontSize = 24.sp,
            lineHeight = 26.sp,
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
                lineHeight = 26.sp,
            )
        }
    }
}

@Composable
context(_: LocalizationContext)
private fun Content(
    image: String?,
    score: Long,
    potential: Double,
    maxLink: Int?,
    grading: Grading?,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp),
    ) {
        val resource = image?.let { Res.allDrawableResources[it] }
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

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(48.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxHeight(),
            ) {
                Layout(
                    content = {
                        val scoreFormater = remember(Unit) { DecimalFormat("000,000,000") }
                        Text(
                            text = scoreFormater.format(score),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 36.sp,
                            lineHeight = 48.sp,
                            maxLines = 1,
                        )
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            val potentialFormater = remember(Unit) { DecimalFormat("00.0000") }
                            Text(
                                text = i18n("component.dive_card.potential"),
                                color = MaterialTheme.colorScheme.onSurface.copy(0.8F),
                                fontSize = 16.sp,
                                maxLines = 1,
                            )
                            Text(
                                text = potentialFormater.format(potential),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 24.sp,
                                lineHeight = 32.sp,
                                maxLines = 1,
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = i18n("component.dive_card.max_link"),
                                color = MaterialTheme.colorScheme.onSurface.copy(0.8F),
                                fontSize = 16.sp,
                                maxLines = 1,
                            )
                            Text(
                                text = maxLink?.toString() ?: "?",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 24.sp,
                                lineHeight = 32.sp,
                                maxLines = 1,
                            )
                        }
                    },
                ) { measurables, constraints ->
                    val score = measurables[0].measure(constraints.copy())

                    val width = score.width
                    val rowConstraints = constraints.copy(
                        minWidth = width,
                        maxWidth = width,
                    )

                    val rows = measurables.drop(1).map {
                        it.measure(rowConstraints)
                    }

                    val height = score.height + rows.sumOf { it.height } + 4

                    layout(width, height) {
                        var y = 0
                        score.placeRelative(0, y)
                        y += score.height + 4

                        rows.forEach {
                            it.placeRelative(0, y)
                            y += it.height
                        }
                    }
                }

                when (grading != null) {
                    true -> Image(
                        painter = painterResource(
                            when (grading) {
                                EXPlus -> Res.drawable.grading_cube_ex_plus
                                EX -> Res.drawable.grading_cube_ex
                                AA -> Res.drawable.grading_cube_aa
                                A -> Res.drawable.grading_cube_a
                                B -> Res.drawable.grading_cube_b
                                C -> Res.drawable.grading_cube_c
                                D -> Res.drawable.grading_cube_d
                            }
                        ),
                        contentDescription = "Grading",
                        modifier = Modifier.size(96.dp),
                    )

                    else -> Box(modifier = Modifier.size(96.dp))
                }
            }
        }
    }
}

@Composable
context(_: LocalizationContext)
private fun JudgementContainer(judgement: CountContainer?) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
    ) {
        Judgment(
            background = Colors.DiveCard.JudgementCount.Background.exactPlus,
            image = Res.drawable.judgement_exact_plus,
            total = judgement?.exactPlus,
            early = null,
            late = null,
            displayDetails = false,
        )

        Judgment(
            background = Colors.DiveCard.JudgementCount.Background.exact,
            image = Res.drawable.judgement_exact,
            total = judgement?.let { it.exactEarly + it.exactLate },
            early = judgement?.exactEarly,
            late = judgement?.exactLate,
            displayDetails = true,
        )

        Judgment(
            background = Colors.DiveCard.JudgementCount.Background.near,
            image = Res.drawable.judgement_near,
            total = judgement?.let { it.nearEarly + it.nearLate },
            early = judgement?.nearEarly,
            late = judgement?.nearLate,
            displayDetails = true,
        )

        Judgment(
            background = Colors.DiveCard.JudgementCount.Background.`break`,
            image = Res.drawable.judgement_break,
            total = judgement?.`break`,
            early = null,
            late = null,
            displayDetails = false,
        )
    }
}

@Composable
context(_: LocalizationContext)
private fun RowScope.Judgment(
    background: Brush,
    image: DrawableResource,
    total: Int?,
    early: Int?,
    late: Int?,
    displayDetails: Boolean,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .weight(1.0F)
            .height(70.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp),
        ) {
            Image(
                painter = painterResource(image),
                contentDescription = null,
                modifier = Modifier.height(16.dp),
            )
            Text(
                text = total?.toString() ?: "?",
                color = Color.White,
                fontSize = 16.sp,
                lineHeight = 18.sp,
                maxLines = 1,
                style = LocalTextStyle.current.copy(
                    shadow = androidx.compose.ui.graphics.Shadow(blurRadius = 2F),
                ),
            )
        }

        if (displayDetails) {
            Column {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp),
                ) {
                    Text(
                        text = i18n("component.dive_card.early"),
                        color = Colors.DiveCard.JudgementCount.early,
                        fontSize = 16.sp,
                        lineHeight = 18.sp,
                        maxLines = 1,
                        style = LocalTextStyle.current.copy(
                            shadow = androidx.compose.ui.graphics.Shadow(blurRadius = 2F),
                        ),
                        modifier = Modifier.width(48.dp),
                    )
                    Text(
                        text = early?.toString() ?: "?",
                        color = Color.White,
                        fontSize = 16.sp,
                        textAlign = TextAlign.End,
                        lineHeight = 18.sp,
                        maxLines = 1,
                        style = LocalTextStyle.current.copy(
                            shadow = androidx.compose.ui.graphics.Shadow(blurRadius = 2F),
                        ),
                        modifier = Modifier.width(48.dp),
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp),
                ) {
                    Text(
                        text = i18n("component.dive_card.late"),
                        color = Colors.DiveCard.JudgementCount.late,
                        fontSize = 16.sp,
                        lineHeight = 18.sp,
                        maxLines = 1,
                        style = LocalTextStyle.current.copy(
                            shadow = androidx.compose.ui.graphics.Shadow(blurRadius = 2F),
                        ),
                        modifier = Modifier.width(48.dp),
                    )
                    Text(
                        text = late?.toString() ?: "?",
                        color = Color.White,
                        fontSize = 16.sp,
                        textAlign = TextAlign.End,
                        lineHeight = 18.sp,
                        maxLines = 1,
                        style = LocalTextStyle.current.copy(
                            shadow = androidx.compose.ui.graphics.Shadow(blurRadius = 2F),
                        ),
                        modifier = Modifier.width(48.dp),
                    )
                }
            }
        }
    }
}