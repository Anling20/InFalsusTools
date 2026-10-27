package cn.anling.inftool.ranking

import cn.anling.inftool.ClearType.*
import cn.anling.inftool.Difficult.*
import cn.anling.inftool.ProcessedDiveRecord
import kotlinx.serialization.json.Json

fun generatePlayJson(userName: String, highScores: List<ProcessedDiveRecord>): String =
    Json.encodeToString(
        PlayRecordsContainer(
            userName = userName,
            records = highScores.map { highScore ->
                PlayRecord(
                    id = highScore.song.id,
                    difficult = when (highScore.difficult) {
                        MIN -> Difficult.Minimal
                        EVO -> Difficult.Evolved
                        ULT -> Difficult.Ultimate
                        FBD -> Difficult.Forbidden
                    },
                    score = highScore.score,
                    clearType = when (highScore.clearType) {
                        PerfectDive -> ClearType.PerfectDive
                        FullLink -> ClearType.FullLink
                        DiveCleared -> ClearType.DiveCleared
                        DiveFailed -> ClearType.DiveFailed
                        None -> error("")
                    },
                    diveValue = highScore.diveValue,
                    judgement = JudgementContainer(
                        exactPlus = highScore.judgement.exactPlus,
                        exactEarly = highScore.judgement.exactEarly,
                        exactLate = highScore.judgement.exactLate,
                        nearEarly = highScore.judgement.nearEarly,
                        nearLate = highScore.judgement.nearLate,
                        `break` = highScore.judgement.`break`,
                    )
                )
            },
        )
    )