package cn.anling.inftool.potential_algorithm

import cn.anling.inftool.Difficult.*
import cn.anling.inftool.DiveRecord
import cn.anling.inftool.ProcessedDiveRecord
import cn.anling.inftool.Song
import cn.anling.inftool.calcR30
import cn.anling.inftool.song.Songs
import kotlin.math.max

val PotentialAlgorithm.Companion.arcaeaClassic
    get() = ArcaeaClassicPotentialAlgorithm

val PotentialAlgorithm.Companion.arcaeaClassicMax
    get() = ArcaeaClassicMaxPotentialAlgorithm

val PotentialAlgorithm.Companion.arcaeaClassicMin
    get() = ArcaeaClassicMinPotentialAlgorithm

object ArcaeaClassicPotentialAlgorithm : PotentialAlgorithm("potential_algorithm.arcaea_classic") {
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
            in 0..<98000000 -> max(0.0, level + ((playRecord.score - 95000000) / 3000000.0))
            in 98000000..<100000000 -> level + 1 + ((playRecord.score - 98000000) / 2000000.0)
            else -> level + 2
        }

        return potential
    }

    override fun calcPotential(
        highScores: List<ProcessedDiveRecord>,
        histories: List<ProcessedDiveRecord>,
    ): Double {
        val b30ptt = highScores.sortedByDescending { it.potential }.map { it.potential }.take(30)
        val r30 = calcR30(histories)
        val r10ptt = r30.distinctBy {
            it.song to it.difficult
        }.sortedByDescending {
            it.potential
        }.map {
            it.potential
        }.take(10)
        return (b30ptt + r10ptt).sum() / 40.0
    }

    override fun contents(
        highScores: List<ProcessedDiveRecord>,
        histories: List<ProcessedDiveRecord>,
    ): Map<String, List<ProcessedDiveRecord>> {
        val b30 = highScores.sortedByDescending { it.potential }.take(30)
        val r30 = calcR30(histories)
        val r10 = r30.distinctBy {
            it.song to it.difficult
        }.sortedByDescending {
            it.potential
        }.take(10)
        return mapOf(
            "Best 30" to b30,
            "Recent top 10" to r10,
        )
    }
}

object ArcaeaClassicMaxPotentialAlgorithm : PotentialAlgorithm("potential_algorithm.arcaea_classic_max") {
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
            in 0..<98000000 -> max(0.0, level + ((playRecord.score - 95000000) / 3000000.0))
            in 98000000..<100000000 -> level + 1 + ((playRecord.score - 98000000) / 2000000.0)
            else -> level + 2
        }

        return potential
    }

    override fun calcPotential(
        highScores: List<ProcessedDiveRecord>,
        histories: List<ProcessedDiveRecord>,
    ): Double {
        val b30ptt = highScores.sortedByDescending { it.potential }.map { it.potential }.take(30)
        val r10ptt = b30ptt.take(10)
        return (b30ptt + r10ptt).sum() / 40.0
    }

    override fun contents(
        highScores: List<ProcessedDiveRecord>,
        histories: List<ProcessedDiveRecord>,
    ): Map<String, List<ProcessedDiveRecord>> {
        val b30 = highScores.sortedByDescending { it.potential }.take(30)
        val r10 = b30.take(10)
        return mapOf(
            "Best 30" to b30,
            "Recent top 10" to r10,
        )
    }
}

object ArcaeaClassicMinPotentialAlgorithm : PotentialAlgorithm("potential_algorithm.arcaea_classic_min") {
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
            in 0..<98000000 -> max(0.0, level + ((playRecord.score - 95000000) / 3000000.0))
            in 98000000..<100000000 -> level + 1 + ((playRecord.score - 98000000) / 2000000.0)
            else -> level + 2
        }

        return potential
    }

    override fun calcPotential(
        highScores: List<ProcessedDiveRecord>,
        histories: List<ProcessedDiveRecord>,
    ): Double {
        val b30ptt = highScores.sortedByDescending { it.potential }.map { it.potential }.take(30)
        val r10ptt = emptyList<Double>()
        return (b30ptt + r10ptt).sum() / 40.0
    }

    override fun contents(
        highScores: List<ProcessedDiveRecord>,
        histories: List<ProcessedDiveRecord>,
    ): Map<String, List<ProcessedDiveRecord>> {
        val b30 = highScores.sortedByDescending { it.potential }.take(30)
        val r10 = emptyList<ProcessedDiveRecord>()
        return mapOf(
            "Best 30" to b30,
            "Recent top 10" to r10,
        )
    }
}