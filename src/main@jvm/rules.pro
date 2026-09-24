-dontobfuscate
-dontoptimize

-keep class androidx.compose.** { *; }
-keep class com.github.ajalt.mordant.terminal.terminalinterface.ffm.** { *; }
-keep class com.github.ajalt.mordant.terminal.terminalinterface.jna.** { *; }
-keep class com.github.ajalt.mordant.terminal.terminalinterface.nativeimage.** { *; }
-keep class com.github.ajalt.mordant.terminal.TerminalInterfaceProvider { *; }

-dontwarn androidx.compose.desktop.DesktopTheme_jvmKt
-dontwarn com.github.ajalt.mordant.terminal.terminalinterface.ffm.**
-dontwarn com.github.ajalt.mordant.terminal.terminalinterface.nativeimage.**
-dontwarn com.oracle.svm.**

-dontnote org.freedesktop.dbus.**
-dontnote com.sun.jna.**
-dontnote com.github.ajalt.mordant.terminal.**