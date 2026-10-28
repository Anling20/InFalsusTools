package cn.anling.inftool

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.ClipEntry
import io.github.vinceglb.filekit.PlatformFile

expect fun getFocusingWindowPID(): Long
expect fun listGamesPID(): List<Long>
expect fun getCursorPosition(): Pair<Int, Int>?
expect fun getFocusingGameTranslation(): WindowTranslation?
expect fun makeImageClipEntry(bitmap: ImageBitmap): ClipEntry
expect fun getConfigDirectory(): PlatformFile