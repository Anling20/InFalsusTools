package cn.anling.inftool.potential_algorithm

import cn.anling.inftool.Difficult.EVO
import cn.anling.inftool.Difficult.FBD
import cn.anling.inftool.Difficult.MIN
import cn.anling.inftool.Difficult.ULT
import cn.anling.inftool.DiveRecord
import cn.anling.inftool.ProcessedDiveRecord
import cn.anling.inftool.Song
import cn.anling.inftool.song.Songs
import kotlin.math.pow

val PotentialAlgorithm.Companion.inFalsusTools
    get() = InFalsusToolsPotentialAlgorithm

object InFalsusToolsPotentialAlgorithm : PotentialAlgorithm("potential_algorithm.in_falsus_tools") {
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
        val b30 = highScores.sortedByDescending { it.potential }.take(30).map { it.potential }
        return (b30 + b30.take(10)).sum() / 40.0
    }

    override fun contents(
        highScores: List<ProcessedDiveRecord>,
        histories: List<ProcessedDiveRecord>,
    ): Map<String, List<ProcessedDiveRecord>> {
        val b30 = highScores.sortedByDescending { it.potential }.take(30)
        return mapOf(
            "Best30" to b30,
            "Best10" to b30.take(10),
        )
    }
}