package cn.anling.inftool.potential_algorithm

import cn.anling.inftool.ClearType
import cn.anling.inftool.DiveRecord
import cn.anling.inftool.ProcessedDiveRecord
import cn.anling.inftool.or

val PotentialAlgorithm.Companion.pd30
    get() = PD30PotentialAlgorithm

val PotentialAlgorithm.Companion.fl30
    get() = FL30PotentialAlgorithm

val PotentialAlgorithm.Companion.sex30
    get() = Sex30PotentialAlgorithm

object PD30PotentialAlgorithm : PotentialAlgorithm("potential_algorithm.pd30") {
    override fun calcPlayPotential(
        highScores: List<DiveRecord>,
        histories: List<DiveRecord>,
        playRecord: DiveRecord,
        level: Double,
    ): Double = InFalsusToolsPotentialAlgorithm.calcPlayPotential(
        highScores = highScores,
        histories = histories,
        playRecord = playRecord,
        level = level,
    )

    override fun calcPotential(
        highScores: List<ProcessedDiveRecord>,
        histories: List<ProcessedDiveRecord>,
    ): Double {
        val p30ptt = highScores
            .filter { it.clearType == ClearType.PerfectDive }
            .sortedByDescending { it.potential }
            .map { it.potential }
            .take(30)
        return (p30ptt + p30ptt.take(10)).average()
    }

    override fun contents(
        highScores: List<ProcessedDiveRecord>,
        histories: List<ProcessedDiveRecord>,
    ): Map<String, List<ProcessedDiveRecord>> {
        val p30 = highScores
            .filter { it.clearType == ClearType.PerfectDive }
            .sortedByDescending { it.potential }
            .take(30)
        return mapOf(
            "Perfect Dive 30" to p30,
        )
    }
}

object FL30PotentialAlgorithm : PotentialAlgorithm("potential_algorithm.fl30") {
    override fun calcPlayPotential(
        highScores: List<DiveRecord>,
        histories: List<DiveRecord>,
        playRecord: DiveRecord,
        level: Double,
    ): Double = InFalsusToolsPotentialAlgorithm.calcPlayPotential(
        highScores = highScores,
        histories = histories,
        playRecord = playRecord,
        level = level,
    )

    override fun calcPotential(
        highScores: List<ProcessedDiveRecord>,
        histories: List<ProcessedDiveRecord>,
    ): Double {
        val p30ptt = highScores
            .filter { it.clearType == ClearType.FullLink }
            .sortedByDescending { it.potential }
            .map { it.potential }
            .take(30)
        return (p30ptt + p30ptt.take(10)).average()
    }

    override fun contents(
        highScores: List<ProcessedDiveRecord>,
        histories: List<ProcessedDiveRecord>,
    ): Map<String, List<ProcessedDiveRecord>> {
        val p30 = highScores
            .filter { it.clearType == ClearType.FullLink }
            .sortedByDescending { it.potential }
            .take(30)
        return mapOf(
            "Full Link 30" to p30,
        )
    }
}

object Sex30PotentialAlgorithm : PotentialAlgorithm("potential_algorithm.sex30") {
    override fun calcPlayPotential(
        highScores: List<DiveRecord>,
        histories: List<DiveRecord>,
        playRecord: DiveRecord,
        level: Double,
    ): Double = InFalsusToolsPotentialAlgorithm.calcPlayPotential(
        highScores = highScores,
        histories = histories,
        playRecord = playRecord,
        level = level,
    )

    override fun calcPotential(
        highScores: List<ProcessedDiveRecord>,
        histories: List<ProcessedDiveRecord>,
    ): Double {
        val p30ptt = highScores
            .filter {
                or(
                    it.judgement.near == 1 && it.judgement.`break` == 0,
                    it.judgement.`break` == 1 && it.judgement.near == 0,
                )
            }.sortedByDescending { it.potential }
            .map { it.potential }
            .take(30)
        return (p30ptt + p30ptt.take(10)).average()
    }

    override fun contents(
        highScores: List<ProcessedDiveRecord>,
        histories: List<ProcessedDiveRecord>,
    ): Map<String, List<ProcessedDiveRecord>> {
        val p30 = highScores
            .filter {
                or(
                    it.judgement.near == 1 && it.judgement.`break` == 0,
                    it.judgement.`break` == 1 && it.judgement.near == 0,
                )
            }.sortedByDescending { it.potential }
            .sortedByDescending { it.potential }
            .take(30)
        return mapOf(
            "Sex 30" to p30,
        )
    }
}