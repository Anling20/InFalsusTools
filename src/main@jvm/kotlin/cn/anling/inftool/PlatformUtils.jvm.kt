package cn.anling.inftool

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import io.github.vinceglb.filekit.PlatformFile
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.image.BufferedImage
import java.io.File

actual fun getFocusingWindowPID(): Long =
    WindowsForeground.getForegroundProcessId()

actual fun listGamesPID(): List<Long> =
    WindowsForeground.listProcessPID("infalsus.exe")

actual fun getCursorPosition(): Pair<Int, Int>? =
    MouseOverlayDetector.getCursorPosition()

actual fun getFocusingGameTranslation(): WindowTranslation? =
    WindowTranslationUtil.getTranslation()

actual fun copyImageToClipboard(bitmap: ImageBitmap) {
    Toolkit.getDefaultToolkit().systemClipboard.setContents(ImageTransferable(bitmap.toAwtImage()), null)
}

actual fun getWorkingFolder(): PlatformFile =
    PlatformFile(File("").absolutePath)

class ImageTransferable(private val image: BufferedImage) : Transferable {
    override fun getTransferDataFlavors() = arrayOf(DataFlavor.imageFlavor)

    override fun isDataFlavorSupported(flavor: DataFlavor) = flavor == DataFlavor.imageFlavor

    override fun getTransferData(flavor: DataFlavor): Any {
        if (!isDataFlavorSupported(flavor)) {
            throw UnsupportedOperationException()
        }

        return image
    }
}