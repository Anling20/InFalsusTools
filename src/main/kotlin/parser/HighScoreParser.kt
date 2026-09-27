package cn.anling.inftool.parser

object HighScoresParser {

    private const val RECORD_PAYLOAD = 99

    private val DIFFICULTY_FLAGS =
        setOf(1, 2, 4, 8)

    fun parse(data: ByteArray): List<ParsedRecord> {
        val found = find(data)
            ?: return emptyList()

        return found.second.map { location ->
            RecordParser.parseHighScore(
                data = data,
                name = location.name,
                payloadOffset = location.payloadOffset,
            )
        }
    }

    fun find(
        data: ByteArray,
    ): Pair<Int, List<RecordLocation>>? {
        var best: Pair<Int, List<RecordLocation>>? = null

        /*
         * Python:
         *
         * for start in range(0, len(data) - 16):
         */
        for (start in 0 until (data.size - 16).coerceAtLeast(0)) {
            if (start + 1 >= data.size) {
                continue
            }

            elementAt(
                data = data,
                offset = start,
            ) ?: continue

            val records = ArrayList<RecordLocation>()

            var position = start

            while (true) {
                val current = elementAt(
                    data = data,
                    offset = position,
                ) ?: break

                records += current

                position =
                    current.payloadOffset + RECORD_PAYLOAD
            }

            /*
             * Python:
             *
             * if len(recs) < 3:
             *     continue
             */
            if (records.size < 3) {
                continue
            }

            /*
             * Python:
             *
             * cnt = struct.unpack_from(
             *     '<i',
             *     data,
             *     start - 4
             * )[0]
             *
             * if cnt != len(recs):
             *     continue
             */
            if (start < 4) {
                continue
            }

            val count =
                BinaryReader(data).i32(start - 4)

            if (count != records.size) {
                continue
            }

            /*
             * Python:
             *
             * if best is None or len(recs) > len(best[1]):
             *     best = (start - 4, recs)
             */
            if (best == null ||
                records.size > best.second.size
            ) {
                best = start - 4 to records
            }
        }

        return best
    }

    private fun elementAt(
        data: ByteArray,
        offset: Int,
    ): RecordLocation? {
        /*
         * Python:
         *
         * if off + 1 >= len(data):
         *     return None
         */
        if (offset + 1 >= data.size) {
            return null
        }

        /*
         * Python:
         *
         * name = memorypack.string_at(data, off + 1)
         */
        val name =
            MemoryPack.stringAt(
                data = data,
                offset = offset + 1,
            ) ?: return null

        /*
         * Python:
         *
         * pay = off + 1 + 8 + len(name)
         */
        val payloadOffset =
            offset + 1 + 8 + name.length

        /*
         * Python:
         *
         * if pay + RECORD_PAYLOAD > len(data):
         *     return None
         */
        if (payloadOffset + RECORD_PAYLOAD > data.size) {
            return null
        }

        val reader =
            BinaryReader(data)

        /*
         * GameResultKey #1
         */
        val songId1 =
            reader.u16(payloadOffset)

        val difficulty1 =
            reader.u8(payloadOffset + 2)

        /*
         * GameResultKey #2
         *
         * +3 是 GameResultV4 开头。
         * Python 用这个重复值来验证记录。
         */
        val songId2 =
            reader.u16(payloadOffset + 3)

        val difficulty2 =
            reader.u8(payloadOffset + 5)

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
}