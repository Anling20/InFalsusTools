package cn.anling.inftool.parser

class Parser(private val data: ByteArray) {
    private val songNames: Map<Int, String> = emptyMap()

    private fun findRecords(): List<RecordLocation>? {

        var best: List<RecordLocation>? = null

        for (start in 0 until data.size - 16) {

            if (elementAt(start) == null) {
                continue
            }

            val records = mutableListOf<RecordLocation>()

            var position = start

            while (true) {

                val record = elementAt(position) ?: break

                records += record

                position = record.payloadOffset + RECORD_PAYLOAD
            }

            if (records.size < 3) {
                continue
            }

            if (start < 4) {
                continue
            }

            val count = data.i32(start - 4)

            if (count != records.size) {
                continue
            }

            if (best == null || records.size > best.size) {
                best = records
            }
        }

        return best
    }

    private fun elementAt(
        offset: Int
    ): RecordLocation? {

        if (offset + 1 >= data.size) {
            return null
        }

        val name = readMemoryPackStringAt(
            data, offset + 1
        ) ?: return null

        val payloadOffset = offset + 1 + 8 + name.length

        if (payloadOffset + RECORD_PAYLOAD > data.size) {
            return null
        }

        val songId1 = data.u16(payloadOffset)

        val difficulty1 = data.u8(payloadOffset + 2)

        val songId2 = data.u16(payloadOffset + 3)

        val difficulty2 = data.u8(payloadOffset + 5)

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
            name = name, payloadOffset = payloadOffset
        )
    }

    private fun decodeRecord(
        record: RecordLocation
    ): ParseResult {

        val base = record.payloadOffset

        val songId = data.u16(base)

        val difficultyFlag = data.u8(base + 2)

        val difficultyIndex = Integer.numberOfTrailingZeros(
            difficultyFlag
        )

        val counts = JudgementCounts(
            tap = readJudgementCount(
                base + 0x0D
            ),

            hold = readJudgementCount(
                base + 0x1D
            ),

            field = readJudgementCount(
                base + 0x2D
            ),

            flick = readJudgementCount(
                base + 0x3D
            )
        )

        return ParseResult(
            songId = songId,

            songName = songNames[songId] ?: record.name,

            difficultFlag = difficultyFlag,

            difficultIndex = difficultyIndex,

            score = data.u32(base + 0x53),

            maxLink = data.u16(base + 0x09),

            lamp = data.u8(base + 0x06),

            resultClear = data.u16(base + 0x07),

            paceValue = data.i16(base + 0x0B),

            counts = counts
        )
    }

    private fun readJudgementCount(
        offset: Int
    ): JudgementCount {

        return JudgementCount(
            none = data.u16(offset),

            `break` = data.u16(offset + 2),

            nearEarly = data.u16(offset + 4),

            nearLate = data.u16(offset + 6),

            exactEarly = data.u16(offset + 8),

            exactLate = data.u16(offset + 10),

            exactPlus = data.u16(offset + 12),

            unused = data.u16(offset + 14)
        )
    }

    fun parse(): List<ParseResult> {
        val records = findRecords() ?: return emptyList()

        return records.map {
            decodeRecord(it)
        }
    }

    companion object {
        private const val RECORD_PAYLOAD = 99

        private val DIFFICULTY_FLAGS = setOf(1, 2, 4, 8)
    }

    private data class RecordLocation(
        val name: String, val payloadOffset: Int
    )
}