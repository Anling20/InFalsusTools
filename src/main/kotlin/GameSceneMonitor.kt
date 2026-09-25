package cn.anling.inftool

import kotlinx.coroutines.*
import java.lang.foreign.*
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class GameSceneMonitor(
    private val scope: CoroutineScope,
    private val onGameStarted: suspend () -> Unit,
    private val onGameExited: suspend () -> Unit,
    private val pollInterval: Duration = 2.seconds,
) : AutoCloseable {

    companion object {
        /*
         * GameAssembly.dll:
         *
         * GameAssemblyBase + 0x319E300
         *     -> GameScene Il2CppClass*
         *
         * GameScene Il2CppClass + 0xB8
         *     -> static_fields
         *
         * GameScene static_fields + 0x00
         *     -> _xn
         */
        private const val GAME_SCENE_CLASS_RVA = 0x319E300L
        private const val STATIC_FIELDS_OFFSET = 0xB8L
        private const val GAME_SCENE_INSTANCE_OFFSET = 0x00L

        private const val PROCESS_QUERY_INFORMATION = 0x0400
        private const val PROCESS_VM_READ = 0x0010

        private const val PROCESS_ACCESS =
            PROCESS_QUERY_INFORMATION or PROCESS_VM_READ

        private const val TH32CS_SNAPMODULE = 0x00000008
        private const val TH32CS_SNAPMODULE32 = 0x00000010

        /*
         * MODULEENTRY32W 在 x64 下的布局：
         *
         * DWORD   dwSize             0x00
         * DWORD   th32ModuleID      0x04
         * DWORD   th32ProcessID     0x08
         * DWORD   GlblcntUsage      0x0C
         * DWORD   ProccntUsage      0x10
         * BYTE*   modBaseAddr       0x18
         * DWORD   modBaseSize       0x20
         * HMODULE hModule           0x28
         * WCHAR   szModule[256]     0x30
         * WCHAR   szExePath[260]    0x230
         *
         * sizeof = 0x438 = 1080
         */
        private const val MODULE_ENTRY_SIZE = 1080L
        private const val MODULE_BASE_ADDRESS_OFFSET = 0x18L
        private const val MODULE_NAME_OFFSET = 0x30L
        private const val MODULE_NAME_CHARS = 256
    }

    private val running = AtomicBoolean(false)

    private var monitorJob: Job? = null
    private var processMemory: ProcessMemory? = null

    /*
     * 上一次的 GameScene._xn。
     *
     * 我们只关心：
     *
     *     0 -> 非 0
     *
     * 这个瞬间就是 GameScene.OnEnable 写入 _xn 的结果。
     */
    private var previousGameScene = 0L

    fun start() {
        if (!running.compareAndSet(false, true)) {
            return
        }

        monitorJob = scope.launch(Dispatchers.IO) {
            runMonitor()
        }
    }

    private suspend fun runMonitor() {
        while (running.get()) {
            try {
                if (processMemory == null || !processMemory!!.isAlive()) {
                    processMemory?.close()
                    processMemory = findGameProcess()

                    if (processMemory == null) {
                        previousGameScene = 0L
                        delay(pollInterval)
                        continue
                    }
                }

                val currentGameScene = processMemory!!.readGameSceneInstance()
                val previous = previousGameScene

                when {
                    previous == 0L && currentGameScene != 0L -> {
                        println("[GameSceneMonitor] GameScene entered")
                        onGameStarted()
                    }

                    previous != 0L && currentGameScene == 0L -> {
                        println("[GameSceneMonitor] GameScene exited")
                        onGameExited()
                    }
                }

                previousGameScene = currentGameScene

            } catch (e: Exception) {
                println("[GameSceneMonitor] Monitor error: ${e.stackTraceToString()}")

                processMemory?.close()
                processMemory = null
                previousGameScene = 0L
            }

            delay(pollInterval)
        }
    }

    private fun findGameProcess(): ProcessMemory? {
        val process =
            ProcessHandle
                .allProcesses()
                .filter { handle ->
                    val command = handle.info()
                        .command()
                        .orElse("")

                    command.endsWith(
                        "InFalsus.exe",
                        ignoreCase = true,
                    )
                }
                .findFirst()
                .orElse(null)

        if (process == null) {
            println("[GameSceneMonitor] InFalsus.exe not found")
            return null
        }

        val pid = process.pid().toInt()

        println("[GameSceneMonitor] Found InFalsus.exe PID=$pid")

        return try {
            val memory = ProcessMemory.open(pid)

            println("[GameSceneMonitor] OpenProcess OK, PID=$pid")

            val base = memory.findModuleBase("GameAssembly.dll")

            if (base == 0L) {
                memory.close()

                println("[GameSceneMonitor] GameAssembly.dll NOT FOUND")

                null
            } else {
                memory.gameAssemblyBase = base
                memory
            }
        } catch (e: Exception) {
            println("[GameSceneMonitor] Failed to open process: ${e.stackTraceToString()}")
            null
        }
    }

    override fun close() {
        if (!running.compareAndSet(true, false)) {
            return
        }

        monitorJob?.cancel()
        monitorJob = null

        processMemory?.close()
        processMemory = null
        previousGameScene = 0L
    }

    private class ProcessMemory private constructor(
        private val arena: Arena,
        private val processHandle: MemorySegment,
        private val pid: Int,
        private val openProcess: java.lang.invoke.MethodHandle,
        private val readProcessMemory: java.lang.invoke.MethodHandle,
        private val closeHandle: java.lang.invoke.MethodHandle,
        private val createToolhelp32Snapshot: java.lang.invoke.MethodHandle,
        private val module32First: java.lang.invoke.MethodHandle,
        private val module32Next: java.lang.invoke.MethodHandle,
    ) : AutoCloseable {
        var gameAssemblyBase: Long = 0L

        private val psapi =
            SymbolLookup.libraryLookup(
                "psapi.dll",
                symbolArena,
            )

        private val kernel32 =
            SymbolLookup.libraryLookup("kernel32.dll", symbolArena)

        private val getLastError =
            linker.downcallHandle(
                kernel32.find("GetLastError").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT),
            )

        companion object {
            private val linker =
                Linker.nativeLinker()

            private val symbolArena =
                Arena.ofShared()

            private val kernel32 =
                SymbolLookup.libraryLookup(
                    "kernel32.dll",
                    symbolArena,
                )

            private val OPEN_PROCESS =
                FunctionDescriptor.of(
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                )

            private val READ_PROCESS_MEMORY =
                FunctionDescriptor.of(
                    ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS,
                    ValueLayout.ADDRESS,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_LONG,
                    ValueLayout.ADDRESS,
                )

            private val CLOSE_HANDLE =
                FunctionDescriptor.of(
                    ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS,
                )

            private val CREATE_TOOLHELP32_SNAPSHOT =
                FunctionDescriptor.of(
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                )

            private val MODULE32_FIRST =
                FunctionDescriptor.of(
                    ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS,
                    ValueLayout.ADDRESS,
                )

            private val MODULE32_NEXT =
                FunctionDescriptor.of(
                    ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS,
                    ValueLayout.ADDRESS,
                )

            private val ENUM_PROCESS_MODULES_EX =
                FunctionDescriptor.of(
                    ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS, // hProcess
                    ValueLayout.ADDRESS, // lphModule
                    ValueLayout.JAVA_INT, // cb
                    ValueLayout.ADDRESS, // lpcbNeeded
                    ValueLayout.JAVA_INT, // dwFilterFlag
                )

            private val GET_MODULE_BASE_NAME_W =
                FunctionDescriptor.of(
                    ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS, // hProcess
                    ValueLayout.ADDRESS, // hModule
                    ValueLayout.ADDRESS, // lpBaseName
                    ValueLayout.JAVA_INT, // nSize
                )

            fun open(pid: Int): ProcessMemory {
                val arena = Arena.ofShared()

                val openProcess =
                    linker.downcallHandle(
                        kernel32.find("OpenProcess")
                            .orElseThrow(),
                        OPEN_PROCESS,
                    )

                val readProcessMemory =
                    linker.downcallHandle(
                        kernel32.find("ReadProcessMemory")
                            .orElseThrow(),
                        READ_PROCESS_MEMORY,
                    )

                val closeHandle =
                    linker.downcallHandle(
                        kernel32.find("CloseHandle")
                            .orElseThrow(),
                        CLOSE_HANDLE,
                    )

                val createSnapshot =
                    linker.downcallHandle(
                        kernel32.find(
                            "CreateToolhelp32Snapshot"
                        ).orElseThrow(),
                        CREATE_TOOLHELP32_SNAPSHOT,
                    )

                val module32First =
                    linker.downcallHandle(
                        kernel32.find("Module32FirstW")
                            .orElseThrow(),
                        MODULE32_FIRST,
                    )

                val module32Next =
                    linker.downcallHandle(
                        kernel32.find("Module32NextW")
                            .orElseThrow(),
                        MODULE32_NEXT,
                    )

                val handle =
                    openProcess.invoke(
                        PROCESS_ACCESS,
                        0,
                        pid,
                    ) as MemorySegment

                if (
                    handle == MemorySegment.NULL ||
                    handle.address() == 0L
                ) {
                    arena.close()
                    error(
                        "OpenProcess failed: pid=$pid"
                    )
                }

                return ProcessMemory(
                    arena = arena,
                    processHandle = handle,
                    pid = pid,
                    openProcess = openProcess,
                    readProcessMemory = readProcessMemory,
                    closeHandle = closeHandle,
                    createToolhelp32Snapshot = createSnapshot,
                    module32First = module32First,
                    module32Next = module32Next,
                )
            }
        }

        fun isAlive(): Boolean {
            return ProcessHandle
                .of(pid.toLong())
                .map { it.isAlive }
                .orElse(false) ?: false
        }

        fun readPointer(address: Long): Long {
            if (address == 0L) return 0L

            val buffer = arena.allocate(ValueLayout.JAVA_LONG)

            buffer.set(
                ValueLayout.JAVA_LONG,
                0L,
                -1L
            )

            val bytesRead = arena.allocate(ValueLayout.JAVA_LONG)

            val success =
                readProcessMemory.invoke(
                    processHandle,
                    MemorySegment.ofAddress(address),
                    buffer,
                    8L,
                    bytesRead,
                ) as Int

            val actualBytes =
                bytesRead.get(ValueLayout.JAVA_LONG, 0L)

            val value = buffer.get(ValueLayout.JAVA_LONG, 0L)

            if (success == 0) {
                val errorCode = getLastError.invoke() as Int

                error(
                    "ReadProcessMemory failed: " +
                            "address=0x${address.toString(16)}, " +
                            "bytesRead=$actualBytes, " +
                            "error=0x${errorCode.toUInt().toString(16)}"
                )
            }

            return value
        }

        fun readBytes(address: Long, size: Int): ByteArray {
            val buffer = arena.allocate(size.toLong())
            val bytesRead = arena.allocate(ValueLayout.JAVA_LONG)

            val success =
                readProcessMemory.invoke(
                    processHandle,
                    MemorySegment.ofAddress(address),
                    buffer,
                    size.toLong(),
                    bytesRead,
                ) as Int

            val actualBytes =
                bytesRead.get(ValueLayout.JAVA_LONG, 0L)

            if (success == 0 || actualBytes != size.toLong()) {
                error(
                    "ReadProcessMemory failed: " +
                            "address=0x${address.toString(16)}, " +
                            "requested=$size, " +
                            "bytesRead=$actualBytes"
                )
            }

            return ByteArray(size) { index ->
                buffer.get(
                    ValueLayout.JAVA_BYTE,
                    index.toLong()
                )
            }
        }

        fun readGameSceneInstance(): Long {
            val classAddress =
                gameAssemblyBase + GAME_SCENE_CLASS_RVA

            val gameSceneClass =
                readPointer(classAddress)

            if (gameSceneClass == 0L) {
                return 0L
            }

            val staticFields =
                readPointer(
                    gameSceneClass + STATIC_FIELDS_OFFSET
                )

            if (staticFields == 0L) {
                return 0L
            }

            return readPointer(
                staticFields + GAME_SCENE_INSTANCE_OFFSET
            )
        }

        private fun dumpRipLoads(
            address: Long,
            code: ByteArray,
        ) {
            for (i in 0..code.size - 7) {
                if (
                    code[i].toInt() and 0xFF == 0x48 &&
                    code[i + 1].toInt() and 0xFF == 0x8B &&
                    code[i + 2].toInt() and 0xFF == 0x05
                ) {
                    val disp =
                        (code[i + 3].toInt() and 0xFF) or
                                ((code[i + 4].toInt() and 0xFF) shl 8) or
                                ((code[i + 5].toInt() and 0xFF) shl 16) or
                                ((code[i + 6].toInt() and 0xFF) shl 24)

                    val instructionAddress = address + i
                    val target = instructionAddress + 7 + disp.toLong()

                    println(
                        "[GameSceneMonitor] RIP load: " +
                                "instruction=0x${instructionAddress.toString(16)}, " +
                                "target=0x${target.toString(16)}"
                    )
                }
            }
        }

        fun findModuleBase(moduleName: String): Long {
            val enumProcessModulesEx =
                linker.downcallHandle(
                    psapi.find("EnumProcessModulesEx")
                        .orElseThrow(),
                    ENUM_PROCESS_MODULES_EX,
                )

            val getModuleBaseNameW =
                linker.downcallHandle(
                    psapi.find("GetModuleBaseNameW")
                        .orElseThrow(),
                    GET_MODULE_BASE_NAME_W,
                )

            val modules =
                arena.allocate(
                    ValueLayout.ADDRESS,
                    1024,
                )

            val bytesNeeded =
                arena.allocate(ValueLayout.JAVA_INT)

            val ok =
                enumProcessModulesEx.invoke(
                    processHandle,
                    modules,
                    1024 * ValueLayout.ADDRESS.byteSize().toInt(),
                    bytesNeeded,
                    0x03, // LIST_MODULES_ALL
                ) as Int

            if (ok == 0) {
                error("EnumProcessModulesEx failed")
            }

            val count =
                bytesNeeded.get(
                    ValueLayout.JAVA_INT,
                    0,
                ).toInt() /
                        ValueLayout.ADDRESS.byteSize().toInt()

            for (i in 0 until count) {
                val module =
                    modules.get(
                        ValueLayout.ADDRESS,
                        i.toLong() *
                                ValueLayout.ADDRESS.byteSize(),
                    )

                val nameBuffer =
                    arena.allocate(
                        ValueLayout.JAVA_CHAR,
                        260,
                    )

                val length =
                    getModuleBaseNameW.invoke(
                        processHandle,
                        module,
                        nameBuffer,
                        260,
                    ) as Int

                if (length <= 0) {
                    continue
                }

                val bytes = nameBuffer
                    .asSlice(0, length.toLong() * 2)
                    .toArray(ValueLayout.JAVA_BYTE)

                val name =
                    String(bytes, StandardCharsets.UTF_16LE)

                if (
                    name.equals(
                        moduleName,
                        ignoreCase = true,
                    )
                ) {
                    return module.address()
                }
            }

            return 0L
        }

        private fun readUtf16String(
            segment: MemorySegment,
            offset: Long,
            maxChars: Int,
        ): String {
            val bytes =
                segment
                    .asSlice(
                        offset,
                        maxChars * 2L,
                    )
                    .toArray(
                        ValueLayout.JAVA_BYTE
                    )

            val zero =
                bytes.indexOfFirst {
                    it.toInt() == 0 &&
                            // UTF-16LE 的字符结尾需要两个 0。
                            true
                }

            val length =
                if (zero >= 0) {
                    zero
                } else {
                    bytes.size
                }

            /*
             * 保证 UTF-16 字符不会被截成半个。
             */
            val evenLength =
                length and 1.inv()

            return String(
                bytes,
                0,
                evenLength,
                StandardCharsets.UTF_16LE,
            )
        }

        override fun close() {
            if (
                processHandle != MemorySegment.NULL
            ) {
                closeHandle.invoke(processHandle)
            }

            arena.close()
        }
    }
}