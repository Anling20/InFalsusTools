package cn.anling.inftool.parser

import cn.anling.inftool.and

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
        var best: List<RecordLocation>? = null
        for (start in 0 until (data.size - 16).coerceAtLeast(0)) {
            if (start + 1 >= data.size) {
                continue
            }

            elementAt(data, start)
                ?: continue

            val records = ArrayList<RecordLocation>()
            var position = start

            while (true) {
                val current = elementAt(data, position)
                    ?: break

                records += current
                position = current.payloadOffset + RECORD_PAYLOAD
            }

            if (records.size < 3) {
                continue
            }

            if (start < 4) {
                continue
            }

            val count = BinaryReader(data).i32(start - 4)

            if (count != records.size) {
                continue
            }

            if (best == null || records.size > best.size) {
                best = records
            }
        }

        val records = best
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

    private fun elementAt(
        data: ByteArray,
        offset: Int,
    ): RecordLocation? {
        if (offset + 1 >= data.size) {
            return null
        }

        val name = MemoryPack.stringAt(data, offset + 1)
            ?: return null

        val payloadOffset = offset + 1 + 8 + name.length

        if (payloadOffset + RECORD_PAYLOAD > data.size) {
            return null
        }

        val reader = BinaryReader(data)
        val songId1 = reader.u16(payloadOffset)
        val difficulty1 = reader.u8(payloadOffset + 2)
        val songId2 = reader.u16(payloadOffset + 3)
        val difficulty2 = reader.u8(payloadOffset + 5)

        if (songId1 != songId2) {
            return null
        }

        if (difficulty1 != difficulty2) {
            return null
        }

        if (difficulty1 !in DIFFICULTY_FLAGS) {
            return null
        }

        return RecordLocation(
            name = name,
            payloadOffset = payloadOffset,
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

        return and(
            songId in 1..4096,
            difficulty in DIFFICULTY_FLAGS,
            lamp <= 2,
            clear <= 4,
            score in 0 until 1_000_000_000L,
        )
    }
}