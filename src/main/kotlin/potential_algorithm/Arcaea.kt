package cn.anling.inftool.potential_algorithm

import cn.anling.inftool.ClearType.*
import cn.anling.inftool.DiveRecord
import cn.anling.inftool.ProcessedDiveRecord
import kotlin.math.max

val PotentialAlgorithm.Companion.arcaea
    get() = ArcaeaPotentialAlgorithm

object ArcaeaPotentialAlgorithm : PotentialAlgorithm("potential_algorithm.arcaea") {
    override fun calcPlayPotential(
        highScores: List<DiveRecord>,
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
        highScores: List<ProcessedDiveRecord>,
        histories: List<ProcessedDiveRecord>,
    ): Double {
        val b50ptt = highScores
            .sortedByDescending { it.potential }
            .map { it.potential }
            .take(50)
        return (b50ptt + b50ptt.take(10)).average()
    }

    override fun contents(
        highScores: List<ProcessedDiveRecord>,
        histories: List<ProcessedDiveRecord>,
    ): Map<String, List<ProcessedDiveRecord>> {
        val b50 = highScores
            .sortedByDescending { it.potential }
            .take(50)
        return mapOf(
            "Best 50" to b50,
            "Best 10" to b50.take(10),
        )
    }
}