package cn.anling.inftool.command

import cn.anling.inftool.WindowsForeground
import cn.anling.inftool.MouseOverlayDetector
import cn.anling.inftool.WindowTranslation
import cn.anling.inftool.WindowTranslationUtil

actual fun isFocusingGame(): Boolean =
    WindowsForeground.getForegroundProcessId() in WindowsForeground.findProcessId("infalsus.exe")

actual fun getCursorPosition(): Pair<Int, Int>? =
    MouseOverlayDetector.getCursorPosition()

actual fun getFocusingGameTranslation(): WindowTranslation? =
    WindowTranslationUtil.getTranslation()