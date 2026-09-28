import cn.anling.inftool.WindowTranslation
import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemoryLayout
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout

object WindowTranslationUtil {
    private val user32: SymbolLookup =
        SymbolLookup.libraryLookup("user32.dll", Arena.global())

    private val linker = Linker.nativeLinker()

    private val getClientRect = linker.downcallHandle(
        user32.find("GetClientRect").orElseThrow(),
        FunctionDescriptor.of(
            ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        ),
    )

    private val clientToScreen = linker.downcallHandle(
        user32.find("ClientToScreen").orElseThrow(),
        FunctionDescriptor.of(
            ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        ),
    )

    private val rectLayout = MemoryLayout.structLayout(
        ValueLayout.JAVA_INT.withName("left"),
        ValueLayout.JAVA_INT.withName("top"),
        ValueLayout.JAVA_INT.withName("right"),
        ValueLayout.JAVA_INT.withName("bottom"),
    )

    private val rightOffset =
        rectLayout.byteOffset(
            MemoryLayout.PathElement.groupElement("right")
        )

    private val bottomOffset =
        rectLayout.byteOffset(
            MemoryLayout.PathElement.groupElement("bottom")
        )

    private val pointLayout = MemoryLayout.structLayout(
        ValueLayout.JAVA_INT.withName("x"),
        ValueLayout.JAVA_INT.withName("y"),
    )

    private val pointXOffset =
        pointLayout.byteOffset(
            MemoryLayout.PathElement.groupElement("x")
        )

    private val pointYOffset =
        pointLayout.byteOffset(
            MemoryLayout.PathElement.groupElement("y")
        )

    fun getTranslation(): WindowTranslation? {
        Arena.ofConfined().use { arena ->
            if (WindowsForeground.getForegroundProcessId() !in WindowsForeground.findProcessId("infalsus.exe")) {
                return null
            }

            // HWND
            val hwnd = WindowsForeground.getForegroundWindow()

            if (hwnd.address() == 0L) {
                return null
            }

            // Client Area
            val rect = arena.allocate(rectLayout)

            val result = getClientRect.invokeExact(
                hwnd,
                rect,
            ) as Int

            if (result == 0) {
                return null
            }

            val width = rect.get(
                ValueLayout.JAVA_INT,
                rightOffset,
            )

            val height = rect.get(
                ValueLayout.JAVA_INT,
                bottomOffset,
            )

            if (width <= 0 || height <= 0) {
                return null
            }

            // Client (0, 0) -> Screen coordinates
            val point = arena.allocate(pointLayout)

            point.set(
                ValueLayout.JAVA_INT,
                pointXOffset,
                0,
            )

            point.set(
                ValueLayout.JAVA_INT,
                pointYOffset,
                0,
            )

            val converted = clientToScreen.invokeExact(
                hwnd,
                point,
            ) as Int

            if (converted == 0) {
                return null
            }

            val x = point.get(
                ValueLayout.JAVA_INT,
                pointXOffset,
            )

            val y = point.get(
                ValueLayout.JAVA_INT,
                pointYOffset,
            )

            return WindowTranslation(
                position = x to y,
                size = width to height,
            )
        }
    }
}