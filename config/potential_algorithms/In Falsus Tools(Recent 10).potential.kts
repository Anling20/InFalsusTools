#!/usr/bin/env kotlin

import cn.anling.inftool.DiveRecord
import cn.anling.inftool.ProcessedDiveRecord
import cn.anling.inftool.calcR30
import cn.anling.inftool.potential_algorithm.PotentialAlgorithm
import kotlin.math.pow

object : PotentialAlgorithm("In Falsus Tools(Recent 10)") {
    override fun calcPlayPotential(
        highScores: List<DiveRecord>,
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
        highScores: List<ProcessedDiveRecord>,
        histories: List<ProcessedDiveRecord>,
    ): Double {
        val b30ptt = highScores
            .sortedByDescending { it.potential }
            .map { it.potential }
            .take(30)
        val r30 = calcR30(histories)
        val r10ptt = r30
            .distinctBy { it.song to it.difficult }
            .sortedByDescending { it.potential }
            .map { it.potential }
            .take(10)
        return (b30ptt + r10ptt).sum() / 40
    }

    override fun contents(
        highScores: List<ProcessedDiveRecord>,
        histories: List<ProcessedDiveRecord>,
    ): Map<String, List<ProcessedDiveRecord>> {
        val b30 = highScores
            .sortedByDescending { it.potential }
            .take(30)
        val r30 = calcR30(histories)
        val r10 = r30
            .distinctBy { it.song to it.difficult }
            .sortedByDescending { it.potential }
            .take(10)
        return mapOf(
            "Best 30" to b30,
            "Recent top 10" to r10,
        )
    }
}