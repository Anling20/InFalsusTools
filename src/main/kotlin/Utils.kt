package cn.anling.inftool

import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.key.*
import cn.anling.inftool.parser.ParsedJudgementTypeCount
import cn.anling.inftool.parser.ParsedRecord
import cn.anling.inftool.potential_algorithm.PotentialAlgorithm
import cn.anling.inftool.song.ConstantTable
import cn.anling.inftool.song.Song
import cn.anling.inftool.song.Songs
import cn.anling.inftool.song.selectLevel
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.readString
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
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
    levelMap: ConstantTable,
    algorithm: PotentialAlgorithm,
): ProcessedDiveRecord {
    val song = Songs.entries
        .firstOrNull { it.id == id }
        ?: Song(id, name)

    val potential = algorithm.calcPlayPotential(histories, levelMap, this)

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
        level = levelMap.selectLevel(this),
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

fun PlatformFile.readStringSync(): String = runBlocking { readString() }