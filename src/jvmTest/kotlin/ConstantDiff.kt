import cn.anling.inftool.Difficult.*
import cn.anling.inftool.readStringSync
import cn.anling.inftool.song.ConstantTable
import cn.anling.inftool.song.Songs
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.serialization.json.Json

fun main() {
    val oldJson = """
        {
          "2": {
            "minimal": 1.0,
            "evolved": 5.0,
            "ultimate": 9.4,
            "forbidden": 11.4
          },
          "3": {
            "minimal": 2.0,
            "evolved": 5.0,
            "ultimate": 9.5,
            "forbidden": 11.5
          },
          "5": {
            "minimal": 3.0,
            "evolved": 6.0,
            "ultimate": 10.2,
            "forbidden": 12.1
          },
          "6": {
            "minimal": 4.0,
            "evolved": 7.0,
            "ultimate": 10.7,
            "forbidden": 13.8
          },
          "7": {
            "minimal": 2.0,
            "evolved": 5.0,
            "ultimate": 10.1,
            "forbidden": 11.1
          },
          "8": {
            "minimal": 6.0,
            "evolved": 9.8,
            "ultimate": 12.1,
            "forbidden": 14.6
          },
          "9": {
            "minimal": 5.0,
            "evolved": 8.0,
            "ultimate": 11.8,
            "forbidden": 14.2
          },
          "10": {
            "minimal": 4.0,
            "evolved": 7.0,
            "ultimate": 10.9,
            "forbidden": 13.3
          },
          "11": {
            "minimal": 2.0,
            "evolved": 5.0,
            "ultimate": 9.2,
            "forbidden": 12.4
          },
          "12": {
            "minimal": 3.0,
            "evolved": 6.0,
            "ultimate": 10.5,
            "forbidden": 12.3
          },
          "13": {
            "minimal": 3.0,
            "evolved": 6.0,
            "ultimate": 9.4,
            "forbidden": 12.2
          },
          "14": {
            "minimal": 4.0,
            "evolved": 7.0,
            "ultimate": 11.8,
            "forbidden": 13.7
          },
          "15": {
            "minimal": 1.0,
            "evolved": 4.0,
            "ultimate": 8.0,
            "forbidden": 10.9
          },
          "16": {
            "minimal": 3.0,
            "evolved": 6.0,
            "ultimate": 10.3,
            "forbidden": 12.7
          },
          "17": {
            "minimal": 1.0,
            "evolved": 4.0,
            "ultimate": 8.0,
            "forbidden": 11.1
          },
          "18": {
            "minimal": 2.0,
            "evolved": 6.0,
            "ultimate": 10.8,
            "forbidden": 12.7
          },
          "19": {
            "minimal": 2.0,
            "evolved": 5.0,
            "ultimate": 9.9,
            "forbidden": 11.7
          },
          "20": {
            "minimal": 2.0,
            "evolved": 5.0,
            "ultimate": 9.9,
            "forbidden": 11.7
          },
          "21": {
            "minimal": 2.0,
            "evolved": 5.0,
            "ultimate": 10.4,
            "forbidden": 12.4
          },
          "22": {
            "minimal": 2.0,
            "evolved": 6.0,
            "ultimate": 9.7,
            "forbidden": 11.0
          },
          "23": {
            "minimal": 3.0,
            "evolved": 6.0,
            "ultimate": 10.3,
            "forbidden": 12.3
          },
          "24": {
            "minimal": 2.0,
            "evolved": 5.0,
            "ultimate": 9.1,
            "forbidden": 11.3
          },
          "25": {
            "minimal": 2.0,
            "evolved": 5.0,
            "ultimate": 9.5,
            "forbidden": 11.2
          },
          "26": {
            "minimal": 4.0,
            "evolved": 7.0,
            "ultimate": 10.7,
            "forbidden": 13.5
          },
          "27": {
            "minimal": 4.0,
            "evolved": 7.0,
            "ultimate": 11.5,
            "forbidden": 13.5
          },
          "28": {
            "minimal": 2.0,
            "evolved": 5.0,
            "ultimate": 10.9,
            "forbidden": 12.7
          },
          "29": {
            "minimal": 4.0,
            "evolved": 7.0,
            "ultimate": 10.5,
            "forbidden": 12.4
          },
          "30": {
            "minimal": 4.0,
            "evolved": 7.0,
            "ultimate": 11.5,
            "forbidden": 13.4
          },
          "31": {
            "minimal": 2.0,
            "evolved": 5.0,
            "ultimate": 9.2,
            "forbidden": 11.7
          },
          "32": {
            "minimal": 2.0,
            "evolved": 6.0,
            "ultimate": 9.1,
            "forbidden": 12.1
          },
          "33": {
            "minimal": 2.0,
            "evolved": 5.0,
            "ultimate": 9.7,
            "forbidden": 11.8
          },
          "34": {
            "minimal": 3.0,
            "evolved": 6.0,
            "ultimate": 10.5,
            "forbidden": 13.3
          },
          "35": {
            "minimal": 2.0,
            "evolved": 6.0,
            "ultimate": 9.6,
            "forbidden": 12.3
          },
          "36": {
            "minimal": 1.0,
            "evolved": 4.0,
            "ultimate": 9.4,
            "forbidden": 11.0
          },
          "37": {
            "minimal": 3.0,
            "evolved": 6.0,
            "ultimate": 10.8,
            "forbidden": 12.6
          },
          "38": {
            "minimal": 3.0,
            "evolved": 6.0,
            "ultimate": 10.5,
            "forbidden": 12.4
          },
          "39": {
            "minimal": 3.0,
            "evolved": 6.0,
            "ultimate": 9.4,
            "forbidden": 12.4
          },
          "40": {
            "minimal": 1.0,
            "evolved": 4.0,
            "ultimate": 8.0,
            "forbidden": 10.4
          },
          "41": {
            "minimal": 6.0,
            "evolved": 10.1,
            "ultimate": 12.6,
            "forbidden": 14.5
          },
          "42": {
            "minimal": 2.0,
            "evolved": 5.0,
            "ultimate": 8.0,
            "forbidden": 10.4
          },
          "43": {
            "minimal": 2.0,
            "evolved": 5.0,
            "ultimate": 9.3,
            "forbidden": 11.1
          },
          "44": {
            "minimal": 4.0,
            "evolved": 7.0,
            "ultimate": 10.2,
            "forbidden": 13.0
          },
          "45": {
            "minimal": 3.0,
            "evolved": 6.0,
            "ultimate": 9.1,
            "forbidden": 12.6
          },
          "46": {
            "minimal": 3.0,
            "evolved": 6.0,
            "ultimate": 10.2,
            "forbidden": 12.4
          },
          "47": {
            "minimal": 4.0,
            "evolved": 7.0,
            "ultimate": 10.4,
            "forbidden": 12.3
          },
          "48": {
            "minimal": 3.0,
            "evolved": 6.0,
            "ultimate": 10.7,
            "forbidden": 13.5
          },
          "49": {
            "minimal": 3.0,
            "evolved": 7.0,
            "ultimate": 10.4,
            "forbidden": 12.9
          },
          "50": {
            "minimal": 2.0,
            "evolved": 5.0,
            "ultimate": 9.4,
            "forbidden": 11.3
          },
          "51": {
            "minimal": 4.0,
            "evolved": 8.0,
            "ultimate": 11.8,
            "forbidden": 14.7
          },
          "52": {
            "minimal": 2.0,
            "evolved": 5.0,
            "ultimate": 9.1,
            "forbidden": 11.4
          },
          "53": {
            "minimal": 3.0,
            "evolved": 6.0,
            "ultimate": 10.2,
            "forbidden": 13.0
          },
          "54": {
            "minimal": 6.0,
            "evolved": 10.0,
            "ultimate": 12.9,
            "forbidden": 15.0
          },
          "55": {
            "minimal": 3.0,
            "evolved": 6.0,
            "ultimate": 10.5,
            "forbidden": 12.4
          },
          "56": {
            "minimal": 1.0,
            "evolved": 4.0,
            "ultimate": 8.0,
            "forbidden": 10.5
          },
          "57": {
            "minimal": 3.0,
            "evolved": 6.0,
            "ultimate": 10.5,
            "forbidden": 13.3
          },
          "58": {
            "minimal": 1.0,
            "evolved": 4.0,
            "ultimate": 8.0,
            "forbidden": 10.1
          },
          "59": {
            "minimal": 4.0,
            "evolved": 7.0,
            "ultimate": 10.5,
            "forbidden": 13.8
          },
          "60": {
            "minimal": 3.0,
            "evolved": 6.0,
            "ultimate": 10.3,
            "forbidden": 12.5
          },
          "61": {
            "minimal": 3.0,
            "evolved": 6.0,
            "ultimate": 10.5,
            "forbidden": 13.9
          },
          "62": {
            "minimal": 2.0,
            "evolved": 6.0,
            "ultimate": 10.8,
            "forbidden": 12.6
          },
          "63": {
            "minimal": 2.0,
            "evolved": 4.0,
            "ultimate": 9.2,
            "forbidden": 11.4
          },
          "64": {
            "minimal": 1.0,
            "evolved": 4.0,
            "ultimate": 8.0,
            "forbidden": 9.4
          },
          "65": {
            "minimal": 3.0,
            "evolved": 6.0,
            "ultimate": 9.2,
            "forbidden": 10.1
          },
          "67": {
            "minimal": 3.0,
            "evolved": 6.0,
            "ultimate": 10.7,
            "forbidden": 12.0
          },
          "68": {
            "minimal": 3.0,
            "evolved": 6.0,
            "ultimate": 10.0,
            "forbidden": 12.5
          },
          "69": {
            "minimal": 1.0,
            "evolved": 4.0,
            "ultimate": 8.0,
            "forbidden": 10.1
          },
          "70": {
            "minimal": 4.0,
            "evolved": 7.0,
            "ultimate": 10.5,
            "forbidden": 13.2
          },
          "72": {
            "minimal": 3.0,
            "evolved": 6.0,
            "ultimate": 10.2,
            "forbidden": 13.1
          },
          "73": {
            "minimal": 3.0,
            "evolved": 6.0,
            "ultimate": 9.4,
            "forbidden": 11.8
          },
          "77": {
            "minimal": 5.0,
            "evolved": 8.0,
            "ultimate": 11.2,
            "forbidden": 14.0
          },
          "78": {
            "minimal": 3.0,
            "evolved": 6.0,
            "ultimate": 10.7,
            "forbidden": 13.1
          },
          "79": {
            "minimal": 4.0,
            "evolved": 7.0,
            "ultimate": 10.1,
            "forbidden": 13.7
          },
          "80": {
            "minimal": 2.0,
            "evolved": 5.0,
            "ultimate": 9.0,
            "forbidden": 13.2
          },
          "81": {
            "minimal": 2.0,
            "evolved": 5.0,
            "ultimate": 8.0,
            "forbidden": 10.8
          },
          "82": {
            "minimal": 5.0,
            "evolved": 8.0,
            "ultimate": 11.8,
            "forbidden": 14.3
          },
          "84": {
            "minimal": 1.0,
            "evolved": 4.0,
            "ultimate": 8.0,
            "forbidden": 11.2
          },
          "86": {
            "minimal": 4.0,
            "evolved": 7.0,
            "ultimate": 10.6,
            "forbidden": 13.4
          },
          "87": {
            "minimal": 2.0,
            "evolved": 5.0,
            "ultimate": 8.0,
            "forbidden": 11.1
          },
          "88": {
            "minimal": 3.0,
            "evolved": 6.0,
            "ultimate": 9.3,
            "forbidden": 12.4
          },
          "89": {
            "minimal": 4.0,
            "evolved": 7.0,
            "ultimate": 10.4,
            "forbidden": 12.7
          }
        }
    """.trimIndent()

    val old = Json.decodeFromString<ConstantTable>(oldJson)
    val new = Json.decodeFromString<ConstantTable>(
        PlatformFile("C:/Users/Anling/IdeaProjects/InFalsusTools_Data/constants.json").readStringSync()
    )

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