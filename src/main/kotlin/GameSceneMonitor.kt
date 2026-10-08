package cn.anling.inftool

import kotlinx.coroutines.*
import java.lang.foreign.*
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.streams.asSequence
import kotlin.time.Duration

class GameSceneMonitor(
    private val scope: CoroutineScope,
    private val onGameStarted: suspend () -> Unit,
    private val onGameExited: suspend () -> Unit,
    private val onResultsStarted: suspend () -> Unit,
    private val onResultsExited: suspend () -> Unit,
    private val pollInterval: Duration,
) : AutoCloseable {

    companion object {
        private const val GAME_SCENE_CLASS_RVA = 0x31A87B8L
        private const val STATIC_FIELDS_OFFSET = 0xB8L
        private const val GAME_SCENE_INSTANCE_OFFSET = 0x00L

        private const val PROCESS_QUERY_INFORMATION = 0x0400
        private const val PROCESS_VM_READ = 0x0010

        private const val PROCESS_ACCESS = PROCESS_QUERY_INFORMATION or PROCESS_VM_READ

        private const val RESULTS_SCENE_CLASS_RVA = 0x317AD10L
        private const val RESULTS_SCENE_MANAGER_CLASS_RVA = 0x31BC340L

        private const val MANAGER_STATIC_FIELDS_OFFSET = 0xB8L
        private const val MANAGER_INSTANCE_OFFSET = 0x10L

        private const val MANAGER_ARRAY_1_OFFSET = 0x30L
        private const val MANAGER_ARRAY_2_OFFSET = 0x38L

        private const val ARRAY_COUNT_OFFSET = 0x18L
        private const val ARRAY_DATA_OFFSET = 0x20L

        private const val NODE_INSTANCE_OFFSET = 0x18L
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
    private var previousResultsScene = 0L

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
                        delay(pollInterval * 10)
                        continue
                    }
                }

                val currentGameScene = processMemory!!.readGameSceneInstance()

                val currentResultsScene = processMemory!!.readResultsSceneInstance()
                val previousResults = previousResultsScene

                when {
                    previousResults == 0L && currentResultsScene != 0L -> {
                        println("[GameSceneMonitor] ResultsScene entered")
                        onResultsStarted()
                    }

                    previousResults != 0L && currentResultsScene == 0L -> {
                        println("[GameSceneMonitor] ResultsScene exited")
                        onResultsExited()
                    }
                }

                previousResultsScene = currentResultsScene

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
                println("[GameSceneMonitor] Monitor error: ${e::class.simpleName}: ${e.message}")

                processMemory?.close()
                processMemory = null
                previousGameScene = 0L
            }

            delay(pollInterval)
        }
    }

    private fun findGameProcess(): ProcessMemory? {
        val process = ProcessHandle.allProcesses().asSequence().firstNotNullOfOrNull { handle ->
            val command = handle.info().command().orElse(null)

            when (command?.endsWith("infalsus.exe", ignoreCase = true)) {
                true -> handle
                else -> null
            }
        } ?: run {
            println("[GameSceneMonitor] infalsus.exe not found")
            return null
        }

        val pid = process.pid().toInt()

        println("[GameSceneMonitor] Found infalsus.exe PID=$pid")

        return try {
            val memory = ProcessMemory.open(pid)

            println("[GameSceneMonitor] OpenProcess OK, PID=$pid")

            val base = memory.findModuleBase("GameAssembly.dll")

            if (base == 0L) {
                println("[GameSceneMonitor] GameAssembly.dll not ready")
                memory.close()
                null
            } else {
                memory.gameAssemblyBase = base
                println("[GameSceneMonitor] GameAssembly.dll ready: 0x${base.toString(16)}")
                memory
            }
        } catch (e: Exception) {
            println("[GameSceneMonitor] Failed to open process: ${e::class.simpleName}: ${e.message}")
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
        private val readProcessMemory: java.lang.invoke.MethodHandle,
        private val closeHandle: java.lang.invoke.MethodHandle,
    ) : AutoCloseable {
        var gameAssemblyBase: Long = 0L

        private val psapi = SymbolLookup.libraryLookup("psapi.dll", symbolArena)

        companion object {
            private val linker = Linker.nativeLinker()

            private val symbolArena = Arena.ofShared()

            private val kernel32 = SymbolLookup.libraryLookup("kernel32.dll", symbolArena)

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
                    readProcessMemory = readProcessMemory,
                    closeHandle = closeHandle,
                )
            }
        }

        fun isAlive(): Boolean {
            return ProcessHandle
                .of(pid.toLong())
                .map { it.isAlive }
                .orElse(false) ?: false
        }

        private fun readPointerOrNull(address: Long): Long? {
            if (address == 0L) {
                return null
            }

            val buffer = arena.allocate(ValueLayout.JAVA_LONG)

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

            if (success == 0 || actualBytes != 8L) {
                return null
            }

            return buffer.get(
                ValueLayout.JAVA_LONG,
                0L,
            )
        }

        private fun readInt(address: Long): Int {
            if (address == 0L) {
                return 0
            }

            val buffer = arena.allocate(ValueLayout.JAVA_INT)
            val bytesRead = arena.allocate(ValueLayout.JAVA_LONG)

            val success =
                readProcessMemory.invoke(
                    processHandle,
                    MemorySegment.ofAddress(address),
                    buffer,
                    4L,
                    bytesRead,
                ) as Int

            val actualBytes =
                bytesRead.get(ValueLayout.JAVA_LONG, 0L)

            if (success == 0 || actualBytes != 4L) {
                return 0
            }

            return buffer.get(
                ValueLayout.JAVA_INT,
                0L,
            )
        }

        fun readGameSceneInstance(): Long {
            val classAddress =
                gameAssemblyBase + GAME_SCENE_CLASS_RVA

            val gameSceneClass = readPointerOrNull(classAddress)
                ?: return 0L

            // IL2CPP 初始化完成之前，这个位置可能暂时不是有效指针。
            // Windows x64 正常的模块/堆地址通常远高于 4GB。
            if (gameSceneClass < 0x1_0000_0000L) {
                return 0L
            }

            val staticFields =
                readPointerOrNull(
                    gameSceneClass + STATIC_FIELDS_OFFSET
                ) ?: return 0L

            if (staticFields < 0x1_0000_0000L) {
                return 0L
            }

            return readPointerOrNull(
                staticFields + GAME_SCENE_INSTANCE_OFFSET
            ) ?: 0L
        }

        fun readResultsSceneInstance(): Long {
            // ResultsScene Il2CppClass*
            val resultsSceneClass =
                readPointerOrNull(
                    gameAssemblyBase + RESULTS_SCENE_CLASS_RVA
                ) ?: return 0L

            if (resultsSceneClass < 0x1_0000_0000L) {
                return 0L
            }

            // Manager Il2CppClass*
            val managerClass =
                readPointerOrNull(
                    gameAssemblyBase + RESULTS_SCENE_MANAGER_CLASS_RVA
                ) ?: return 0L

            if (managerClass < 0x1_0000_0000L) {
                return 0L
            }

            // Manager static_fields
            val managerStatics =
                readPointerOrNull(
                    managerClass + MANAGER_STATIC_FIELDS_OFFSET
                ) ?: return 0L

            if (managerStatics < 0x1_0000_0000L) {
                return 0L
            }

            // manager instance
            val manager =
                readPointerOrNull(
                    managerStatics + MANAGER_INSTANCE_OFFSET
                ) ?: return 0L

            if (manager < 0x1_0000_0000L) {
                return 0L
            }

            // 两个注册表
            for (arrayOffset in longArrayOf(
                MANAGER_ARRAY_1_OFFSET,
                MANAGER_ARRAY_2_OFFSET,
            )) {
                val array =
                    readPointerOrNull(
                        manager + arrayOffset
                    ) ?: continue

                if (array < 0x1_0000_0000L) {
                    continue
                }

                val count =
                    readInt(
                        array + ARRAY_COUNT_OFFSET
                    )

                if (count !in 1..10000) {
                    continue
                }

                for (i in 0 until count) {
                    val node =
                        readPointerOrNull(
                            array +
                                    ARRAY_DATA_OFFSET +
                                    i * 8L
                        ) ?: continue

                    val instance =
                        readPointerOrNull(
                            node + NODE_INSTANCE_OFFSET
                        ) ?: continue

                    if (instance < 0x1_0000_0000L) {
                        continue
                    }

                    val klass =
                        readPointerOrNull(instance)
                            ?: continue

                    if (klass == resultsSceneClass) {
                        return instance
                    }
                }
            }

            return 0L
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

            val count = bytesNeeded.get(ValueLayout.JAVA_INT, 0) / ValueLayout.ADDRESS.byteSize().toInt()

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