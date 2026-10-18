package cn.anling.inftool.potential_algorithm

import cn.anling.inftool.DiveRecord
import cn.anling.inftool.ProcessedDiveRecord

sealed class PotentialAlgorithm(val i18nKey: String) {
    abstract fun calcPlayPotential(
        highScores: List<DiveRecord>,
        histories: List<DiveRecord>,
        playRecord: DiveRecord,
        level: Double,
    ): Double

    abstract fun calcPotential(
        highScores: List<ProcessedDiveRecord>,
        histories: List<ProcessedDiveRecord>,
    ): Double

    abstract fun contents(
        highScores: List<ProcessedDiveRecord>,
        histories: List<ProcessedDiveRecord>,
    ): Map<String, List<ProcessedDiveRecord>>

    companion object {
        val entries = listOf(
            PotentialAlgorithm.inFalsusTools,
            PotentialAlgorithm.inFalsusToolsRecent10,
            PotentialAlgorithm.inFalsusRating,
            PotentialAlgorithm.arcaea,
            PotentialAlgorithm.arcaeaClassic,
            PotentialAlgorithm.arcaeaClassicMax,
            PotentialAlgorithm.arcaeaClassicMin,
            PotentialAlgorithm.pd30,
            PotentialAlgorithm.fl30,
            PotentialAlgorithm.sex30,
        )
    }
}