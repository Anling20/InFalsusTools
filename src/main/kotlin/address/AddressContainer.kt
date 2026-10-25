package cn.anling.inftool.address

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class AddressContainer(
    val gameScene: GameScene,
    val resultsScene: ResultsScene,
    val resultsSceneManager: ResultsSceneManager,
) {
    @Serializable
    data class GameScene(
        @Serializable(with = HexToLongSerializer::class)
        val classRVA: Long,
        @Serializable(with = HexToLongSerializer::class)
        val staticFieldsOffset: Long,
        @Serializable(with = HexToLongSerializer::class)
        val instanceReferenceOffset: Long,
    )

    @Serializable
    data class ResultsScene(
        @Serializable(with = HexToLongSerializer::class)
        val classRVA: Long,
    )

    @Serializable
    data class ResultsSceneManager(
        @Serializable(with = HexToLongSerializer::class)
        val classRVA: Long,
        @Serializable(with = HexToLongSerializer::class)
        val staticFieldsOffset: Long,
        @Serializable(with = HexToLongSerializer::class)
        val instanceReferenceOffset: Long,
        @Serializable(with = HexToLongSerializer::class)
        val array1Offset: Long,
        @Serializable(with = HexToLongSerializer::class)
        val array2Offset: Long,
        @Serializable(with = HexToLongSerializer::class)
        val arrayCountOffset: Long,
        @Serializable(with = HexToLongSerializer::class)
        val arrayDataOffset: Long,
        @Serializable(with = HexToLongSerializer::class)
        val arrayNodeInstanceOffset: Long,
    )

    object HexToLongSerializer : KSerializer<Long> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor(HexToLongSerializer::class.qualifiedName!!, PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: Long) {
            encoder.encodeString("0x${value.toString(16).uppercase()}")
        }

        override fun deserialize(decoder: Decoder): Long =
            decoder.decodeString()
                .removePrefix("0x")
                .toLong(16)

    }
}