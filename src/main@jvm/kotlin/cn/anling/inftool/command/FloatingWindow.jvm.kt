package cn.anling.inftool.command

import WindowsForeground

actual fun isFocusingGame(): Boolean =
    WindowsForeground.getForegroundProcessId() in WindowsForeground.findProcessId("infalsus.exe")

actual fun getCursorPosition(): Pair<Int, Int> =
    MouseOverlayDetector.getCursorPosition()