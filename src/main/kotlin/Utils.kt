package cn.anling.inftool

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.singleWindowApplication
import cn.anling.inftool.Difficult.*
import cn.anling.inftool.parser.ParsedRecord
import cn.anling.inftool.parser.ParsedSave
import cn.anling.inftool.potential_algorithm.PotentialAlgorithm
import cn.anling.inftool.song.Songs
import cn.anling.inftool.theme.AppTheme
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.floor
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds

fun ParsedSave.toDiveRecordList(): List<DiveRecord> = highScores.map { highScoreRecord ->
    val result = history.findLast { historyRecord ->
        historyRecord.songId == highScoreRecord.songId &&
                historyRecord.difficultyIndex == highScoreRecord.difficultyIndex &&
                historyRecord.score == highScoreRecord.score
    }
    if (result == null) {
        println("Play history not found: ${highScoreRecord.baseName}(songId:${highScoreRecord.songId}, difficultyIndex:${highScoreRecord.difficultyIndex}, score:${highScoreRecord.score})")
    }
    highScoreRecord to result
}.map { (highScoreRecord, historyRecord) ->
    DiveRecord(
        id = highScoreRecord.songId,
        name = highScoreRecord.baseName,
        difficult = Difficult.entries[highScoreRecord.difficultyIndex],
        score = highScoreRecord.score,
        clearType = historyRecord?.run { ClearType.entries[resultClear] },
        diveValue = historyRecord?.paceValue,
        judgement = historyRecord?.run {
            CountContainer(
                maxLink = maxCombo,
                tap = JudgementCount(
                    exactPlus = counts.tap.shiny,
                    exactEarly = counts.tap.perfectEarly,
                    exactLate = counts.tap.perfectLate,
                    nearEarly = counts.tap.nearEarly,
                    nearLate = counts.tap.nearLate,
                    `break` = counts.tap.miss,
                ),
                hold = JudgementCount(
                    exactPlus = counts.hold.shiny,
                    exactEarly = counts.hold.perfectEarly,
                    exactLate = counts.hold.perfectLate,
                    nearEarly = counts.hold.nearEarly,
                    nearLate = counts.hold.nearLate,
                    `break` = counts.hold.miss,
                ),
                field = JudgementCount(
                    exactPlus = counts.skyArea.shiny,
                    exactEarly = counts.skyArea.perfectEarly,
                    exactLate = counts.skyArea.perfectLate,
                    nearEarly = counts.skyArea.nearEarly,
                    nearLate = counts.skyArea.nearLate,
                    `break` = counts.skyArea.miss,
                ),
                flick = JudgementCount(
                    exactPlus = counts.flick.shiny,
                    exactEarly = counts.flick.perfectEarly,
                    exactLate = counts.flick.perfectLate,
                    nearEarly = counts.flick.nearEarly,
                    nearLate = counts.flick.nearLate,
                    `break` = counts.flick.miss,
                ),
            )
        },
    )
}

fun ParsedRecord.toDiveRecord(): DiveRecord = DiveRecord(
    id = songId,
    name = baseName,
    difficult = Difficult.entries[difficultyIndex],
    score = score,
    clearType = ClearType.entries[resultClear],
    diveValue = paceValue,
    judgement = CountContainer(
        maxLink = maxCombo,
        tap = JudgementCount(
            exactPlus = counts.tap.shiny,
            exactEarly = counts.tap.perfectEarly,
            exactLate = counts.tap.perfectLate,
            nearEarly = counts.tap.nearEarly,
            nearLate = counts.tap.nearLate,
            `break` = counts.tap.miss,
        ),
        hold = JudgementCount(
            exactPlus = counts.hold.shiny,
            exactEarly = counts.hold.perfectEarly,
            exactLate = counts.hold.perfectLate,
            nearEarly = counts.hold.nearEarly,
            nearLate = counts.hold.nearLate,
            `break` = counts.hold.miss,
        ),
        field = JudgementCount(
            exactPlus = counts.skyArea.shiny,
            exactEarly = counts.skyArea.perfectEarly,
            exactLate = counts.skyArea.perfectLate,
            nearEarly = counts.skyArea.nearEarly,
            nearLate = counts.skyArea.nearLate,
            `break` = counts.skyArea.miss,
        ),
        flick = JudgementCount(
            exactPlus = counts.flick.shiny,
            exactEarly = counts.flick.perfectEarly,
            exactLate = counts.flick.perfectLate,
            nearEarly = counts.flick.nearEarly,
            nearLate = counts.flick.nearLate,
            `break` = counts.flick.miss,
        ),
    ),
)

fun DiveRecord.process(
    highScores: List<DiveRecord>,
    histories: List<DiveRecord>,
    algorithm: PotentialAlgorithm,
    useExperimentalLevel: Boolean,
): ProcessedDiveRecord {
    val song = Songs.entries.firstOrNull { it.id == id } ?: Song(
        id = id,
        name = name,
        minimal = 1.0,
        evolved = 1.0,
        ultimate = 1.0,
        forbidden = 1.0,
    )

    val level = when (difficult) {
        MIN -> song.minimal
        EVO -> song.evolved
        ULT -> song.ultimate
        FBD -> song.forbidden
    }.let {
        when (useExperimentalLevel) {
            true -> it
            else -> it.toInt().toDouble()
        }
    }

    val potential = algorithm.calcPlayPotential(highScores, histories, this, useExperimentalLevel)

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
    onRendered: (GraphicsLayer) -> Unit,
    content: @Composable () -> Unit,
) {
    singleWindowApplication(
        visible = false,
        exitProcessOnExit = false,
    ) {
        val graphicsLayer = rememberGraphicsLayer()
        var rendered by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .wrapContentSize(unbounded = true)
                .drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }
                    drawLayer(graphicsLayer)

                    rendered = true
                },
        ) {
            CompositionLocalProvider(LocalDensity provides Density(1f, 1f)) {
                AppTheme(false) {
                    content()
                }
            }
        }

        LaunchedEffect(Unit) {
            while (!rendered) {
                delay(50.milliseconds)
            }
            withContext(Dispatchers.IO) {
                onRendered(graphicsLayer)
            }
            exitApplication()
        }
    }
}