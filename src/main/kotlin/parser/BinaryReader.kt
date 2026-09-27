package cn.anling.inftool.parser

import java.nio.ByteBuffer
import java.nio.ByteOrder

class BinaryReader(
    private val data: ByteArray,
) {
    private fun buffer(offset: Int): ByteBuffer =
        ByteBuffer
            .wrap(data, offset, data.size - offset)
            .order(ByteOrder.LITTLE_ENDIAN)

    fun u8(offset: Int): Int =
        data[offset].toInt() and 0xFF

    fun i8(offset: Int): Byte =
        data[offset]

    fun u16(offset: Int): Int =
        buffer(offset).short.toInt() and 0xFFFF

    fun i16(offset: Int): Int =
        buffer(offset).short.toInt()

    fun u32(offset: Int): Long =
        buffer(offset).int.toLong() and 0xFFFF_FFFFL

    fun i32(offset: Int): Int =
        buffer(offset).int

    fun u64(offset: Int): Long =
        buffer(offset).long

    fun i64(offset: Int): Long =
        buffer(offset).long

    fun f64(offset: Int): Double =
        buffer(offset).double

    fun bytes(
        offset: Int,
        length: Int,
    ): ByteArray =
        data.copyOfRange(offset, offset + length)

    fun hex(
        offset: Int,
        length: Int,
    ): String =
        bytes(offset, length)
            .joinToString("") {
                "%02x".format(it.toInt() and 0xFF)
            }

    fun size(): Int =
        data.size
}