package cn.anling.inftool

import cn.anling.inftool.ClearType.*
import cn.anling.inftool.Difficult.*
import cn.anling.inftool.parser.ParseResult
import cn.anling.inftool.song.Songs
import kotlinx.coroutines.delay
import java.io.File
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

fun ParseResult.toDiveRecord(): DiveRecord {
    return DiveRecord.Detailed(
        song = songName,
        difficult = Difficult.entries[difficultIndex],
        score = score.toInt(),
        clearType = ClearType.entries[resultClear],
        maxLink = maxLink,
        tap = DiveRecord.Detailed.Count(
            exactPlus = counts.tap.exactPlus,
            exactEarly = counts.tap.exactEarly,
            exactLate = counts.tap.exactLate,
            nearEarly = counts.tap.nearEarly,
            nearLate = counts.tap.nearLate,
            `break` = counts.tap.`break`,
        ),
        hold = DiveRecord.Detailed.Count(
            exactPlus = counts.hold.exactPlus,
            exactEarly = counts.hold.exactEarly,
            exactLate = counts.hold.exactLate,
            nearEarly = counts.hold.nearEarly,
            nearLate = counts.hold.nearLate,
            `break` = counts.hold.`break`,
        ),
        field = DiveRecord.Detailed.Count(
            exactPlus = counts.field.exactPlus,
            exactEarly = counts.field.exactEarly,
            exactLate = counts.field.exactLate,
            nearEarly = counts.field.nearEarly,
            nearLate = counts.field.nearLate,
            `break` = counts.field.`break`,
        ),
        flick = DiveRecord.Detailed.Count(
            exactPlus = counts.flick.exactPlus,
            exactEarly = counts.flick.exactEarly,
            exactLate = counts.flick.exactLate,
            nearEarly = counts.flick.nearEarly,
            nearLate = counts.flick.nearLate,
            `break` = counts.flick.`break`,
        ),
    )
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
        exact = exact,
        near = near,
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

suspend fun File.readBytesWhenAvailable(): ByteArray {
    while (true) {
        runCatching {
            return readBytes()
        }
        delay(50.milliseconds)
    }
}