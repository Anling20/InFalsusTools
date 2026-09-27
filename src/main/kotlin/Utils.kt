package cn.anling.inftool

import cn.anling.inftool.ClearType.*
import cn.anling.inftool.Difficult.*
import cn.anling.inftool.parser.ParsedSave
import cn.anling.inftool.song.Songs
import kotlinx.coroutines.delay
import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

fun ParsedSave.toDiveRecordList(): List<DiveRecord> {
    return highScores.map { highScoreRecord ->
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
            song = highScoreRecord.baseName,
            difficult = Difficult.entries[highScoreRecord.difficultyIndex],
            score = highScoreRecord.score,
            clearType = ClearType.entries[highScoreRecord.resultClear],
            maxLink = historyRecord?.maxCombo,
            exactPlus = historyRecord?.counts?.run {
                tap.shiny + hold.shiny + skyArea.shiny + flick.shiny
            },
            exactEarly = historyRecord?.counts?.run {
                tap.perfectEarly + hold.perfectEarly + skyArea.perfectEarly + flick.perfectEarly
            },
            exactLate = historyRecord?.counts?.run {
                tap.perfectLate + hold.perfectLate + skyArea.perfectLate + flick.perfectLate
            },
            nearEarly = historyRecord?.counts?.run {
                tap.nearEarly + hold.nearEarly + skyArea.nearEarly + flick.nearEarly
            },
            nearLate = historyRecord?.counts?.run {
                tap.nearLate + hold.nearLate + skyArea.nearLate + flick.nearLate
            },
            `break` = historyRecord?.counts?.run {
                tap.miss + hold.miss + skyArea.miss + flick.miss
            },
        )
    }
}

fun DiveRecord.process(experimentalLevel: Boolean): ProcessedDiveRecord {
    val additionPotential = when (clearType) {
        PerfectDive -> 0.2
        FullLink -> 0.2
        DiveCleared -> 0.2
        DiveFailed -> 0.0
        None -> 0.0
    }

    val song = Songs.entries.first { it.id == song }
    val rawLevel = when (difficult) {
        MIN -> song.minimal
        EVO -> song.evolved
        ULT -> song.ultimate
        FBD -> song.forbidden
    }

    val level = when (experimentalLevel) {
        true -> rawLevel
        else -> rawLevel.toInt().toDouble()
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
        maxLink = maxLink,
        exactPlus = exactPlus,
        exactEarly = exactEarly,
        exactLate = exactLate,
        nearEarly = nearEarly,
        nearLate = nearLate,
        `break` = `break`,
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