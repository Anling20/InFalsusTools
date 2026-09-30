package cn.anling.inftool

enum class Difficult {
    MIN, EVO, ULT, FBD
}

enum class ClearType {
    None, DiveFailed, DiveCleared, FullLink, PerfectDive,
}

enum class Grading {
    EXPlus, EX, AA, A, B, C, D
}

data class Song(
    val id: Int,
    val name: String,
    val title: String,
    val minimal: Double,
    val evolved: Double,
    val ultimate: Double,
    val forbidden: Double,
)

data class ProcessedDiveRecord(
    val song: Song,
    val difficult: Difficult,
    val score: Long,
    val clearType: ClearType,
    val count: CountContainer?,
    val potential: Double,
    val grading: Grading,
    val level: Double,
    val raw: DiveRecord,
)

data class DiveRecord(
    val id: Int,
    val name: String,
    val difficult: Difficult,
    val score: Long,
    val clearType: ClearType,
    val count: CountContainer?,
)

data class CountContainer(
    val maxLink: Int,
    val tap: JudgementCount,
    val hold: JudgementCount,
    val field: JudgementCount,
    val flick: JudgementCount,
) {
    val exactPlus: Int =
        tap.exactPlus + hold.exactPlus + field.exactPlus + flick.exactPlus

    val exactEarly: Int =
        tap.exactEarly + hold.exactEarly + field.exactEarly + flick.exactEarly

    val exactLate: Int =
        tap.exactLate + hold.exactLate + field.exactLate + flick.exactLate

    val exact: Int =
        exactEarly + exactLate

    val nearEarly: Int =
        tap.nearEarly + hold.nearEarly + field.nearEarly + flick.nearEarly

    val nearLate: Int =
        tap.nearLate + hold.nearLate + field.nearLate + flick.nearLate

    val near: Int =
        nearEarly + nearLate

    val `break`: Int =
        tap.`break` + hold.`break` + field.`break` + flick.`break`
}

data class JudgementCount(
    val exactPlus: Int,
    val exactEarly: Int,
    val exactLate: Int,
    val nearEarly: Int,
    val nearLate: Int,
    val `break`: Int,
)

data class WindowTranslation(
    val position: Pair<Int, Int>,
    val size: Pair<Int, Int>,
)