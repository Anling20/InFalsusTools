package cn.anling.inftool.potential_algorithm

import cn.anling.inftool.DiveRecord
import cn.anling.inftool.ProcessedDiveRecord
import cn.anling.inftool.song.LevelMap

abstract class PotentialAlgorithm(val name: String) {
    abstract fun calcPlayPotential(
        histories: List<DiveRecord>,
        levelMap: LevelMap,
        playRecord: DiveRecord,
    ): Double

    abstract fun calcPotential(
        histories: List<ProcessedDiveRecord>,
        levelMap: LevelMap,
    ): Double

    abstract fun contents(
        histories: List<ProcessedDiveRecord>,
        levelMap: LevelMap,
    ): Map<String, List<ProcessedDiveRecord>>

    object Empty : PotentialAlgorithm("Empty") {
        override fun calcPlayPotential(
            histories: List<DiveRecord>,
            levelMap: LevelMap,
            playRecord: DiveRecord
        ): Double = 0.0

        override fun calcPotential(
            histories: List<ProcessedDiveRecord>,
            levelMap: LevelMap
        ): Double = 0.0

        override fun contents(
            histories: List<ProcessedDiveRecord>,
            levelMap: LevelMap
        ): Map<String, List<ProcessedDiveRecord>> = emptyMap()
    }
}