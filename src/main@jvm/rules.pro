-dontobfuscate
-dontoptimize

-keep class com.sun.jna.** { *; }
-keep class * implements com.sun.jna.** { *; }
-keep class androidx.compose.** { *; }
-keep class com.github.ajalt.mordant.terminal.terminalinterface.ffm.** { *; }
-keep class com.github.ajalt.mordant.terminal.terminalinterface.jna.** { *; }
-keep class com.github.ajalt.mordant.terminal.terminalinterface.nativeimage.** { *; }
-keep class com.github.ajalt.mordant.terminal.TerminalInterfaceProvider { *; }
-keep class org.freedesktop.dbus.** { *; }
-keep class io.github.vinceglb.filekit.dialogs.platform.xdg.** { *; }

-keepattributes Signature,InnerClasses,RuntimeVisibleAnnotations

-dontwarn androidx.compose.desktop.DesktopTheme_jvmKt
-dontwarn com.github.ajalt.mordant.terminal.terminalinterface.ffm.**
-dontwarn com.github.ajalt.mordant.terminal.terminalinterface.nativeimage.**
-dontwarn com.oracle.svm.**
-dontwarn java.lang.invoke.MethodHandle

-dontnote org.freedesktop.dbus.**
-dontnote com.sun.jna.**
-dontnote com.github.ajalt.mordant.terminal.**