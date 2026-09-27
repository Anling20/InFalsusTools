package cn.anling.inftool.parser

import cn.anling.inftool.readBytesWhenAvailable
import java.nio.file.Path

object SaveFileParser {
    suspend fun parse(path: Path): ParsedSave {
        val data = path.readBytesWhenAvailable()

        return ParsedSave(
            history = HistoryParser.parse(data = data),
            highScores = HighScoresParser.parse(data = data),
        )
    }
}