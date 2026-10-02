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
import cn.anling.infalsustools.generated.resources.head_story
import cn.anling.inftool.theme.AppTheme
import cn.anling.inftool.theme.exo2
import cn.anling.inftool.theme.notosans
import org.jetbrains.compose.resources.painterResource
import java.text.DecimalFormat

@Composable
fun FloatingWindow(
    scale: Float,
    username: String,
    potential: Double,
    additionPotential: Double?,
) {
    val formater = remember(Unit) {
        DecimalFormat("0.000")
    }

    AppTheme(false) {
        Surface(
            color = Color.Transparent,
            modifier = Modifier
                .wrapContentSize(unbounded = true)
                .scale(scale),
        ) {
            Box {
                Image(
                    painter = painterResource(Res.drawable.floating_window_background),
                    contentDescription = null,
                    modifier = Modifier.size(768.dp, 128.dp),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(120.dp),
                    modifier = Modifier.padding(start = 60.dp, top = 15.dp),
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
                                text = formater.format(it),
                                color = Color.White,
                                fontSize = 24.sp,
                                maxLines = 1,
                                style = LocalTextStyle.current.copy(fontFamily = exo2)
                            )
                        }

                        AnimatedContent(additionPotential) {
                            if (it != null) {
                                Text(
                                    text = "+" + formater.format(it),
                                    color = Color(83, 225, 230),
                                    fontSize = 24.sp,
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
}