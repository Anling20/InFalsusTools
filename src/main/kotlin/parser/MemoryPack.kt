package cn.anling.inftool.parser


object MemoryPack {
    fun stringAt(
        data: ByteArray,
        offset: Int,
    ): String? {
        if (offset < 0 || offset + 8 > data.size) {
            return null
        }

        val reader = BinaryReader(data)

        val negativeLength = reader.i32(offset)
        val byteLength = reader.i32(offset + 4)

        if (byteLength !in 1..256) {
            return null
        }

        if (negativeLength != -(byteLength + 1)) {
            return null
        }

        if (offset + 8 + byteLength > data.size) {
            return null
        }

        val bytes = reader.bytes(
            offset + 8,
            byteLength,
        )

        if (!bytes.all { it.toInt() in 32..126 }) {
            return null
        }

        return bytes.toString(Charsets.US_ASCII)
    }
}