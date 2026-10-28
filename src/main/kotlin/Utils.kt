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

fun calcR30(histories: List<ProcessedDiveRecord>): List<ProcessedDiveRecord> {
    val personalBest = mutableMapOf<Pair<Song, Difficult>, ProcessedDiveRecord>()
    val r30 = mutableListOf<ProcessedDiveRecord>()

    fun ProcessedDiveRecord.key() = song to difficult

    histories.forEach { record ->
        val key = record.key()
        val pb = personalBest[key]

        // 直接写入是否会降低不同铺面数量
        val directWouldReduceSongCount = when (r30.size == 30) {
            true -> {
                val firstKey = r30.first().key()
                firstKey != key && r30.count { it.key() == firstKey } == 1
            }

            false -> false
        }

        val conditionalPush = or(
            // 分数达到 98M / 分数达到 PB / 首次游玩
            record.score >= min(98_000_000, pb?.score ?: -1),
            // 直接写入会降低不同谱面数量
            directWouldReduceSongCount,
        )

        // 更新 PB
        if (record.score > (pb?.score ?: -1)) {
            personalBest[key] = record
        }

        when {
            // R30 未满, 直接写入
            r30.size < 30 -> r30.add(record)

            // 条件写入
            conditionalPush -> {
                val minIndex = r30.indices.minBy { r30[it].potential }

                if (record.potential > r30[minIndex].potential) {
                    // 模拟替换, 检查不同谱面数量是否下降
                    val replaced = r30.toMutableList()
                        .apply { this[minIndex] = record }
                    when (replaced.distinctBy { it.key() }.size >= r30.distinctBy { it.key() }.size) {
                        // 替换后不同铺面数量未减少
                        true -> r30[minIndex] = record
                        // 替换后不同铺面数量减少, 替换已有铺面
                        else -> {
                            val minSame = r30.indices
                                .filter { r30[it].key() == key }
                                .minBy { r30[it].potential }
                            if (r30[minSame].potential < record.potential) {
                                r30[minSame] = record
                            }
                        }
                    }
                }
            }

            // 直接写入
            else -> {
                r30.add(record)
                r30.removeFirst()
            }
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