package cn.anling.inftool

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.anling.infalsustools.generated.resources.Res
import cn.anling.infalsustools.generated.resources.banner_story
import cn.anling.infalsustools.generated.resources.head_story
import cn.anling.infalsustools.generated.resources.home_background
import cn.anling.inftool.ClearType.None
import cn.anling.inftool.Difficult.*
import cn.anling.inftool.localization.LocalizationContext
import cn.anling.inftool.localization.i18n
import cn.anling.inftool.potential_algorithm.PotentialAlgorithm
import cn.anling.inftool.song.LevelMap
import cn.anling.inftool.song.Song
import cn.anling.inftool.song.Songs
import org.jetbrains.compose.resources.painterResource
import java.text.DecimalFormat

private data class AhvData(
    val song: Song,
    val difficult: Difficult,
    val diveRecord: ProcessedDiveRecord?,
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
context(_: LocalizationContext)
fun AchievementsImage(
    useNeoCard: Boolean,
    username: String,
    potential: Double,
    algorithm: PotentialAlgorithm,
    highScores: List<ProcessedDiveRecord>,
    levelMap: LevelMap,
    min: Double?,
    max: Double?,
    difficulties: List<Difficult>,
) {
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
                    AchievementsImageContent(
                        useNeoCard = useNeoCard,
                        username = username,
                        potential = potential,
                        algorithm = algorithm,
                        highScores = highScores,
                        levelMap = levelMap,
                        min = min,
                        max = max,
                        difficulties = difficulties,
                    )
                },
            ) { measurables, constraints ->
                val content = measurables[0].measure(constraints.copy())

                layout((content.width * scale).toInt(), (content.height * scale).toInt()) {
                    content.placeWithLayer(0, 0) {
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin(0F, 0F)
                    }
                }
            }
        }
        HorizontalScrollbar(
            adapter = rememberScrollbarAdapter(horizontalScrollState),
            modifier = Modifier.align(Alignment.BottomCenter),
        )
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(verticalScrollState),
            modifier = Modifier.align(Alignment.CenterEnd),
        )
    }
}

@Composable
context(_: LocalizationContext)
fun AchievementsImageContent(
    useNeoCard: Boolean,
    username: String,
    potential: Double,
    algorithm: PotentialAlgorithm,
    highScores: List<ProcessedDiveRecord>,
    levelMap: LevelMap,
    min: Double?,
    max: Double?,
    difficulties: List<Difficult>,
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

                val groupedData = Songs.entries
                    .flatMap { song ->
                        listOf(
                            AhvData(
                                song = song,
                                difficult = MIN,
                                diveRecord = highScores.find { it.song == song && it.difficult == MIN }
                            ),
                            AhvData(
                                song = song,
                                difficult = EVO,
                                diveRecord = highScores.find { it.song == song && it.difficult == EVO }
                            ),
                            AhvData(
                                song = song,
                                difficult = ULT,
                                diveRecord = highScores.find { it.song == song && it.difficult == ULT }
                            ),
                            AhvData(
                                song = song,
                                difficult = FBD,
                                diveRecord = highScores.find { it.song == song && it.difficult == FBD }
                            ),
                        )
                    }
                    .filter { difficulties.isEmpty() || it.difficult in difficulties }
                    .groupBy { ahvData ->
                        levelMap[ahvData.song.id]?.run {
                            when (ahvData.difficult) {
                                MIN -> minimal
                                EVO -> evolved
                                ULT -> ultimate
                                FBD -> forbidden
                            }
                        } ?: 0.0
                    }
                    .filterKeys { min == null || it >= min }
                    .filterKeys { max == null || it <= max }
                Content(
                    groupedData = groupedData,
                    useNeoCard = useNeoCard,
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

            val height = title.height + 24 + content.height

            layout(width, height) {
                title.place(0, 0)
                content.place(0, title.height + 24)
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
    groupedData: Map<Double, List<AhvData>>,
    useNeoCard: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
        groupedData.keys.sortedDescending().forEachIndexed { index, level ->
            if (index > 0) {
                HorizontalDivider(
                    thickness = 2.dp,
                    color = Color.Transparent,
                    modifier = Modifier
                        .width(1500.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                0.00F to MaterialTheme.colorScheme.outline.copy(alpha = 0.0F),
                                0.25F to MaterialTheme.colorScheme.outline.copy(alpha = 1.0F),
                                0.75F to MaterialTheme.colorScheme.outline.copy(alpha = 1.0F),
                                1.00F to MaterialTheme.colorScheme.outline.copy(alpha = 0.0F),
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
                    text = formater.format(level),
                    color = Color.White,
                    fontSize = 64.sp,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(200.dp),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    maxItemsInEachRow = 5,
                ) {
                    groupedData[level]!!
                        .sortedByDescending { it.diveRecord?.potential ?: 0.0 }
                        .forEach { (song, difficult, diveRecord) ->
                            val clearType = diveRecord?.clearType ?: None
                            val titleKey = "song_title.${song.name}"
                            val image = song.name
                            val score = diveRecord?.score ?: 0
                            val potential = diveRecord?.potential ?: 0.0
                            val grading = diveRecord?.grading
                            when (useNeoCard) {
                                true -> DiveCard(
                                    clearType = clearType,
                                    difficult = difficult,
                                    level = level,
                                    titleKey = titleKey,
                                    position = null,
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
                                    position = null,
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
}