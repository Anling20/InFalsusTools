package cn.anling.inftool.potential_algorithm

import cn.anling.inftool.DiveRecord
import cn.anling.inftool.ProcessedDiveRecord

sealed class PotentialAlgorithm(val i18nKey: String) {
    abstract fun calcPlayPotential(
        highScores: List<DiveRecord>,
        histories: List<DiveRecord>,
        playRecord: DiveRecord,
        useExperimentalLevel: Boolean,
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
            PotentialAlgorithm.inFalsusRating,
            PotentialAlgorithm.arcaeaBest50,
        )
    }
}