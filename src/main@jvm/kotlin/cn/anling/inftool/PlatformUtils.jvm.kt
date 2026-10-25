package cn.anling.inftool

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.div
import java.awt.EventQueue
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.image.BufferedImage

actual fun getFocusingWindowPID(): Long =
    WindowsForeground.getForegroundProcessId()

actual fun listGamesPID(): List<Long> =
    WindowsForeground.listProcessPID("infalsus.exe")

actual fun getCursorPosition(): Pair<Int, Int>? =
    MouseOverlayDetector.getCursorPosition()

actual fun getFocusingGameTranslation(): WindowTranslation? =
    WindowTranslationUtil.getTranslation()

actual fun copyImageToClipboard(bitmap: ImageBitmap) {
    EventQueue.invokeLater {
        Toolkit.getDefaultToolkit().systemClipboard.setContents(ImageTransferable(bitmap.toAwtImage()), null)
    }
}

actual fun getConfigDirectory(): PlatformFile = when {
    System.getenv("APPDATA") != null ->
        PlatformFile(System.getenv("APPDATA")) / "In Falsus Tools"

    System.getProperty("user.home") != null ->
        PlatformFile(System.getProperty("user.home")) / ".config/In Falsus Tools"

    else -> PlatformFile("config")
}

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