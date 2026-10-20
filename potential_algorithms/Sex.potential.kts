#!/usr/bin/env kotlin

import cn.anling.inftool.DiveRecord
import cn.anling.inftool.ProcessedDiveRecord
import cn.anling.inftool.findHighScores
import cn.anling.inftool.or
import cn.anling.inftool.potential_algorithm.PotentialAlgorithm
import cn.anling.inftool.song.LevelMap
import cn.anling.inftool.song.selectLevel

object : PotentialAlgorithm("Sex") {
    override fun calcPlayPotential(
        histories: List<DiveRecord>,
        levelMap: LevelMap,
        playRecord: DiveRecord,
    ): Double = levelMap.selectLevel(playRecord)

    override fun calcPotential(
        histories: List<ProcessedDiveRecord>,
        levelMap: LevelMap,
    ): Double = 0.0

    override fun contents(
        histories: List<ProcessedDiveRecord>,
        levelMap: LevelMap,
    ): Map<String, List<ProcessedDiveRecord>> {
        val list = histories
            .findHighScores()
            .filter {
                or(
                    it.judgement.near == 1 && it.judgement.`break` == 0,
                    it.judgement.`break` == 1 && it.judgement.near == 0,
                )
            }
            .sortedByDescending { it.potential }
        return mapOf(
            "Sex" to list,
        )
    }
}