package cn.anling.inftool

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.image.BufferedImage

actual fun copyImageToClipboard(bitmap: ImageBitmap) {
    Toolkit.getDefaultToolkit().systemClipboard.setContents(ImageTransferable(bitmap.toAwtImage()), null)
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