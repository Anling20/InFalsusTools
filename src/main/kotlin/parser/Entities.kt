package cn.anling.inftool.parser

data class ParseResult(
    val songId: Int,
    val songName: String,
    val difficultFlag: Int,
    val difficultIndex: Int,
    val score: Long,
    val maxLink: Int,
    val lamp: Int,
    val resultClear: Int,
    val paceValue: Int,
    val counts: JudgementCounts,
)

data class JudgementCounts(
    val tap: JudgementCount,
    val hold: JudgementCount,
    val field: JudgementCount,
    val flick: JudgementCount,
)

data class JudgementCount(
    val none: Int,
    val `break`: Int,
    val nearEarly: Int,
    val nearLate: Int,
    val exactEarly: Int,
    val exactLate: Int,
    val exactPlus: Int,
    val unused: Int,
)