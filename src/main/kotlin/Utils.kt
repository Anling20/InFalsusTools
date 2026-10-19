package cn.anling.inftool

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.singleWindowApplication
import cn.anling.inftool.parser.ParsedJudgementTypeCount
import cn.anling.inftool.parser.ParsedRecord
import cn.anling.inftool.potential_algorithm.PotentialAlgorithm
import cn.anling.inftool.song.Song
import cn.anling.inftool.song.Songs
import cn.anling.inftool.theme.AppTheme
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds

fun ParsedRecord.toDiveRecord(): DiveRecord = DiveRecord(
    id = songId,
    name = baseName,
    difficult = Difficult.entries[difficultyIndex],
    score = score,
    clearType = ClearType.entries[resultClear],
    diveValue = paceValue,
    judgement = CountContainer(
        maxLink = maxCombo,
        tap = counts.tap.toJudgementCount(),
        hold = counts.hold.toJudgementCount(),
        field = counts.skyArea.toJudgementCount(),
        flick = counts.flick.toJudgementCount(),
    ),
)

fun ParsedJudgementTypeCount.toJudgementCount(): JudgementCount = JudgementCount(
    exactPlus = shiny,
    exactEarly = perfectEarly,
    exactLate = perfectLate,
    nearEarly = nearEarly,
    nearLate = nearLate,
    `break` = miss,
)

fun DiveRecord.process(
    histories: List<DiveRecord>,
    algorithm: PotentialAlgorithm,
    level: Double,
): ProcessedDiveRecord {
    val song = Songs.entries
        .firstOrNull { it.id == id }
        ?: Song(id, name)

    val potential = algorithm.calcPlayPotential(histories, this, level)

    val grading = when (score) {
        in 0..<80000000 -> Grading.D
        in 80000000..<85000000 -> Grading.C
        in 85000000..<92000000 -> Grading.B
        in 92000000..<95000000 -> Grading.A
        in 95000000..<98000000 -> Grading.AA
        in 98000000..<99000000 -> Grading.EX
        else -> Grading.EXPlus
    }

    return ProcessedDiveRecord(
        song = song,
        difficult = difficult,
        score = score,
        clearType = clearType,
        diveValue = diveValue,
        judgement = judgement,
        potential = potential,
        grading = grading,
        level = level,
        raw = this,
    )
}

suspend fun PlatformFile.readBytesWhenAvailable(): ByteArray {
    while (true) {
        runCatching {
            return readBytes()
        }
        delay(50.milliseconds)
    }
}

fun Double.floor(decimalPlaces: Int): Double {
    val n = 10.0.pow(decimalPlaces)
    return floor(this * n) / n
}

fun renderComposable(
    onRendered: suspend (ImageBitmap) -> Unit,
    content: @Composable () -> Unit,
) {
    singleWindowApplication(
        visible = false,
        exitProcessOnExit = false,
    ) {
        val graphicsLayer = rememberGraphicsLayer()
        val rendered = remember { CompletableDeferred<Unit>() }
        Box(
            modifier = Modifier
                .wrapContentSize(unbounded = true)
                .drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }
                    drawLayer(graphicsLayer)
                    rendered.complete(Unit)
                },
        ) {
            CompositionLocalProvider(LocalDensity provides Density(1f, 1f)) {
                AppTheme(false) {
                    content()
                }
            }
        }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                rendered.await()
                val imageBitmap = graphicsLayer.toImageBitmap()
                onRendered(imageBitmap)
                exitApplication()
            }
        }
    }
}

private enum class PushType {
    Direct, Conditional, None
}

fun calcR30(histories: List<ProcessedDiveRecord>): List<ProcessedDiveRecord> {
    val personalBest = mutableMapOf<Pair<Song, Difficult>, ProcessedDiveRecord>()
    val r30 = mutableListOf<ProcessedDiveRecord>()
    histories.forEach { record ->
        val pb = personalBest[record.song to record.difficult]
        val conditionalPush = run {
            val cond1 = record.score >= min(98000000, pb?.score ?: -1)
            val pushed = r30.toMutableList().also { list ->
                list.add(record)
                if (list.size > 30) {
                    list.removeFirst()
                }
            }
            val cond2 =
                pushed.distinctBy { it.song to it.difficult }.size < r30.distinctBy { it.song to it.difficult }.size

            when {
                r30.distinctBy { it.song to it.difficult }.size >= 10
                        && pushed.distinctBy { it.song to it.difficult }.size < 10 -> PushType.None

                cond1 || cond2 -> PushType.Conditional
                else -> PushType.Direct
            }
        }
        if (record.score > (pb?.score ?: -1)) {
            personalBest[record.song to record.difficult] = record
        }

        when (conditionalPush) {
            PushType.Direct -> {
                r30.add(record)
                if (r30.size > 30) {
                    r30.removeFirst()
                }
            }

            PushType.Conditional -> {
                when (r30.size) {
                    in 0..29 -> r30.add(record)
                    else if r30.any { it.potential < record.potential } -> {
                        val minIndex = r30.mapIndexed { index, record ->
                            index to record
                        }.minBy {
                            it.second.potential
                        }.first
                        if (minIndex != -1) {
                            r30[minIndex] = record
                        }
                    }
                }
            }

            PushType.None -> Unit
        }
    }

    return r30.toList()
}

fun and(vararg conditions: Boolean): Boolean =
    conditions.all { it }

fun or(vararg conditions: Boolean): Boolean =
    conditions.any { it }

fun Modifier.unfocusOnPressEnter(focusManager: FocusManager): Modifier =
    onKeyEvent { event ->
        when (event.type == KeyEventType.KeyUp && (event.key == Key.Enter || event.key == Key.NumPadEnter)) {
            true -> {
                focusManager.clearFocus()
                true
            }

            else -> false
        }
    }

fun List<ProcessedDiveRecord>.findHighScores(): List<ProcessedDiveRecord> = this
    .groupBy { it.song to it.difficult }
    .mapValues { (_, value) -> value.maxBy { it.potential } }
    .values
    .toList()