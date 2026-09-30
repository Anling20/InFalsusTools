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

val songIdMap = mapOf(
    2 to "alamode",
    3 to "alterededge",
    5 to "bethere",
    6 to "chaoticismlegacy",
    7 to "chronomia",
    8 to "chronophobia",
    9 to "codeleviathan",
    10 to "coldsea",
    11 to "cryogenic",
    12 to "cyaegha",
    13 to "destroyer",
    14 to "enigma",
    15 to "everything",
    16 to "exceedmind",
    17 to "fallingshadow",
    18 to "falsequre",
    19 to "ffff",
    20 to "fochaos",
    21 to "greyscalecity",
    22 to "hanabi",
    23 to "hyalouyne",
    24 to "init",
    25 to "kirapico",
    26 to "kiretsu",
    27 to "latentduality",
    28 to "lfdy",
    29 to "lightsout",
    30 to "memoire",
    31 to "minerva",
    32 to "mirinae",
    33 to "moondiver",
    34 to "moonsliders",
    35 to "mydeadlysins",
    36 to "newvision",
    37 to "noctidea",
    38 to "nowiknow",
    39 to "outerjustice",
    40 to "papillonblanc",
    41 to "phylaxron",
    42 to "primevaltexture",
    43 to "reconnect",
    44 to "reflection",
    45 to "scarletespada",
    46 to "selfconfrontation",
    47 to "shadowsofunknown",
    48 to "shouldhavebeen",
    49 to "sinutopia",
    50 to "sunanokioku",
    51 to "tactom",
    52 to "thingsitreasure",
    53 to "toccatafunebre",
    54 to "tokonagi",
    55 to "trajectoryofhope",
    56 to "transparency",
    57 to "vesper",
    58 to "withtruth",
    59 to "worldender",
    60 to "yggthrasir",
    61 to "zetta",
    62 to "zfc",
    63 to "forbiddensouls",
    64 to "ghostray",
    65 to "wonderroom",
    67 to "deepintothevibe",
    68 to "cylin",
    69 to "ordirehv",
    70 to "rainshower",
    72 to "albedo",
    73 to "neonvision",
    74 to "tutorialevolong",
    75 to "tutorialevoshort",
    76 to "tutorialmin",
    82 to "kuukaku",
    84 to "monitoring",
    86 to "roki",
    87 to "lostonesweeping",
    88 to "nonbreathoblige",
    89 to "kakushicommand",
)

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
        song = songIdMap[highScoreRecord.songId]!!,
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
    song = songIdMap[songId]!!,
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

    val song = Songs.entries.first { it.name == song }
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