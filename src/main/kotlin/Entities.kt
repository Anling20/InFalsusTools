package cn.anling.inftool

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import cn.anling.inftool.song.Song
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.serialization.Serializable

enum class Difficult {
    MIN, EVO, ULT, FBD
}

enum class ClearType {
    None, DiveFailed, DiveCleared, FullLink, PerfectDive,
}

enum class Grading {
    EXPlus, EX, AA, A, B, C, D
}

data class ProcessedDiveRecord(
    val song: Song,
    val difficult: Difficult,
    val score: Long,
    val clearType: ClearType,
    val diveValue: Int,
    val judgement: CountContainer,
    val potential: Double,
    val grading: Grading,
    val level: Double,
    val raw: DiveRecord,
)

data class DiveRecord(
    val id: Int,
    val name: String,
    val difficult: Difficult,
    val score: Long,
    val clearType: ClearType,
    val diveValue: Int,
    val judgement: CountContainer,
)

data class CountContainer(
    val maxLink: Int,
    val tap: JudgementCount,
    val hold: JudgementCount,
    val field: JudgementCount,
    val flick: JudgementCount,
) {
    val exactPlus: Int =
        tap.exactPlus + hold.exactPlus + field.exactPlus + flick.exactPlus

    val exactEarly: Int =
        tap.exactEarly + hold.exactEarly + field.exactEarly + flick.exactEarly

    val exactLate: Int =
        tap.exactLate + hold.exactLate + field.exactLate + flick.exactLate

    val exact: Int =
        exactEarly + exactLate

    val nearEarly: Int =
        tap.nearEarly + hold.nearEarly + field.nearEarly + flick.nearEarly

    val nearLate: Int =
        tap.nearLate + hold.nearLate + field.nearLate + flick.nearLate

    val near: Int =
        nearEarly + nearLate

    val `break`: Int =
        tap.`break` + hold.`break` + field.`break` + flick.`break`
}

data class JudgementCount(
    val exactPlus: Int,
    val exactEarly: Int,
    val exactLate: Int,
    val nearEarly: Int,
    val nearLate: Int,
    val `break`: Int,
)

data class WindowTranslation(
    val position: Pair<Int, Int>,
    val size: Pair<Int, Int>,
)

data class RenderRequest(
    val onRendered: suspend (ImageBitmap) -> Job,
    val content: @Composable () -> Unit,
) {
    val finished: CompletableDeferred<Unit> = CompletableDeferred()
}

sealed class UpdateInfo {
    abstract val onCancel: () -> Unit

    data class Update(
        val version: String,
        val time: String,
        val size: String,
        val onUpdate: () -> Unit,
        override val onCancel: () -> Unit,
    ) : UpdateInfo()

    data class Downloading(
        val total: String,
        override val onCancel: () -> Unit,
    ) : UpdateInfo() {
        var downloaded by mutableStateOf("0.00 MiB")
        var percent by mutableStateOf(0f)
    }
}

@Serializable
data class Manifest(
    val default: String,
    val files: List<String>,
)