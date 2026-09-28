package cn.anling.inftool.command

import WindowsForeground
import cn.anling.inftool.WindowTranslation

actual fun isFocusingGame(): Boolean =
    WindowsForeground.getForegroundProcessId() in WindowsForeground.findProcessId("infalsus.exe")

actual fun getCursorPosition(): Pair<Int, Int> =
    MouseOverlayDetector.getCursorPosition()

actual fun getFocusingGameTranslation(): WindowTranslation? =
    WindowTranslationUtil.getTranslation()