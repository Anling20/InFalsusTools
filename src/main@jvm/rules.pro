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
-keep class org.jetbrains.kotlin.** { *; }
-keep class org.jetbrains.org.objectweb.asm.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keep class cn.anling.inftool.potential_algorithm.PotentialAlgorithm { *; }

-keepattributes Signature,InnerClasses,RuntimeVisibleAnnotations

-dontwarn androidx.compose.desktop.DesktopTheme_jvmKt
-dontwarn com.github.ajalt.mordant.terminal.terminalinterface.ffm.**
-dontwarn com.github.ajalt.mordant.terminal.terminalinterface.nativeimage.**
-dontwarn com.oracle.svm.**
-dontwarn java.lang.invoke.MethodHandle
-dontwarn org.jetbrains.concurrency.CancellablePromise
-dontwarn org.jetbrains.kotlin.**

-dontnote "META-INF**"
-dontnote "kotlin**"
-dontnote "module-info"
-dontnote org.freedesktop.dbus.**
-dontnote com.sun.jna.**
-dontnote com.github.ajalt.mordant.terminal.**
-dontnote org.jetbrains.kotlin.**
-dontnote org.slf4j.LoggerFactory
-dontnote org.slf4j.helpers.SubstituteLogger
-dontnote io.github.vinceglb.filekit.dialogs.platform.**
-dontnote org.jetbrains.org.objectweb.asm.**
-dontnote cn.anling.inftool.potential_algorithm.PotentialAlgorithm