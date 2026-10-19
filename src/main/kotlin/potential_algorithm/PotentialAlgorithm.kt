package cn.anling.inftool.potential_algorithm

import cn.anling.inftool.DiveRecord
import cn.anling.inftool.ProcessedDiveRecord

abstract class PotentialAlgorithm(val name: String) {
    abstract fun calcPlayPotential(
        histories: List<DiveRecord>,
        playRecord: DiveRecord,
        level: Double,
    ): Double

    abstract fun calcPotential(
        histories: List<ProcessedDiveRecord>,
    ): Double

    abstract fun contents(
        histories: List<ProcessedDiveRecord>,
    ): Map<String, List<ProcessedDiveRecord>>
}