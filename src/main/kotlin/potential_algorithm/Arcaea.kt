package cn.anling.inftool.potential_algorithm

import cn.anling.inftool.ClearType.*
import cn.anling.inftool.DiveRecord
import cn.anling.inftool.ProcessedDiveRecord
import cn.anling.inftool.calcR30
import cn.anling.inftool.findHighScores
import cn.anling.inftool.song.ConstantTable
import cn.anling.inftool.song.selectLevel
import kotlin.math.max

object ArcaeaPotentialAlgorithm : PotentialAlgorithm("arcaea", "Arcaea") {
    override fun calcPlayPotential(
        histories: List<DiveRecord>,
        constantTable: ConstantTable,
        playRecord: DiveRecord,
    ): Double {
        val clearAddition = when (playRecord.clearType) {
            PerfectDive -> 0.2
            FullLink -> 0.2
            DiveCleared -> 0.2
            DiveFailed -> 0.0
            None -> 0.0
        }

        val level = constantTable.selectLevel(playRecord)
        val potential = when (playRecord.score) {
            in 0..<98000000 -> max(0.0, level + ((playRecord.score - 95000000) / 3000000.0) + clearAddition)
            in 98000000..<100000000 -> level + 1 + ((playRecord.score - 98000000) / 2000000.0) + clearAddition
            else -> level + 2 + clearAddition
        }

        return potential
    }

    override fun calcPotential(
        histories: List<ProcessedDiveRecord>,
        constantTable: ConstantTable,
    ): Double {
        val b50ptt = histories
            .findHighScores()
            .sortedByDescending { it.potential }
            .map { it.potential }
            .take(50)
        return (b50ptt + b50ptt.take(10)).sum() / 60
    }

    override fun contents(
        histories: List<ProcessedDiveRecord>,
        constantTable: ConstantTable,
    ): Map<String, List<ProcessedDiveRecord>> {
        val b50 = histories
            .findHighScores()
            .sortedByDescending { it.potential }
            .take(50)
        return mapOf(
            "Best 50" to b50,
            "Best 10" to b50.take(10),
        )
    }
}

object ArcaeaClassicPotentialAlgorithm : PotentialAlgorithm("arcaea_classic", "Arcaea Classic") {
    override fun calcPlayPotential(
        histories: List<DiveRecord>,
        constantTable: ConstantTable,
        playRecord: DiveRecord,
    ): Double {
        val level = constantTable.selectLevel(playRecord)
        val potential = when (playRecord.score) {
            in 0..<98000000 -> max(0.0, level + ((playRecord.score - 95000000) / 3000000.0))
            in 98000000..<100000000 -> level + 1 + ((playRecord.score - 98000000) / 2000000.0)
            else -> level + 2
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