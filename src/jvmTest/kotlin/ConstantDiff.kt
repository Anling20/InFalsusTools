import cn.anling.inftool.Difficult.*
import cn.anling.inftool.song.BuiltinConstantTable
import cn.anling.inftool.song.SongConstant
import cn.anling.inftool.song.Songs

fun main() {
    val old = mapOf(
        2 to SongConstant(
            minimal = 1.0,
            evolved = 5.0,
            ultimate = 9.3,
            forbidden = 11.4,
        ),
        3 to SongConstant(
            minimal = 2.0,
            evolved = 5.0,
            ultimate = 9.4,
            forbidden = 11.5,
        ),
        5 to SongConstant(
            minimal = 3.0,
            evolved = 6.0,
            ultimate = 10.2,
            forbidden = 12.1,
        ),
        6 to SongConstant(
            minimal = 4.0,
            evolved = 7.0,
            ultimate = 10.7,
            forbidden = 13.8,
        ),
        7 to SongConstant(
            minimal = 2.0,
            evolved = 5.0,
            ultimate = 10.1,
            forbidden = 11.1,
        ),
        8 to SongConstant(
            minimal = 6.0,
            evolved = 9.8,
            ultimate = 12.1,
            forbidden = 14.6,
        ),
        9 to SongConstant(
            minimal = 5.0,
            evolved = 8.0,
            ultimate = 11.8,
            forbidden = 14.2,
        ),
        10 to SongConstant(
            minimal = 4.0,
            evolved = 7.0,
            ultimate = 10.9,
            forbidden = 13.3,
        ),
        11 to SongConstant(
            minimal = 2.0,
            evolved = 5.0,
            ultimate = 9.1,
            forbidden = 12.4,
        ),
        12 to SongConstant(
            minimal = 3.0,
            evolved = 6.0,
            ultimate = 10.5,
            forbidden = 12.3,
        ),
        13 to SongConstant(
            minimal = 3.0,
            evolved = 6.0,
            ultimate = 9.3,
            forbidden = 12.2,
        ),
        14 to SongConstant(
            minimal = 4.0,
            evolved = 7.0,
            ultimate = 11.8,
            forbidden = 13.7,
        ),
        15 to SongConstant(
            minimal = 1.0,
            evolved = 4.0,
            ultimate = 8.0,
            forbidden = 10.9,
        ),
        16 to SongConstant(
            minimal = 3.0,
            evolved = 6.0,
            ultimate = 10.3,
            forbidden = 12.7,
        ),
        17 to SongConstant(
            minimal = 1.0,
            evolved = 4.0,
            ultimate = 8.0,
            forbidden = 11.1,
        ),
        18 to SongConstant(
            minimal = 2.0,
            evolved = 6.0,
            ultimate = 10.8,
            forbidden = 12.7,
        ),
        19 to SongConstant(
            minimal = 2.0,
            evolved = 5.0,
            ultimate = 9.9,
            forbidden = 11.7,
        ),
        20 to SongConstant(
            minimal = 2.0,
            evolved = 5.0,
            ultimate = 9.9,
            forbidden = 11.7,
        ),
        21 to SongConstant(
            minimal = 2.0,
            evolved = 5.0,
            ultimate = 10.4,
            forbidden = 12.4,
        ),
        22 to SongConstant(
            minimal = 2.0,
            evolved = 6.0,
            ultimate = 9.7,
            forbidden = 11.0,
        ),
        23 to SongConstant(
            minimal = 3.0,
            evolved = 6.0,
            ultimate = 10.3,
            forbidden = 12.3,
        ),
        24 to SongConstant(
            minimal = 2.0,
            evolved = 5.0,
            ultimate = 9.1,
            forbidden = 11.3,
        ),
        25 to SongConstant(
            minimal = 2.0,
            evolved = 5.0,
            ultimate = 9.5,
            forbidden = 11.2,
        ),
        26 to SongConstant(
            minimal = 4.0,
            evolved = 7.0,
            ultimate = 10.7,
            forbidden = 13.5,
        ),
        27 to SongConstant(
            minimal = 4.0,
            evolved = 7.0,
            ultimate = 11.5,
            forbidden = 13.5,
        ),
        28 to SongConstant(
            minimal = 2.0,
            evolved = 5.0,
            ultimate = 10.9,
            forbidden = 12.7,
        ),
        29 to SongConstant(
            minimal = 4.0,
            evolved = 7.0,
            ultimate = 10.5,
            forbidden = 12.4,
        ),
        30 to SongConstant(
            minimal = 4.0,
            evolved = 7.0,
            ultimate = 11.5,
            forbidden = 13.4,
        ),
        31 to SongConstant(
            minimal = 2.0,
            evolved = 5.0,
            ultimate = 9.1,
            forbidden = 11.7,
        ),
        32 to SongConstant(
            minimal = 2.0,
            evolved = 6.0,
            ultimate = 9.1,
            forbidden = 12.1,
        ),
        33 to SongConstant(
            minimal = 2.0,
            evolved = 5.0,
            ultimate = 9.7,
            forbidden = 11.8,
        ),
        34 to SongConstant(
            minimal = 3.0,
            evolved = 6.0,
            ultimate = 10.5,
            forbidden = 13.3,
        ),
        35 to SongConstant(
            minimal = 2.0,
            evolved = 6.0,
            ultimate = 9.6,
            forbidden = 12.3,
        ),
        36 to SongConstant(
            minimal = 1.0,
            evolved = 4.0,
            ultimate = 9.3,
            forbidden = 11.0,
        ),
        37 to SongConstant(
            minimal = 3.0,
            evolved = 6.0,
            ultimate = 10.8,
            forbidden = 12.6,
        ),
        38 to SongConstant(
            minimal = 3.0,
            evolved = 6.0,
            ultimate = 10.5,
            forbidden = 12.4,
        ),
        39 to SongConstant(
            minimal = 3.0,
            evolved = 6.0,
            ultimate = 9.4,
            forbidden = 12.4,
        ),
        40 to SongConstant(
            minimal = 1.0,
            evolved = 4.0,
            ultimate = 8.0,
            forbidden = 10.4,
        ),
        41 to SongConstant(
            minimal = 6.0,
            evolved = 10.1,
            ultimate = 12.6,
            forbidden = 14.5,
        ),
        42 to SongConstant(
            minimal = 2.0,
            evolved = 5.0,
            ultimate = 8.0,
            forbidden = 10.4,
        ),
        43 to SongConstant(
            minimal = 2.0,
            evolved = 5.0,
            ultimate = 9.2,
            forbidden = 11.1,
        ),
        44 to SongConstant(
            minimal = 4.0,
            evolved = 7.0,
            ultimate = 10.2,
            forbidden = 13.0,
        ),
        45 to SongConstant(
            minimal = 3.0,
            evolved = 6.0,
            ultimate = 9.1,
            forbidden = 12.6,
        ),
        46 to SongConstant(
            minimal = 3.0,
            evolved = 6.0,
            ultimate = 10.2,
            forbidden = 12.4,
        ),
        47 to SongConstant(
            minimal = 4.0,
            evolved = 7.0,
            ultimate = 10.4,
            forbidden = 12.3,
        ),
        48 to SongConstant(
            minimal = 3.0,
            evolved = 6.0,
            ultimate = 10.7,
            forbidden = 13.5,
        ),
        49 to SongConstant(
            minimal = 3.0,
            evolved = 7.0,
            ultimate = 10.4,
            forbidden = 12.9,
        ),
        50 to SongConstant(
            minimal = 2.0,
            evolved = 5.0,
            ultimate = 9.3,
            forbidden = 11.3,
        ),
        51 to SongConstant(
            minimal = 4.0,
            evolved = 8.0,
            ultimate = 11.8,
            forbidden = 14.7,
        ),
        52 to SongConstant(
            minimal = 2.0,
            evolved = 5.0,
            ultimate = 9.1,
            forbidden = 11.4,
        ),
        53 to SongConstant(
            minimal = 3.0,
            evolved = 6.0,
            ultimate = 10.2,
            forbidden = 13.0,
        ),
        54 to SongConstant(
            minimal = 6.0,
            evolved = 10.0,
            ultimate = 12.9,
            forbidden = 15.0,
        ),
        55 to SongConstant(
            minimal = 3.0,
            evolved = 6.0,
            ultimate = 10.5,
            forbidden = 12.4,
        ),
        56 to SongConstant(
            minimal = 1.0,
            evolved = 4.0,
            ultimate = 8.0,
            forbidden = 10.5,
        ),
        57 to SongConstant(
            minimal = 3.0,
            evolved = 6.0,
            ultimate = 10.5,
            forbidden = 13.3,
        ),
        58 to SongConstant(
            minimal = 1.0,
            evolved = 4.0,
            ultimate = 8.0,
            forbidden = 10.1,
        ),
        59 to SongConstant(
            minimal = 4.0,
            evolved = 7.0,
            ultimate = 10.5,
            forbidden = 13.8,
        ),
        60 to SongConstant(
            minimal = 3.0,
            evolved = 6.0,
            ultimate = 10.3,
            forbidden = 12.5,
        ),
        61 to SongConstant(
            minimal = 3.0,
            evolved = 6.0,
            ultimate = 10.5,
            forbidden = 13.9,
        ),
        62 to SongConstant(
            minimal = 2.0,
            evolved = 6.0,
            ultimate = 10.8,
            forbidden = 12.6,
        ),
        63 to SongConstant(
            minimal = 2.0,
            evolved = 4.0,
            ultimate = 9.2,
            forbidden = 11.4,
        ),
        64 to SongConstant(
            minimal = 1.0,
            evolved = 4.0,
            ultimate = 8.0,
            forbidden = 9.3,
        ),
        65 to SongConstant(
            minimal = 3.0,
            evolved = 6.0,
            ultimate = 9.2,
            forbidden = 10.1,
        ),
        67 to SongConstant(
            minimal = 3.0,
            evolved = 6.0,
            ultimate = 10.7,
            forbidden = 12.0,
        ),
        68 to SongConstant(
            minimal = 3.0,
            evolved = 6.0,
            ultimate = 10.0,
            forbidden = 12.5,
        ),
        69 to SongConstant(
            minimal = 1.0,
            evolved = 4.0,
            ultimate = 8.0,
            forbidden = 10.1,
        ),
        70 to SongConstant(
            minimal = 4.0,
            evolved = 7.0,
            ultimate = 10.5,
            forbidden = 13.2,
        ),
        72 to SongConstant(
            minimal = 3.0,
            evolved = 6.0,
            ultimate = 10.2,
            forbidden = 13.1,
        ),
        73 to SongConstant(
            minimal = 3.0,
            evolved = 6.0,
            ultimate = 9.4,
            forbidden = 11.8,
        ),
        77 to SongConstant(
            minimal = 5.0,
            evolved = 8.0,
            ultimate = 11.2,
            forbidden = 14.0,
        ),
        78 to SongConstant(
            minimal = 3.0,
            evolved = 6.0,
            ultimate = 10.7,
            forbidden = 13.2,
        ),
        79 to SongConstant(
            minimal = 4.0,
            evolved = 7.0,
            ultimate = 10.1,
            forbidden = 13.7,
        ),
        80 to SongConstant(
            minimal = 2.0,
            evolved = 5.0,
            ultimate = 8.9,
            forbidden = 13.1,
        ),
        81 to SongConstant(
            minimal = 2.0,
            evolved = 5.0,
            ultimate = 8.0,
            forbidden = 10.8,
        ),
        82 to SongConstant(
            minimal = 5.0,
            evolved = 8.0,
            ultimate = 11.8,
            forbidden = 14.3,
        ),
        84 to SongConstant(
            minimal = 1.0,
            evolved = 4.0,
            ultimate = 8.0,
            forbidden = 11.2,
        ),
        86 to SongConstant(
            minimal = 4.0,
            evolved = 7.0,
            ultimate = 10.6,
            forbidden = 13.4,
        ),
        87 to SongConstant(
            minimal = 2.0,
            evolved = 5.0,
            ultimate = 8.0,
            forbidden = 11.1,
        ),
        88 to SongConstant(
            minimal = 3.0,
            evolved = 6.0,
            ultimate = 9.3,
            forbidden = 12.4,
        ),
        89 to SongConstant(
            minimal = 4.0,
            evolved = 7.0,
            ultimate = 10.4,
            forbidden = 12.7,
        ),
    )

    val new = BuiltinConstantTable.Experimental

    val diff = old.filter { it.value != new[it.key] }.keys.flatMap { id ->
        buildList {
            if (old[id]!!.minimal != new[id]!!.minimal) {
                add(id to MIN)
            }
            if (old[id]!!.evolved != new[id]!!.evolved) {
                add(id to EVO)
            }
            if (old[id]!!.ultimate != new[id]!!.ultimate) {
                add(id to ULT)
            }
            if (old[id]!!.forbidden != new[id]!!.forbidden) {
                add(id to FBD)
            }
        }
    }

    val diffStrings = diff.sortedBy { it.first }.map { (id, difficult) ->
        buildString {
            append(id)
            append("(")
            append(Songs.entries.find { it.id == id }!!.name)
            append(")")
            append(" ")
            append("[")
            append(difficult)
            append("]")
            append(" ")
            append(
                old[id]!!.run {
                    when (difficult) {
                        MIN -> minimal
                        EVO -> evolved
                        ULT -> ultimate
                        FBD -> forbidden
                    }
                }
            )
            append(" -> ")
            append(
                new[id]!!.run {
                    when (difficult) {
                        MIN -> minimal
                        EVO -> evolved
                        ULT -> ultimate
                        FBD -> forbidden
                    }
                }
            )
        }
    }

    diffStrings.forEach(::println)
}