package cn.anling.inftool

import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemorySegment
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout
import java.lang.invoke.MethodHandle

object WindowsForeground {
    private val linker = Linker.nativeLinker()

    private val user32 =
        SymbolLookup.libraryLookup("user32.dll", Arena.global())

    private val getForegroundWindow: MethodHandle =
        linker.downcallHandle(
            user32.find("GetForegroundWindow").orElseThrow(),
            FunctionDescriptor.of(ValueLayout.ADDRESS)
        )

    private val getWindowThreadProcessId: MethodHandle =
        linker.downcallHandle(
            user32.find("GetWindowThreadProcessId").orElseThrow(),
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
        )

    fun getForegroundWindow(): MemorySegment {
        return getForegroundWindow.invokeExact() as MemorySegment
    }

    fun getForegroundProcessId(): Long {
        val hwnd = getForegroundWindow()

        Arena.ofConfined().use { arena ->
            val pid = arena.allocate(ValueLayout.JAVA_INT)

            getWindowThreadProcessId.invokeExact(hwnd, pid) as Int

            return pid.get(ValueLayout.JAVA_INT, 0).toLong() and 0xFFFF_FFFFL
        }
    }

    fun listProcessPID(processName: String): List<Long> {
        return ProcessHandle.allProcesses()
            .filter { process ->
                process.info()
                    .command()
                    .map {
                        it.substringAfterLast('\\').equals(processName, ignoreCase = true)
                    }
                    .orElse(false) ?: false
            }
            .map { it.pid() }
            .toList()
    }
}