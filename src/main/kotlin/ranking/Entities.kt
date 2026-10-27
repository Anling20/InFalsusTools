package cn.anling.inftool.ranking

import kotlinx.serialization.Serializable

typealias Ranking = Map<Int, Map<Difficult, List<SongPlay>>>

@Serializable
data class PlayRecordsContainer(
    val userName: String,
    val records: List<PlayRecord>,
)

@Serializable
data class PlayRecord(
    val id: Int,
    val difficult: Difficult,
    val score: Long,
    val clearType: ClearType,
    val diveValue: Int,
    val judgement: JudgementContainer,
)

@Serializable
data class SongPlay(
    val user: String,
    val score: Long,
    val clearType: ClearType,
    val diveValue: Int,
    val judgement: JudgementContainer,
)

@Serializable
enum class Difficult {
    Forbidden, Ultimate, Evolved, Minimal,
}

@Serializable
enum class ClearType {
    DiveFailed, DiveCleared, FullLink, PerfectDive,
}

@Serializable
data class JudgementContainer(
    val exactPlus: Int,
    val exactEarly: Int,
    val exactLate: Int,
    val nearEarly: Int,
    val nearLate: Int,
    val `break`: Int,
)