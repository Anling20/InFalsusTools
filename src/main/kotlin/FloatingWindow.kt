package cn.anling.inftool

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.anling.infalsustools.generated.resources.Res
import cn.anling.infalsustools.generated.resources.floating_window_background
import cn.anling.infalsustools.generated.resources.floating_window_background2
import cn.anling.infalsustools.generated.resources.head_story
import cn.anling.inftool.localization.LocalizationContext
import cn.anling.inftool.localization.i18n
import cn.anling.inftool.theme.exo2
import cn.anling.inftool.theme.notosans
import org.jetbrains.compose.resources.painterResource
import java.text.DecimalFormat

data class PlayResult(
    val level: Double,
    val potential: Double,
    val additionPotential: Double,
)

@Composable
context(_: LocalizationContext)
fun FloatingWindow(
    scale: Float,
    username: String,
    potential: Double,
    playResult: PlayResult?,
) {
    val additionPotentialFormater = remember(Unit) {
        DecimalFormat("0.000")
    }
    val levelFormater = remember(Unit) {
        DecimalFormat("0.0")
    }
    val potentialFormater = remember(Unit) {
        DecimalFormat("00.0000")
    }

    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .wrapContentSize(unbounded = true)
            .scale(scale),
    ) {
        Box(
            modifier = Modifier.size(768.dp, 128.dp),
        ) {
            Image(
                painter = painterResource(Res.drawable.floating_window_background),
                contentDescription = null,
                modifier = Modifier.size(768.dp, 96.dp),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(120.dp),
                modifier = Modifier.padding(start = 60.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.width(220.dp),
                ) {
                    Image(
                        painter = painterResource(Res.drawable.head_story),
                        contentDescription = "Head image",
                        modifier = Modifier.size(96.dp),
                    )

                    Text(
                        text = username,
                        color = Color.White,
                        fontSize = 24.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Right,
                        style = LocalTextStyle.current.copy(fontFamily = notosans),
                        modifier = Modifier.width(100.dp),
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(50.dp),
                ) {
                    Text(
                        text = "PTT",
                        color = Color.White,
                        fontSize = 24.sp,
                        maxLines = 1,
                        style = LocalTextStyle.current.copy(fontFamily = exo2)
                    )

                    AnimatedContent(potential) {
                        Text(
                            text = additionPotentialFormater.format(it),
                            color = Color.White,
                            fontSize = 24.sp,
                            maxLines = 1,
                            style = LocalTextStyle.current.copy(fontFamily = exo2)
                        )
                    }

                    AnimatedContent(playResult) {
                        if (it != null) {
                            Text(
                                text = when {
                                    it.additionPotential > 0 -> "+" + additionPotentialFormater.format(it.additionPotential)
                                    it.additionPotential < 0 -> additionPotentialFormater.format(it.additionPotential)
                                    else -> "Keep"
                                },
                                color = when {
                                    it.additionPotential > 0 -> Colors.PotentialWindow.potentialIncrease
                                    it.additionPotential < 0 -> Colors.PotentialWindow.potentialDecrease
                                    else -> Colors.PotentialWindow.potentialKeep
                                },
                                fontSize = 24.sp,
                                maxLines = 1,
                                style = LocalTextStyle.current.copy(fontFamily = exo2)
                            )
                        }
                    }
                }
            }
            AnimatedContent(
                targetState = playResult,
                modifier = Modifier.padding(start = 350.dp, top = 81.dp),
            ) {
                if (it != null) {
                    Image(
                        painter = painterResource(Res.drawable.floating_window_background2),
                        contentDescription = null,
                        modifier = Modifier.size(300.dp, 40.dp),
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(300.dp, 35.dp),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.width(225.dp),
                        ) {
                            Text(
                                text = i18n("submodule.potential_window.play_potential"),
                                color = Color.White.copy(alpha = 0.75F),
                                fontSize = 16.sp,
                                lineHeight = 16.sp,
                                maxLines = 1,
                                style = LocalTextStyle.current.copy(fontFamily = notosans)
                            )
                            Text(
                                text = buildString {
                                    append(levelFormater.format(it.level))
                                    append("  >  ")
                                    append(potentialFormater.format(it.potential))
                                },
                                color = Color.White.copy(alpha = 0.75F),
                                fontSize = 16.sp,
                                lineHeight = 16.sp,
                                maxLines = 1,
                                style = LocalTextStyle.current.copy(fontFamily = exo2)
                            )
                        }
                    }
                }
            }
        }
    }
}