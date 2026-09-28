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
    val name: String,
    val title: String,
    val chapter: String,
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
    val maxLink: Int?,
    val exactPlus: Int?,
    val exactEarly: Int?,
    val exactLate: Int?,
    val nearEarly: Int?,
    val nearLate: Int?,
    val `break`: Int?,
    val potential: Double,
    val grading: Grading,
    val level: Double,
    val raw: DiveRecord,
)

data class DiveRecord(
    val song: String,
    val difficult: Difficult,
    val score: Long,
    val clearType: ClearType,
    val maxLink: Int?,
    val exactPlus: Int?,
    val exactEarly: Int?,
    val exactLate: Int?,
    val nearEarly: Int?,
    val nearLate: Int?,
    val `break`: Int?,
)

data class WindowTranslation(
    val position: Pair<Int, Int>,
    val size: Pair<Int, Int>,
)