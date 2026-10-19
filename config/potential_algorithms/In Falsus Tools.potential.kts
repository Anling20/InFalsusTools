#!/usr/bin/env kotlin

import cn.anling.inftool.DiveRecord
import cn.anling.inftool.ProcessedDiveRecord
import cn.anling.inftool.findHighScores
import cn.anling.inftool.potential_algorithm.PotentialAlgorithm
import kotlin.math.pow

object : PotentialAlgorithm("In Falsus Tools") {
    override fun calcPlayPotential(
        histories: List<DiveRecord>,
        playRecord: DiveRecord,
        level: Double,
    ): Double {
        val potential = when (playRecord.score) {
            in 0..<95000000 -> level * (playRecord.score / 95000000.0).pow(10)
            in 95000000..<98000000 -> level + ((playRecord.score - 95000000) / 3000000.0)
            in 98000000..<100000000 -> level + 1 + ((playRecord.score - 98000000) / 2000000.0)
            else -> level + 2.1
        }

        return potential
    }

    override fun calcPotential(
        histories: List<ProcessedDiveRecord>,
    ): Double {
        val b30ptt = histories
            .findHighScores()
            .sortedByDescending { it.potential }
            .map { it.potential }
            .take(30)
        return (b30ptt + b30ptt.take(10)).sum() / 40
    }

    override fun contents(
        histories: List<ProcessedDiveRecord>,
    ): Map<String, List<ProcessedDiveRecord>> {
        val b30 = histories
            .findHighScores()
            .sortedByDescending { it.potential }
            .take(30)
        return mapOf(
            "Best 30" to b30,
            "Best 10" to b30.take(10),
        )
    }
}