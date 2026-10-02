import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout
import java.lang.invoke.MethodHandle

object MouseOverlayDetector {
    private val user32 = SymbolLookup.libraryLookup(
        "user32.dll",
        Arena.global()
    )

    private val getCursorPos: MethodHandle = run {
        val address = user32.find("GetCursorPos").orElseThrow()

        Linker.nativeLinker().downcallHandle(
            address,
            FunctionDescriptor.of(
                ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS
            )
        )
    }

    fun getCursorPosition(): Pair<Int, Int> {
        Arena.ofConfined().use { arena ->
            val point = arena.allocate(8)

            val result = getCursorPos.invoke(point)

            check((result as Int) != 0)

            return Pair(
                point.get(ValueLayout.JAVA_INT, 0),
                point.get(ValueLayout.JAVA_INT, 4),
            )
        }
    }
}