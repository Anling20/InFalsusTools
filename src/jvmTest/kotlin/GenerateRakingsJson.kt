import cn.anling.inftool.ranking.PlayRecordsContainer
import cn.anling.inftool.ranking.Ranking
import cn.anling.inftool.ranking.SongPlay
import io.github.vinceglb.filekit.*
import kotlinx.serialization.json.Json

suspend fun main() {
    val folder = PlatformFile("C:/Users/Anling/IdeaProjects/InFalsusTools/plays")
    val map = folder
        .list()
        .filter { it.name.endsWith(".json") }
        .map { file ->
            Json.decodeFromString<PlayRecordsContainer>(file.readString())
        }

    val flatMap = map
        .flatMap { (user, playList) ->
            playList.map {
                user to it
            }
        }

    val ranking: Ranking = flatMap
        .groupBy { it.second.id }
        .mapValues { (_, playList) ->
            playList.groupBy { it.second.difficult }
                .mapValues { (_, playList) ->
                    playList.map { (user, play) ->
                        SongPlay(
                            user = user,
                            score = play.score,
                            clearType = play.clearType,
                            diveValue = play.diveValue,
                            judgement = play.judgement,
                        )
                    }
                }
        }

    PlatformFile("C:/Users/Anling/IdeaProjects/InFalsusTools_Data/ranking.json").writeString(Json.encodeToString(ranking))
}