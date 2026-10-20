#!/usr/bin/env kotlin

import cn.anling.inftool.*
import cn.anling.inftool.potential_algorithm.PotentialAlgorithm
import cn.anling.inftool.song.LevelMap
import cn.anling.inftool.song.Songs
import cn.anling.inftool.song.selectLevel
import kotlin.math.pow

object : PotentialAlgorithm("In Falsus Tools(理论PTT)") {
    override fun calcPlayPotential(
        histories: List<DiveRecord>,
        levelMap: LevelMap,
        playRecord: DiveRecord,
    ): Double {
        val level = levelMap.selectLevel(playRecord)
        val potential = when (playRecord.score) {
            in 0..<95000000 -> level * (playRecord.score / 95000000.0).pow(10)
            in 95000000..<98000000 -> level + ((playRecord.score - 95000000) / 3000000.0)
            in 98000000..<100000000 -> level + 1 + ((playRecord.score - 98000000) / 2000000.0)
            else -> level + 2.1
        }

        return potential
    }

    override fun calcPotential(
        histories: List<ProcessedDiveRecord>,
        levelMap: LevelMap,
    ): Double {
        val b30ptt = Songs.entries
            .flatMap {
                listOf(
                    it to Difficult.MIN,
                    it to Difficult.EVO,
                    it to Difficult.ULT,
                    it to Difficult.FBD,
                )
            }
            .map { (song, difficult) ->
                DiveRecord(
                    id = song.id,
                    name = "",
                    difficult = difficult,
                    score = 100000000,
                    clearType = ClearType.PerfectDive,
                    diveValue = 15,
                    judgement = CountContainer.empty,
                ).process(
                    histories = histories.map { it.raw },
                    levelMap = levelMap,
                    algorithm = this,
                )
            }
            .findHighScores()
            .sortedByDescending { it.potential }
            .map { it.potential }
            .take(30)
        return (b30ptt + b30ptt.take(10)).sum() / 40
    }

    override fun contents(
        histories: List<ProcessedDiveRecord>,
        levelMap: LevelMap,
    ): Map<String, List<ProcessedDiveRecord>> {
        val b30 = Songs.entries
            .flatMap {
                listOf(
                    it to Difficult.MIN,
                    it to Difficult.EVO,
                    it to Difficult.ULT,
                    it to Difficult.FBD,
                )
            }
            .map { (song, difficult) ->
                DiveRecord(
                    id = song.id,
                    name = "",
                    difficult = difficult,
                    score = 100000000,
                    clearType = ClearType.PerfectDive,
                    diveValue = 15,
                    judgement = CountContainer.empty,
                ).process(
                    histories = histories.map { it.raw },
                    levelMap = levelMap,
                    algorithm = this,
                )
            }
            .findHighScores()
            .sortedByDescending { it.potential }
            .take(30)
        return mapOf(
            "Best 30" to b30,
            "Best 10" to b30.take(10),
        )
    }
}