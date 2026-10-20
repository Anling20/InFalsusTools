#!/usr/bin/env kotlin

import cn.anling.inftool.DiveRecord
import cn.anling.inftool.ProcessedDiveRecord
import cn.anling.inftool.findHighScores
import cn.anling.inftool.potential_algorithm.PotentialAlgorithm
import cn.anling.inftool.song.LevelMap
import cn.anling.inftool.song.selectLevel
import kotlin.math.max

object : PotentialAlgorithm("Arcaea Classic(Max)") {
    override fun calcPlayPotential(
        histories: List<DiveRecord>,
        levelMap: LevelMap,
        playRecord: DiveRecord,
    ): Double {
        val level = levelMap.selectLevel(playRecord)
        val potential = when (playRecord.score) {
            in 0..<98000000 -> max(0.0, level + ((playRecord.score - 95000000) / 3000000.0))
            in 98000000..<100000000 -> level + 1 + ((playRecord.score - 98000000) / 2000000.0)
            else -> level + 2
        }

        return potential
    }

    override fun calcPotential(
        histories: List<ProcessedDiveRecord>,
        levelMap: LevelMap,
    ): Double {
        val b30ptt = histories
            .findHighScores()
            .sortedByDescending { it.potential }
            .map { it.potential }
            .take(30)
        val r10ptt = b30ptt.take(10)
        return (b30ptt + r10ptt).sum() / 40
    }

    override fun contents(
        histories: List<ProcessedDiveRecord>,
        levelMap: LevelMap,
    ): Map<String, List<ProcessedDiveRecord>> {
        val b30 = histories
            .findHighScores()
            .sortedByDescending { it.potential }
            .take(30)
        val r10 = b30.take(10)
        return mapOf(
            "Best 30" to b30,
            "Recent top 10" to r10,
        )
    }
}