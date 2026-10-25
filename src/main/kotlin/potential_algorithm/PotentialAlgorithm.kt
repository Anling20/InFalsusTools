package cn.anling.inftool.potential_algorithm

import cn.anling.inftool.DiveRecord
import cn.anling.inftool.ProcessedDiveRecord
import cn.anling.inftool.song.ConstantTable

sealed class PotentialAlgorithm(val id: String, val name: String) {
    abstract fun calcPlayPotential(
        histories: List<DiveRecord>,
        constantTable: ConstantTable,
        playRecord: DiveRecord,
    ): Double

    abstract fun calcPotential(
        histories: List<ProcessedDiveRecord>,
        constantTable: ConstantTable,
    ): Double

    abstract fun contents(
        histories: List<ProcessedDiveRecord>,
        constantTable: ConstantTable,
    ): Map<String, List<ProcessedDiveRecord>>

    companion object {
        val builtin by lazy {
            listOf(
                InFalsusToolsPotentialAlgorithm,
                InFalsusToolsR10PotentialAlgorithm,
                ArcaeaPotentialAlgorithm,
                ArcaeaClassicPotentialAlgorithm,
            )
        }
    }
}