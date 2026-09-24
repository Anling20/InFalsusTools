package cn.anling.inftool.parser

class BinaryReader(
    private val data: ByteArray
) {
    var position: Int = 0

    val remaining: Int
        get() = data.size - position

    fun u8(): Int {
        check(position + 1 <= data.size)

        return data[position++].toInt() and 0xFF
    }

    fun i8(): Byte {
        check(position + 1 <= data.size)

        return data[position++]
    }

    fun u16(): Int {
        check(position + 2 <= data.size)

        val value =
            (data[position].toInt() and 0xFF) or
                    ((data[position + 1].toInt() and 0xFF) shl 8)

        position += 2

        return value
    }

    fun i16(): Int {
        return u16().let {
            if (it and 0x8000 != 0) {
                it - 0x10000
            } else {
                it
            }
        }
    }

    fun u32(): Long {
        check(position + 4 <= data.size)

        val value =
            (data[position].toLong() and 0xFF) or
                    ((data[position + 1].toLong() and 0xFF) shl 8) or
                    ((data[position + 2].toLong() and 0xFF) shl 16) or
                    ((data[position + 3].toLong() and 0xFF) shl 24)

        position += 4

        return value
    }

    fun i32(): Int {
        return u32().toInt()
    }

    fun bytes(length: Int): ByteArray {
        check(position + length <= data.size)

        return data.copyOfRange(
            position,
            position + length
        ).also {
            position += length
        }
    }

    fun skip(length: Int) {
        check(position + length <= data.size)
        position += length
    }
}

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