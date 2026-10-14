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
fun DiveCardFlat(
    clearType: ClearType,
    image: String?,
    difficult: Difficult,
    level: Double,
    titleKey: String,
    score: Long,
    potential: Double,
    grading: Grading?,
    judgement: CountContainer?,
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .height(144.dp)
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
                    PerfectDive -> Colors.DiveCard.Flat.Background.perfectDive
                    FullLink -> Colors.DiveCard.Flat.Background.fullLink
                    DiveCleared -> Colors.DiveCard.Flat.Background.diveCleared
                    DiveFailed -> Colors.DiveCard.Flat.Background.diveFailed
                    None -> Colors.DiveCard.Flat.Background.none
                }
            ),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(8.dp),
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

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                Content(
                    difficult = difficult,
                    level = level,
                    titleKey = titleKey,
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
}

@Composable
context(_: LocalizationContext)
private fun Content(
    difficult: Difficult,
    level: Double,
    titleKey: String,
    score: Long,
    potential: Double,
    maxLink: Int?,
    grading: Grading?,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.width(400.dp),
        ) {
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
                style = LocalTextStyle.current.copy(
                    brush = when (difficult) {
                        MIN -> Colors.SongCard.Difficult.minimal
                        EVO -> Colors.SongCard.Difficult.evolved
                        ULT -> Colors.SongCard.Difficult.ultimate
                        FBD -> Colors.SongCard.Difficult.forbidden
                    }
                ),
                fontSize = 20.sp,
                maxLines = 1,
                lineHeight = 26.sp,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.width(132.dp),
            ) {
                Text(
                    text = i18n("component.dive_card.max_link"),
                    color = MaterialTheme.colorScheme.onSurface.copy(0.8F),
                    fontSize = 10.sp,
                    maxLines = 1,
                )
                Text(
                    text = maxLink?.toString() ?: "?",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 1,
                )
            }
        }

        Layout(
            content = {
                val scoreFormater = remember(Unit) { DecimalFormat("000,000,000") }
                Text(
                    text = scoreFormater.format(score),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,
                    lineHeight = 26.sp,
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
                        fontSize = 10.sp,
                        maxLines = 1,
                    )
                    Text(
                        text = potentialFormater.format(potential),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
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
                modifier = Modifier.size(48.dp),
            )

            else -> Box(modifier = Modifier.size(48.dp))
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
            .height(60.dp),
    ) {
        Judgement(
            background = Colors.DiveCard.JudgementCount.Background.exactPlus,
            image = Res.drawable.judgement_exact_plus,
            total = judgement?.exactPlus,
            early = null,
            late = null,
            displayDetails = false,
        )

        Judgement(
            background = Colors.DiveCard.JudgementCount.Background.exact,
            image = Res.drawable.judgement_exact,
            total = judgement?.let { it.exactEarly + it.exactLate },
            early = judgement?.exactEarly,
            late = judgement?.exactLate,
            displayDetails = true,
        )

        Judgement(
            background = Colors.DiveCard.JudgementCount.Background.near,
            image = Res.drawable.judgement_near,
            total = judgement?.let { it.nearEarly + it.nearLate },
            early = judgement?.nearEarly,
            late = judgement?.nearLate,
            displayDetails = true,
        )

        Judgement(
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
private fun RowScope.Judgement(
    background: Brush,
    image: DrawableResource,
    total: Int?,
    early: Int?,
    late: Int?,
    displayDetails: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .weight(1.0F)
            .height(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 12.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier,
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
            )
        }

        if (displayDetails) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(18.dp),
                ) {
                    Text(
                        text = i18n("component.dive_card.early"),
                        color = Colors.DiveCard.JudgementCount.early,
                        fontSize = 16.sp,
                        lineHeight = 18.sp,
                        maxLines = 1,
                        modifier = Modifier.width(48.dp),
                    )
                    Text(
                        text = early?.toString() ?: "?",
                        color = Color.White,
                        fontSize = 16.sp,
                        textAlign = TextAlign.End,
                        lineHeight = 18.sp,
                        maxLines = 1,
                        modifier = Modifier.width(48.dp),
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(18.dp),
                ) {
                    Text(
                        text = i18n("component.dive_card.late"),
                        color = Colors.DiveCard.JudgementCount.late,
                        fontSize = 16.sp,
                        lineHeight = 18.sp,
                        maxLines = 1,
                        modifier = Modifier.width(48.dp),
                    )
                    Text(
                        text = late?.toString() ?: "?",
                        color = Color.White,
                        fontSize = 16.sp,
                        textAlign = TextAlign.End,
                        lineHeight = 18.sp,
                        maxLines = 1,
                        modifier = Modifier.width(48.dp),
                    )
                }
            }
        }
    }
}