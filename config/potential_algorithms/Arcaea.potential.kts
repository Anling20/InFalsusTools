#!/usr/bin/env kotlin

import cn.anling.inftool.ClearType.*
import cn.anling.inftool.DiveRecord
import cn.anling.inftool.ProcessedDiveRecord
import cn.anling.inftool.findHighScores
import cn.anling.inftool.potential_algorithm.PotentialAlgorithm
import kotlin.math.max

object : PotentialAlgorithm("Arcaea") {
    override fun calcPlayPotential(
        histories: List<DiveRecord>,
        playRecord: DiveRecord,
        level: Double,
    ): Double {
        val clearAddition = when (playRecord.clearType) {
            PerfectDive -> 0.2
            FullLink -> 0.2
            DiveCleared -> 0.2
            DiveFailed -> 0.0
            None -> 0.0
        }

        val potential = when (playRecord.score) {
            in 0..<98000000 -> max(0.0, level + ((playRecord.score - 95000000) / 3000000.0) + clearAddition)
            in 98000000..<100000000 -> level + 1 + ((playRecord.score - 98000000) / 2000000.0) + clearAddition
            else -> level + 2 + clearAddition
        }

        return potential
    }

    override fun calcPotential(
        histories: List<ProcessedDiveRecord>,
    ): Double {
        val b50ptt = histories
            .findHighScores()
            .sortedByDescending { it.potential }
            .map { it.potential }
            .take(50)
        return (b50ptt + b50ptt.take(10)).sum() / 60
    }

    override fun contents(
        histories: List<ProcessedDiveRecord>,
    ): Map<String, List<ProcessedDiveRecord>> {
        val b50 = histories
            .findHighScores()
            .sortedByDescending { it.potential }
            .take(50)
        return mapOf(
            "Best 50" to b50,
            "Best 10" to b50.take(10),
        )
    }
}