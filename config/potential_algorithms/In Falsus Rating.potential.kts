#!/usr/bin/env kotlin

import cn.anling.inftool.DiveRecord
import cn.anling.inftool.ProcessedDiveRecord
import cn.anling.inftool.findHighScores
import cn.anling.inftool.potential_algorithm.PotentialAlgorithm
import kotlin.math.floor

object : PotentialAlgorithm("In Falsus Rating") {
    override fun calcPlayPotential(
        histories: List<DiveRecord>,
        playRecord: DiveRecord,
        level: Double,
    ): Double {
        val baseRating = level + run {
            val baseScore = 95000000.0
            val rangeForMax = 5000000.0
            val maxBonus = 2.0
            ((playRecord.score - baseScore) / rangeForMax * maxBonus).coerceIn(-2.0, maxBonus)
        }

        val maxPace = histories
            .filter { it.id == playRecord.id && it.difficult == playRecord.difficult }
            .maxOf { it.diveValue }

        val rating = when (maxPace >= 0) {
            true -> baseRating + maxPace * 0.01
            else -> {
                val n = -maxPace
                val sumDive = -(n * (n + 1) / 2.0)
                val penaltyPct = floor(sumDive / 1.2)
                val factor = (1.0 + penaltyPct / 100.0).coerceAtLeast(0.1)
                baseRating * factor
            }
        }

        return rating
    }

    override fun calcPotential(
        histories: List<ProcessedDiveRecord>,
    ): Double {
        val b30ptt = histories
            .findHighScores()
            .sortedByDescending { it.potential }
            .map { it.potential }
            .take(30)
        val b6ptt = b30ptt.take(6)
        val b1ptt = b30ptt.take(1)
        return (b30ptt + b6ptt + b1ptt + b1ptt).sum() / 38
    }

    override fun contents(
        histories: List<ProcessedDiveRecord>,
    ): Map<String, List<ProcessedDiveRecord>> {
        val b30 = histories
            .findHighScores()
            .sortedByDescending { it.potential }
            .take(30)
        val b6 = b30.take(6)
        val b1 = b30.take(1)
        return mapOf(
            "Best 30" to b30,
            "Best 6" to b6,
            "Best 1" to b1 + b1,
        )
    }
}