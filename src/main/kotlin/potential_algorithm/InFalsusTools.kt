package cn.anling.inftool.potential_algorithm

import cn.anling.inftool.DiveRecord
import cn.anling.inftool.ProcessedDiveRecord
import cn.anling.inftool.calcR30
import cn.anling.inftool.findHighScores
import cn.anling.inftool.song.ConstantTable
import cn.anling.inftool.song.selectLevel
import kotlin.math.pow

object InFalsusToolsPotentialAlgorithm : PotentialAlgorithm("in_falsus_tools", "In Falsus Tools") {
    override fun calcPlayPotential(
        histories: List<DiveRecord>,
        constantTable: ConstantTable,
        playRecord: DiveRecord,
    ): Double {
        val level = constantTable.selectLevel(playRecord)
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
        constantTable: ConstantTable,
    ): Double {
        val b30ptt = histories
            .findHighScores()
            .sortedByDescending { it.potential }
            .map { it.potential }
            .take(30)
        return (b30ptt + b30ptt.take(10)).sum() / 40
    }

    override fun contents(
        histories: List<ProcessedDiveRecord>,
        constantTable: ConstantTable,
    ): Map<String, List<ProcessedDiveRecord>> {
        val b30 = histories
            .findHighScores()
            .sortedByDescending { it.potential }
            .take(30)
        return mapOf(
            "Best 30" to b30,
            "Best 10" to b30.take(10),
        )
    }
}

object InFalsusToolsR10PotentialAlgorithm : PotentialAlgorithm("in_falsus_tools_r10", "In Falsus Tools(Recent 10)") {
    override fun calcPlayPotential(
        histories: List<DiveRecord>,
        constantTable: ConstantTable,
        playRecord: DiveRecord,
    ): Double = InFalsusToolsPotentialAlgorithm.calcPlayPotential(histories, constantTable, playRecord)

    override fun calcPotential(
        histories: List<ProcessedDiveRecord>,
        constantTable: ConstantTable,
    ): Double {
        val b30ptt = histories
            .findHighScores()
            .sortedByDescending { it.potential }
            .map { it.potential }
            .take(30)
        val r30 = calcR30(histories)
        val r10ptt = r30
            .sortedByDescending { it.potential }
            .distinctBy { it.song to it.difficult }
            .map { it.potential }
            .take(10)
        return (b30ptt + r10ptt).sum() / 40
    }

    override fun contents(
        histories: List<ProcessedDiveRecord>,
        constantTable: ConstantTable,
    ): Map<String, List<ProcessedDiveRecord>> {
        val b30 = histories
            .findHighScores()
            .sortedByDescending { it.potential }
            .take(30)
        val r30 = calcR30(histories)
        val r10 = r30
            .sortedByDescending { it.potential }
            .distinctBy { it.song to it.difficult }
            .take(10)
        return mapOf(
            "Best 30" to b30,
            "Recent top 10" to r10,
        )
    }
}