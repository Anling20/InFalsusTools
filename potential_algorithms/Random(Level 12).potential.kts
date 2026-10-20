#!/usr/bin/env kotlin

import cn.anling.inftool.*
import cn.anling.inftool.potential_algorithm.PotentialAlgorithm
import cn.anling.inftool.song.LevelMap
import cn.anling.inftool.song.Songs
import cn.anling.inftool.song.selectLevel

object : PotentialAlgorithm("Random 30(Level 12)") {
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
        val list = levelMap
            .flatMap { (id, songLevel) ->
                buildList {
                    if (songLevel.minimal in 12.0..12.9) {
                        add(id to Difficult.MIN)
                    }
                    if (songLevel.evolved in 12.0..12.9) {
                        add(id to Difficult.EVO)
                    }
                    if (songLevel.ultimate in 12.0..12.9) {
                        add(id to Difficult.ULT)
                    }
                    if (songLevel.forbidden in 12.0..12.9) {
                        add(id to Difficult.FBD)
                    }
                }
            }
            .map { (id, difficult) ->
                val song = Songs.entries.find { it.id == id }!!
                DiveRecord(
                    id = id,
                    name = "",
                    difficult = difficult,
                    score = 0,
                    clearType = ClearType.None,
                    diveValue = 0,
                    judgement = CountContainer.empty,
                ).process(
                    histories = histories.map { it.raw },
                    levelMap = levelMap,
                    algorithm = this,
                )
            }
        val random30 = List(30) { list.random() }
        return mapOf(
            "Random 30" to random30,
        )
    }
}