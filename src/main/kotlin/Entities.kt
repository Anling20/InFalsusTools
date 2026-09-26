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
    val id: String,
    val name: String,
    val chapter: String,
    val minimal: Double,
    val evolved: Double,
    val ultimate: Double,
    val forbidden: Double,
)

data class ProcessedDiveRecord(
    val song: Song,
    val difficult: Difficult,
    val score: Int,
    val clearType: ClearType,
    val maxLink: Int?,
    val exactPlus: Int?,
    val exact: Int?,
    val near: Int?,
    val `break`: Int?,
    val potential: Double,
    val grading: Grading,
    val level: Double,
    val raw: DiveRecord,
)

sealed interface DiveRecord {
    val song: String
    val difficult: Difficult
    val score: Int
    val clearType: ClearType
    val maxLink: Int?
    val exactPlus: Int?
    val exact: Int?
    val near: Int?
    val `break`: Int?

    data class Simple(
        override val song: String,
        override val difficult: Difficult,
        override val score: Int,
        override val clearType: ClearType,
        override val maxLink: Int?,
        override val exactPlus: Int?,
        override val exact: Int?,
        override val near: Int?,
        override val `break`: Int?,
    ) : DiveRecord

    data class Detailed(
        override val song: String,
        override val difficult: Difficult,
        override val score: Int,
        override val clearType: ClearType,
        override val maxLink: Int,
        val tap: Count,
        val hold: Count,
        val field: Count,
        val flick: Count,
    ) : DiveRecord {
        override val exactPlus =
            tap.exactPlus + hold.exactPlus + field.exactPlus + flick.exactPlus

        override val exact =
            tap.exactEarly + hold.exactEarly + field.exactEarly + flick.exactEarly + tap.exactLate + hold.exactLate + field.exactLate + flick.exactLate

        override val near =
            tap.nearEarly + hold.nearEarly + field.nearEarly + flick.nearEarly + tap.nearLate + hold.nearLate + field.nearLate + flick.nearLate

        override val `break` =
            tap.`break` + hold.`break` + field.`break` + flick.`break`

        data class Count(
            val exactPlus: Int,
            val exactEarly: Int,
            val exactLate: Int,
            val nearEarly: Int,
            val nearLate: Int,
            val `break`: Int,
        )
    }
}