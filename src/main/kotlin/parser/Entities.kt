package cn.anling.inftool.parser

data class ParsedSave(
    val history: List<ParsedRecord>,
    val highScores: List<ParsedRecord>,
)

data class ParsedRecord(
    val songId: Int,
    val baseName: String,

    val difficultyFlag: Int,
    val difficultyIndex: Int,

    val score: Long,
    val maxCombo: Int,

    val lamp: Int,
    val resultClear: Int,
    val paceValue: Int,
    val secondsSinceEpochUtc: Long,

    val counts: ParsedCounts,
    val exactPlus: Map<String, Int>,
    val judgementTotal: Map<String, Int>,

    val payloadHex: String,
)

data class ParsedCounts(
    val tap: ParsedJudgementTypeCount,
    val hold: ParsedJudgementTypeCount,
    val skyArea: ParsedJudgementTypeCount,
    val flick: ParsedJudgementTypeCount,
)

data class ParsedJudgementTypeCount(
    val none: Int,
    val miss: Int,
    val nearEarly: Int,
    val nearLate: Int,
    val perfectEarly: Int,
    val perfectLate: Int,
    val shiny: Int,
    val unused: Int,
) {
    fun total(): Int =
        none +
                miss +
                nearEarly +
                nearLate +
                perfectEarly +
                perfectLate +
                shiny +
                unused
}

data class RecordLocation(
    val name: String,
    val payloadOffset: Int,
)