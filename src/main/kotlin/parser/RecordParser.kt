package cn.anling.inftool.parser

object RecordParser {
    private const val RECORD_PAYLOAD = 99

    private const val COUNT_BLOCK_BASE = 0x0D
    private const val COUNT_BLOCK_SIZE = 16

    private val JUDGEMENT_FIELDS = arrayOf(
        "none",
        "miss",
        "nearEarly",
        "nearLate",
        "perfectEarly",
        "perfectLate",
        "shiny",
        "unused",
    )

    private val COUNT_NAMES = arrayOf(
        "tap",
        "hold",
        "skyArea",
        "flick",
    )

    fun parseHighScore(
        data: ByteArray,
        name: String,
        payloadOffset: Int,
    ): ParsedRecord {
        return parse(
            data = data,
            name = name,
            payloadOffset = payloadOffset,
            bare = false,
        )
    }

    fun parseHistory(
        data: ByteArray,
        offset: Int,
    ): ParsedRecord {
        return parse(
            data = data,
            name = null,
            payloadOffset = offset,
            bare = true,
        )
    }

    private fun parse(
        data: ByteArray,
        name: String?,
        payloadOffset: Int,
        bare: Boolean,
    ): ParsedRecord {
        val shift = if (bare) 3 else 0
        val payloadSize = RECORD_PAYLOAD - shift

        require(payloadOffset >= 0 && payloadOffset + payloadSize <= data.size) {
            "Record payload out of bounds: offset=$payloadOffset size=$payloadSize data=${data.size}"
        }

        val payload = data.copyOfRange(payloadOffset, payloadOffset + payloadSize)

        val reader = BinaryReader(payload)

        /*
         * Python:
         *
         * sid, dif = struct.unpack_from('<HB', pl, 0)
         */
        val songId = reader.u16(0)
        val difficultyFlag = reader.u8(2)

        /*
         * Python:
         *
         * u16 = lambda o: struct.unpack_from('<H', pl, o - shift)[0]
         *
         * 所以这里所有 GameResultV4 offset 都通过 v4U16()
         * 自动处理 History 的 -3。
         */
        fun v4U8(offset: Int): Int =
            reader.u8(offset - shift)

        fun v4U16(offset: Int): Int =
            reader.u16(offset - shift)

        fun v4I16(offset: Int): Int =
            reader.i16(offset - shift)

        fun v4I64(offset: Int): Long =
            reader.i64(offset - shift)

        val blocks = LinkedHashMap<String, ParsedJudgementTypeCount>()

        for (blockIndex in COUNT_NAMES.indices) {
            val blockName = COUNT_NAMES[blockIndex]

            val base = COUNT_BLOCK_BASE +
                    blockIndex * COUNT_BLOCK_SIZE

            val values = IntArray(JUDGEMENT_FIELDS.size)

            for (fieldIndex in JUDGEMENT_FIELDS.indices) {
                values[fieldIndex] =
                    v4U16(base + fieldIndex * 2)
            }

            blocks[blockName] = ParsedJudgementTypeCount(
                none = values[0],
                miss = values[1],
                nearEarly = values[2],
                nearLate = values[3],
                perfectEarly = values[4],
                perfectLate = values[5],
                shiny = values[6],
                unused = values[7],
            )
        }

        val counts = ParsedCounts(
            tap = blocks.getValue("tap"),
            hold = blocks.getValue("hold"),
            skyArea = blocks.getValue("skyArea"),
            flick = blocks.getValue("flick"),
        )

        /*
         * Python:
         *
         * 'baseName': idmap.get(sid, name)
         *
         * HighScore:
         *     name = MemoryPack 解析出来的 basename
         *
         * History:
         *     name = None
         *     所以必须从 songNames[songId] 找。
         */
        val baseName = name ?: ""

        /*
         * Python:
         *
         * 'score': struct.unpack_from('<q', pl, 0x53 - shift)[0]
         */
        val score = v4I64(0x53)

        /*
         * Python:
         *
         * 'maxCombo': u16(0x09)
         */
        val maxCombo = v4U16(0x09)

        /*
         * Python:
         *
         * 'lamp': pl[0x06 - shift]
         */
        val lamp = v4U8(0x06)

        /*
         * Python:
         *
         * 'resultClear': u16(0x07)
         */
        val resultClear = v4U16(0x07)

        /*
         * Python:
         *
         * 'paceValue': struct.unpack_from('<h', pl, 0x0b - shift)[0]
         */
        val paceValue = v4I16(0x0B)

        /*
         * Python:
         *
         * 'secondsSinceEpochUtc':
         *     struct.unpack_from('<q', pl, 0x5B - shift)[0]
         */
        val secondsSinceEpochUtc = v4I64(0x5B)

        return ParsedRecord(
            songId = songId,
            baseName = baseName,
            difficultyFlag = difficultyFlag,
            difficultyIndex = difficultyIndex(difficultyFlag),
            score = score,
            maxCombo = maxCombo,
            lamp = lamp,
            resultClear = resultClear,
            paceValue = paceValue,
            secondsSinceEpochUtc = secondsSinceEpochUtc,
            counts = counts,
            payloadHex = payload.joinToString("") { "%02x".format(it.toInt() and 0xFF) },
        )
    }

    private fun difficultyIndex(difficultyFlag: Int): Int {
        return when (difficultyFlag) {
            1 -> 0
            2 -> 1
            4 -> 2
            8 -> 3
            else -> error(
                "Invalid difficulty flag: $difficultyFlag"
            )
        }
    }
}