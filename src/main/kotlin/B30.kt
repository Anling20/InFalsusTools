package cn.anling.inftool

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.anling.infalsustools.generated.resources.Res
import cn.anling.infalsustools.generated.resources.banner_story
import cn.anling.infalsustools.generated.resources.head_story
import cn.anling.infalsustools.generated.resources.home_background
import cn.anling.inftool.ClearType.None
import cn.anling.inftool.Difficult.ULT
import cn.anling.inftool.localization.LocalizationContext
import cn.anling.inftool.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import java.text.DecimalFormat

@OptIn(ExperimentalComposeUiApi::class)
@Composable
context(_: LocalizationContext)
fun B30(
    darkTheme: Boolean,
    onSizeChanged: (IntSize) -> Unit,
    useNeoCard: Boolean,
    username: String,
    highScores: List<ProcessedDiveRecord>,
) {
    AppTheme(darkTheme) {
        var scale by remember { mutableFloatStateOf(1.0F) }
        val horizontalScrollState = rememberScrollState()
        val verticalScrollState = rememberScrollState()
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScrollState)
                    .verticalScroll(verticalScrollState)
                    .onPointerEvent(PointerEventType.Scroll) { event ->
                        if (event.keyboardModifiers.isCtrlPressed) {
                            val delta = event.changes.firstOrNull()?.scrollDelta?.y ?: 0F
                            when {
                                delta > 0 -> scale = (scale - 0.1f).coerceAtLeast(0.25f)
                                delta < 0 -> scale = (scale + 0.1f).coerceAtMost(4.00f)
                            }

                            event.changes.forEach { it.consume() }
                        }
                    },
            ) {
                Layout(
                    content = {
                        Image(
                            painter = painterResource(Res.drawable.home_background),
                            contentDescription = null,
                            alignment = Alignment.BottomCenter,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.blur(16.dp),
                        )

                        Box {
                            val sortedData = highScores.sortedByDescending {
                                it.potential
                            }
                            val potential = remember(Unit) { highScores.potential().floor(3) }

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
                                            color = Color.White,
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
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                                    verticalArrangement = Arrangement.spacedBy(24.dp),
                                    maxItemsInEachRow = 5,
                                ) {
                                    repeat(30) { index ->
                                        val diveRecord = sortedData.getOrNull(index)
                                        val clearType = diveRecord?.clearType ?: None
                                        val difficult = diveRecord?.difficult ?: ULT
                                        val level = diveRecord?.level ?: 0.0
                                        val titleKey = diveRecord?.song?.name?.let { "song_title.$it" } ?: "?"
                                        val image = diveRecord?.song?.name
                                        val score = diveRecord?.score ?: 0
                                        val potential = diveRecord?.potential ?: 0.0
                                        val grading = diveRecord?.grading
                                        when (useNeoCard) {
                                            true -> DiveCard(
                                                clearType = clearType,
                                                difficult = difficult,
                                                level = level,
                                                titleKey = titleKey,
                                                position = index + 1,
                                                image = image,
                                                score = score,
                                                potential = potential,
                                                grading = grading,
                                                judgement = diveRecord?.judgement
                                            )

                                            else -> SongCard(
                                                clearType = clearType,
                                                grading = grading,
                                                difficult = difficult,
                                                level = level,
                                                titleKey = titleKey,
                                                position = index + 1,
                                                image = image,
                                                score = score,
                                                potential = potential,
                                                exactPlus = diveRecord?.judgement?.exactPlus,
                                                exact = diveRecord?.judgement?.exact,
                                                near = diveRecord?.judgement?.near,
                                                `break` = diveRecord?.judgement?.`break`,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                ) { measurables, constraints ->
                    val content = measurables[1].measure(
                        constraints.copy(minWidth = 0, minHeight = 0)
                    )

                    val width = (content.width * scale).toInt()
                    val height = (content.height * scale).toInt()

                    val background = measurables[0].measure(
                        constraints.copy(
                            minWidth = width,
                            maxWidth = width,
                            minHeight = height,
                            maxHeight = height,
                        )
                    )

                    layout(width, height) {
                        background.place(0, 0)

                        content.placeWithLayer(0, 0) {
                            scaleX = scale
                            scaleY = scale
                            transformOrigin = TransformOrigin(0.0F, 0.0F)
                        }
                    }
                }
            }
            HorizontalScrollbar(
                adapter = rememberScrollbarAdapter(horizontalScrollState),
                modifier = Modifier
                    .align(Alignment.BottomCenter),
            )
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(verticalScrollState),
                modifier = Modifier
                    .align(Alignment.CenterEnd),
            )
        }
    }
}