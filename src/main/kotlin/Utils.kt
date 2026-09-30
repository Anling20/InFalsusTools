package cn.anling.inftool

import cn.anling.inftool.ClearType.*
import cn.anling.inftool.Difficult.*
import cn.anling.inftool.parser.ParsedRecord
import cn.anling.inftool.parser.ParsedSave
import cn.anling.inftool.song.Songs
import kotlinx.coroutines.delay
import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.max
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
        clearType = ClearType.entries[highScoreRecord.resultClear],
        count = historyRecord?.run {
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
    count = CountContainer(
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

fun DiveRecord.process(experimentalLevel: Boolean): ProcessedDiveRecord {
    val song = Songs.entries.firstOrNull { it.id == id } ?: Song(
        id = id,
        name = name,
        title = name,
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
        when (experimentalLevel) {
            true -> it
            else -> it.toInt().toDouble()
        }
    }

    val additionPotential = when {
        count != null && (count.exact + count.near + count.`break`) == 0 -> 0.2
        else -> when (clearType) {
            PerfectDive -> 0.2
            FullLink -> 0.2
            DiveCleared -> 0.2
            DiveFailed -> 0.0
            None -> 0.0
        }
    }

    val potential = when (score) {
        in 0..<98000000 -> max(0.0, level + ((score - 95000000) / 3000000.0) + additionPotential)
        in 98000000..<100000000 -> level + 1 + ((score - 98000000) / 2000000.0) + additionPotential
        else -> level + 2 + additionPotential
    }

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
        count = count,
        potential = potential,
        grading = grading,
        level = level,
        raw = this,
    )
}

fun List<ProcessedDiveRecord>.potential(): Double {
    val b50 = sortedByDescending {
        it.potential
    }.take(50)
    val b10ptt = b50.take(10).map { it.potential }
    val b50ptt = b50.map { it.potential }
    return (b10ptt.sum() + b50ptt.sum()) / 60.0
}

suspend fun Path.readBytesWhenAvailable(): ByteArray {
    while (true) {
        runCatching {
            return Files.readAllBytes(this)
        }
        delay(50.milliseconds)
    }
}