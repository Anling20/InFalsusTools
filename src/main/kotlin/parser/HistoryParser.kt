package cn.anling.inftool.parser

private data class HistoryArray(
    val start: Int,
    val length: Int,
    val count: Int,
)

object HistoryParser {
    private const val RECORD_PAYLOAD = 99
    private const val HISTORY_RECORD = 96
    private const val HISTORY_HEADER = 5

    private val DIFFICULTY_FLAGS = setOf(1, 2, 4, 8)

    fun parse(data: ByteArray): List<ParsedRecord> {
        val found = find(data) ?: return emptyList()

        val result = ArrayList<ParsedRecord>()

        for (k in 0 until minOf(found.count, found.length)) {
            val offset = found.start + k * HISTORY_RECORD

            if (emptySlot(data, offset)) continue
            if (!looksLikeHistory(data, offset)) continue

            result += RecordParser.parseHistory(
                data = data,
                offset = offset,
            )
        }

        return result
    }

    private fun find(data: ByteArray): HistoryArray? {
        // 对应 Python find_records(data)
        val records = HighScoresParser.find(data)
            ?.second
            ?: return null

        if (records.isEmpty()) return null

        // 最后一条高分记录载荷末尾 + 历史数组头部
        val start = records.last().payloadOffset +
                RECORD_PAYLOAD +
                HISTORY_HEADER

        if (start < HISTORY_HEADER || start + 4 > data.size) {
            return null
        }

        val reader = BinaryReader(data)

        // 数组起点前 4 字节是 int32 槽位总数
        val length = reader.i32(start - 4)

        if (length !in 8..(1 shl 22)) {
            return null
        }

        val endLong = start.toLong() +
                length.toLong() * HISTORY_RECORD

        if (endLong + 4 > data.size) {
            return null
        }

        val end = endLong.toInt()

        // 定长数组末尾的 int32 是已用记录数
        val count = reader.i32(end)

        if (count !in 0..length) {
            return null
        }

        // 与上游一致：有记录时，第一槽必须通过合法性校验
        if (count > 0 && !looksLikeHistory(data, start)) {
            return null
        }

        return HistoryArray(
            start = start,
            length = length,
            count = count,
        )
    }

    private fun emptySlot(
        data: ByteArray,
        offset: Int,
    ): Boolean {
        if (offset < 0 || offset.toLong() + HISTORY_RECORD > data.size) {
            return false
        }

        for (i in 0 until HISTORY_RECORD) {
            if (data[offset + i].toInt() != 0) {
                return false
            }
        }

        return true
    }

    private fun looksLikeHistory(
        data: ByteArray,
        offset: Int,
    ): Boolean {
        if (offset < 0 || offset.toLong() + HISTORY_RECORD > data.size) {
            return false
        }

        val reader = BinaryReader(data)

        val songId = reader.u16(offset)
        val difficulty = reader.u8(offset + 2)
        val lamp = reader.u8(offset + 3)
        val clear = reader.u8(offset + 4)
        val score = reader.i64(offset + 0x50)

        return songId in 1..4096 &&
                difficulty in DIFFICULTY_FLAGS &&
                lamp <= 2 &&
                clear <= 4 &&
                score in 0 until 1_000_000_000L
    }
}