package cn.anling.inftool.parser

object HistoryParser {

    private const val HISTORY_RECORD = 96
    private const val HISTORY_SLOTS = 1024

    private val DIFFICULTY_FLAGS =
        setOf(1, 2, 4, 8)

    fun parse(data: ByteArray): List<ParsedRecord> {
        val found =
            find(data)
                ?: return emptyList()

        val start = found.first
        val slots = found.second

        val result = ArrayList<ParsedRecord>()

        for (k in 0 until slots) {
            val offset =
                start + k * HISTORY_RECORD

            /*
             * Python:
             *
             * if _empty_history_slot(data, o)
             *     or not _looks_like_history(data, o):
             *     continue
             */
            if (emptySlot(data, offset)) {
                continue
            }

            if (!looksLikeHistory(data, offset)) {
                continue
            }

            result += RecordParser.parseHistory(
                data = data,
                offset = offset,
            )
        }

        return result
    }

    fun find(
        data: ByteArray,
    ): Pair<Int, Int>? {
        val n = data.size

        var offset = 0
        val candidates = ArrayList<Pair<Int, Int>>()

        while (offset + HISTORY_RECORD <= n) {

            /*
             * Python:
             *
             * if not _empty_history_slot(data, o):
             *     o += 1
             *     continue
             */
            if (!emptySlot(data, offset)) {
                offset++
                continue
            }

            /*
             * Python:
             *
             * z = 0
             * p = o
             * while _empty_history_slot(data, p):
             *     z += 1
             *     p += HISTORY_RECORD
             */
            var emptyCount = 0
            var p = offset

            while (emptySlot(data, p)) {
                emptyCount++
                p += HISTORY_RECORD
            }

            /*
             * Python:
             *
             * if z >= 64:
             */
            if (emptyCount >= 64) {

                var bestCount = -1
                var bestStart = -1

                /*
                 * Python:
                 *
                 * for delta in range(HISTORY_RECORD):
                 */
                for (delta in 0 until HISTORY_RECORD) {

                    /*
                     * Python:
                     *
                     * start = (
                     *     p
                     *     - HISTORY_SLOTS * HISTORY_RECORD
                     *     + delta
                     * )
                     */
                    val start =
                        p -
                                HISTORY_SLOTS * HISTORY_RECORD +
                                delta

                    if (start < 0) {
                        continue
                    }

                    /*
                     * Python:
                     *
                     * cnt = sum(
                     *     1
                     *     for k in range(HISTORY_SLOTS)
                     *     if _looks_like_history(
                     *         data,
                     *         start + k * HISTORY_RECORD
                     *     )
                     * )
                     */
                    var count = 0

                    for (k in 0 until HISTORY_SLOTS) {
                        val recordOffset =
                            start +
                                    k * HISTORY_RECORD

                        if (looksLikeHistory(
                                data,
                                recordOffset,
                            )
                        ) {
                            count++
                        }
                    }

                    /*
                     * Python:
                     *
                     * if cnt >= 8 and (
                     *     best is None
                     *     or cnt > best[0]
                     * ):
                     *     best = (cnt, start)
                     */
                    if (
                        count >= 8 &&
                        count > bestCount
                    ) {
                        bestCount = count
                        bestStart = start
                    }
                }

                /*
                 * Python:
                 *
                 * if best:
                 *     candidates.append((cnt, start))
                 */
                if (bestStart >= 0 && bestCount > 0) {
                    candidates += bestCount to bestStart
                }
            }

            /*
             * Python:
             *
             * o = p
             */
            offset = p
        }

        if (candidates.isEmpty()) {
            return null
        }

        // Python: max(candidates) -> choose the candidate with the most valid records.
        val (_, bestStart) = candidates.maxBy { it.first }

        return bestStart to HISTORY_SLOTS
    }

    private fun emptySlot(
        data: ByteArray,
        offset: Int,
    ): Boolean {
        if (
            offset < 0 ||
            offset + HISTORY_RECORD > data.size
        ) {
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
        /*
         * Python:
         *
         * if o < 0 or o + HISTORY_RECORD > len(data):
         *     return False
         */
        if (
            offset < 0 ||
            offset + HISTORY_RECORD > data.size
        ) {
            return false
        }

        val reader =
            BinaryReader(data)

        /*
         * Python:
         *
         * sid = struct.unpack_from('<H', data, o)[0]
         */
        val songId =
            reader.u16(offset)

        /*
         * Python:
         *
         * dif, lamp, clr =
         *     data[o + 2],
         *     data[o + 3],
         *     data[o + 4]
         */
        val difficulty =
            reader.u8(offset + 2)

        val lamp =
            reader.u8(offset + 3)

        val clear =
            reader.u8(offset + 4)

        /*
         * Python:
         *
         * score =
         *     struct.unpack_from(
         *         '<q',
         *         data,
         *         o + 0x50
         *     )[0]
         */
        val score =
            reader.i64(offset + 0x50)

        /*
         * Python:
         *
         * return (
         *     1 <= sid <= 4096
         *     and dif in DIFFICULTY_FLAGS
         *     and lamp <= 2
         *     and clr <= 4
         *     and 0 <= score < 10 ** 9
         * )
         */
        return (
                songId in 1..4096 &&
                        difficulty in DIFFICULTY_FLAGS &&
                        lamp <= 2 &&
                        clear <= 4 &&
                        score in 0 until 1_000_000_000L
                )
    }
}