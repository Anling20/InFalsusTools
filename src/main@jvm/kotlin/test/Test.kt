package test

import cn.anling.inftool.ClearType
import cn.anling.inftool.Difficult
import cn.anling.inftool.DiveRecord
import cn.anling.inftool.potential_algorithm.PotentialAlgorithm
import cn.anling.inftool.potential_algorithm.inFalsusTools
import cn.anling.inftool.process
import cn.anling.inftool.song.Songs

fun main() {
    val b30 = Songs.entries.flatMap { song ->
        buildList {
            Difficult.entries.forEach {
                add(
                    DiveRecord(
                        id = song.id,
                        name = song.name,
                        difficult = it,
                        score = 100000000L,
                        clearType = ClearType.PerfectDive,
                        diveValue = 15,
                        judgement = null,
                    )
                )
            }
        }
    }.map { it.process(emptyList(), emptyList(), PotentialAlgorithm.inFalsusTools, true) }.sortedByDescending { it.potential }.take(30)
    println((b30 + b30.take(10)).sumOf { it.potential } / 40)
    println("b30: ${b30.map { "${it.song.name}[${it.difficult.name}]" }}")
}