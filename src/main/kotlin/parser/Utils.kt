package cn.anling.inftool.parser

fun ByteArray.u8(offset: Int): Int =
    this[offset].toInt() and 0xFF

fun ByteArray.u16(offset: Int): Int =
    (this[offset].toInt() and 0xFF) or
            ((this[offset + 1].toInt() and 0xFF) shl 8)

fun ByteArray.i16(offset: Int): Int {
    val value = u16(offset)
    return if (value and 0x8000 != 0) {
        value - 0x10000
    } else {
        value
    }
}

fun ByteArray.u32(offset: Int): Long =
    (this[offset].toLong() and 0xFF) or
            ((this[offset + 1].toLong() and 0xFF) shl 8) or
            ((this[offset + 2].toLong() and 0xFF) shl 16) or
            ((this[offset + 3].toLong() and 0xFF) shl 24)

fun ByteArray.i32(offset: Int): Int =
    (this[offset].toInt() and 0xFF) or
            ((this[offset + 1].toInt() and 0xFF) shl 8) or
            ((this[offset + 2].toInt() and 0xFF) shl 16) or
            ((this[offset + 3].toInt() and 0xFF) shl 24)

fun readMemoryPackStringAt(
    data: ByteArray,
    offset: Int
): String? {
    if (offset + 8 > data.size) {
        return null
    }

    val negativeLength = data.i32(offset)
    val length = data.i32(offset + 4)

    if (length !in 1..256) {
        return null
    }

    if (offset + 8 + length > data.size) {
        return null
    }

    if (negativeLength != -(length + 1)) {
        return null
    }

    val bytes = data.copyOfRange(
        offset + 8,
        offset + 8 + length
    )

    // 与 Python 当前实现一致：
    // 只接受 ASCII 可打印字符
    if (bytes.any { it.toInt() and 0xFF !in 32..126 }) {
        return null
    }

    return bytes.toString(Charsets.US_ASCII)
}