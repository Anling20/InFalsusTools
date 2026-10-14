package cn.anling.inftool.potential_algorithm

import cn.anling.inftool.ClearType.*
import cn.anling.inftool.Difficult.*
import cn.anling.inftool.DiveRecord
import cn.anling.inftool.ProcessedDiveRecord
import cn.anling.inftool.Song
import cn.anling.inftool.song.Songs
import kotlin.math.max

val PotentialAlgorithm.Companion.arcaeaBest50
    get() = ArcaeaBest50PotentialAlgorithm

object ArcaeaBest50PotentialAlgorithm : PotentialAlgorithm("potential_algorithm.arcaea_best50") {
    override fun calcPlayPotential(
        highScores: List<DiveRecord>,
        histories: List<DiveRecord>,
        playRecord: DiveRecord,
        useExperimentalLevel: Boolean,
    ): Double {
        val song = Songs.entries.firstOrNull { it.id == playRecord.id } ?: Song(
            id = playRecord.id,
            name = playRecord.name,
            minimal = 1.0,
            evolved = 1.0,
            ultimate = 1.0,
            forbidden = 1.0,
        )

        val level = when (playRecord.difficult) {
            MIN -> song.minimal
            EVO -> song.evolved
            ULT -> song.ultimate
            FBD -> song.forbidden
        }.let {
            when (useExperimentalLevel) {
                true -> it
                else -> it.toInt().toDouble()
            }
        }

        val clearAddition = when (playRecord.clearType) {
            PerfectDive -> 0.2
            FullLink -> 0.2
            DiveCleared -> 0.2
            DiveFailed -> 0.0
            None -> 0.0
            null -> 0.0
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
        val b50 = highScores.sortedByDescending { it.potential }.take(50).map { it.potential }
        return (b50 + b50.take(10)).sum() / 60.0
    }

    override fun contents(
        highScores: List<ProcessedDiveRecord>,
        histories: List<ProcessedDiveRecord>,
    ): Map<String, List<ProcessedDiveRecord>> {
        val b50 = highScores.sortedByDescending { it.potential }.take(50)
        return mapOf(
            "Best50" to b50,
            "Best10" to b50.take(10),
        )
    }
}