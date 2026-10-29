package cn.anling.inftool

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.anling.infalsustools.generated.resources.Res
import cn.anling.infalsustools.generated.resources.banner_story
import cn.anling.infalsustools.generated.resources.head_story
import cn.anling.infalsustools.generated.resources.home_background
import cn.anling.inftool.localization.LocalizationContext
import cn.anling.inftool.localization.i18n
import cn.anling.inftool.potential_algorithm.PotentialAlgorithm
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import java.text.DecimalFormat

@Composable
context(_: LocalizationContext)
fun PotentialImage(
    useNeoCard: Boolean,
    username: String,
    potential: Double,
    algorithm: PotentialAlgorithm,
    contents: Map<String, List<ProcessedDiveRecord>>,
) {
    val scope = rememberCoroutineScope()
    var scale by remember { mutableFloatStateOf(1.0F) }
    val animatedScale by animateFloatAsState(scale)
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(horizontalScrollState)
                .verticalScroll(verticalScrollState)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.first()
                            when (event.type) {
                                PointerEventType.Move -> {
                                    val positionChange = change.positionChange()
                                    if (
                                        and(
                                            change.type == PointerType.Touch,
                                            event.keyboardModifiers.isCtrlPressed,
                                            positionChange.x != 0F,
                                            positionChange.y == 0F,
                                        )
                                    ) {
                                        val delta = positionChange.x
                                        when {
                                            delta > 0 -> scale = (scale - 0.05f).coerceAtLeast(0.25f)
                                            delta < 0 -> scale = (scale + 0.05f).coerceAtMost(4.00f)
                                        }
                                    }
                                }

                                PointerEventType.Scroll -> {
                                    if (event.keyboardModifiers.isShiftPressed) {
                                        val delta = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f
                                        if (delta != 0F) {
                                            scope.launch {
                                                horizontalScrollState.animateScrollBy(delta * 200F)
                                            }
                                            event.changes.forEach { it.consume() }
                                        }
                                    }
                                }

                                else -> Unit
                            }
                        }
                    }
                },
        ) {
            Layout(
                content = {
                    PotentialImageContent(
                        useNeoCard = useNeoCard,
                        username = username,
                        potential = potential,
                        algorithm = algorithm,
                        contents = contents,
                    )
                },
            ) { measurables, constraints ->
                val content = measurables[0].measure(constraints.copy())

                layout((content.width * animatedScale).toInt(), (content.height * animatedScale).toInt()) {
                    content.placeWithLayer(0, 0) {
                        scaleX = animatedScale
                        scaleY = animatedScale
                        transformOrigin = TransformOrigin(0F, 0F)
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

@Composable
context(_: LocalizationContext)
fun PotentialImageContent(
    useNeoCard: Boolean,
    username: String,
    potential: Double,
    algorithm: PotentialAlgorithm,
    contents: Map<String, List<ProcessedDiveRecord>>,
) {
    Box {
        Image(
            painter = painterResource(Res.drawable.home_background),
            contentDescription = null,
            alignment = Alignment.BottomCenter,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .blur(16.dp),
        )

        Layout(
            content = {
                Title(
                    username = username,
                    potential = potential,
                    algorithm = algorithm,
                )

                Content(
                    useNeoCard = useNeoCard,
                    contents = contents,
                )
            },
            modifier = Modifier.padding(24.dp),
        ) { measurables, constraints ->
            val content = measurables[1].measure(constraints.copy())

            val width = content.width

            val title = measurables[0].measure(
                constraints.copy(
                    minWidth = width,
                    maxWidth = width,
                )
            )

            val height = title.height + content.height

            layout(width, height) {
                title.place(0, 0)
                content.place(0, title.height)
            }
        }
    }
}

@Composable
context(_: LocalizationContext)
private fun Title(
    username: String,
    potential: Double,
    algorithm: PotentialAlgorithm,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
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
                    style = LocalTextStyle.current.copy(
                        shadow = Shadow(blurRadius = 10.0F),
                    ),
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
                        text = formater.format(potential.floor(3)),
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 24.sp,
                        maxLines = 1,
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = buildString {
                    append(i18n("submodule.achievements_image.potential_algorithm"))
                    append(": ")
                    append(algorithm.name)
                },
                color = Color.White,
                fontSize = 36.sp,
                maxLines = 1,
                style = LocalTextStyle.current.copy(
                    shadow = Shadow(blurRadius = 10.0F),
                ),
            )
        }
    }
}

@Composable
context(_: LocalizationContext)
private fun Content(
    useNeoCard: Boolean,
    contents: Map<String, List<ProcessedDiveRecord>>,
) {
    Layout(
        content = {
            contents.forEach { (name, records) ->
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 64.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    style = LocalTextStyle.current.copy(
                        shadow = Shadow(blurRadius = 10.0F),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    maxItemsInEachRow = 5,
                ) {
                    records.forEachIndexed { index, record ->
                        val clearType = record.clearType
                        val difficult = record.difficult
                        val level = record.level
                        val titleKey = record.song.name.let { "song_title.$it" }
                        val image = record.song.name
                        val score = record.score
                        val potential = record.potential
                        val grading = record.grading
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
                                judgement = record.judgement
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
                                exactPlus = record.judgement.exactPlus,
                                exact = record.judgement.exact,
                                near = record.judgement.near,
                                `break` = record.judgement.`break`,
                            )
                        }
                    }
                }
            }
        },
    ) { measurables, constraints ->
        val contents = List(contents.size) { index ->
            measurables[index * 2 + 1].measure(constraints.copy())
        }
        val width = contents.maxOfOrNull { it.width } ?: 0
        val labels = List(contents.size) { index ->
            measurables[index * 2].measure(
                constraints.copy(
                    minWidth = width,
                    maxWidth = width,
                )
            )
        }

        val height = labels.sumOf { it.height } + contents.sumOf { it.height } + (contents.size * 2 - 1) * 16
        layout(width, height) {
            var posY = 0
            repeat(contents.size) { index ->
                val label = labels[index]
                label.place(0, posY)
                posY += label.height + 16

                val content = contents[index]
                content.place(0, posY)
                posY += content.height + 16
            }
        }
    }
}