package cn.anling.inftool.song

import kotlinx.serialization.Serializable

data class Song(
    val id: Int,
    val name: String,
)

@Serializable
data class SongConstant(
    val minimal: Double,
    val evolved: Double,
    val ultimate: Double,
    val forbidden: Double,
)

typealias ConstantTable = Map<Int, SongConstant>