package cn.anling.inftool.parser

import cn.anling.inftool.readBytesWhenAvailable
import io.github.vinceglb.filekit.PlatformFile

object SaveFileParser {
    suspend fun parse(file: PlatformFile): ParsedSave {
        val data = file.readBytesWhenAvailable()

        return ParsedSave(
            history = HistoryParser.parse(data = data),
            highScores = HighScoresParser.parse(data = data),
        )
    }
}