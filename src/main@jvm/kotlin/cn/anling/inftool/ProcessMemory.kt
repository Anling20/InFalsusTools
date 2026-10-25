package cn.anling.inftool

import cn.anling.inftool.address.AddressContainer
import java.lang.foreign.*
import java.nio.charset.StandardCharsets

class ProcessMemory private constructor(
    private val addressContainer: AddressContainer,
    private val arena: Arena,
    private val processHandle: MemorySegment,
    private val pid: Long,
    private val readProcessMemory: java.lang.invoke.MethodHandle,
    private val closeHandle: java.lang.invoke.MethodHandle,
) : AutoCloseable {
    var gameAssemblyBase: Long = 0L

    private val psapi = SymbolLookup.libraryLookup("psapi.dll", symbolArena)

    fun isAlive(): Boolean =
        ProcessHandle
            .of(pid)
            .map { it.isAlive }
            .orElse(false) ?: false

    private fun readPointerOrNull(address: Long): Long? {
        if (address == 0L) {
            return null
        }

        val buffer = arena.allocate(ValueLayout.JAVA_LONG)
        val bytesRead = arena.allocate(ValueLayout.JAVA_LONG)

        val success = readProcessMemory.invoke(
            processHandle,
            MemorySegment.ofAddress(address),
            buffer,
            8L,
            bytesRead,
        ) as Int

        val actualBytes = bytesRead.get(ValueLayout.JAVA_LONG, 0L)

        if (success == 0 || actualBytes != 8L) {
            return null
        }

        return buffer.get(ValueLayout.JAVA_LONG, 0L)
    }

    private fun readInt(address: Long): Int {
        if (address == 0L) {
            return 0
        }

        val buffer = arena.allocate(ValueLayout.JAVA_INT)
        val bytesRead = arena.allocate(ValueLayout.JAVA_LONG)

        val success = readProcessMemory.invoke(
            processHandle,
            MemorySegment.ofAddress(address),
            buffer,
            4L,
            bytesRead,
        ) as Int

        val actualBytes = bytesRead.get(ValueLayout.JAVA_LONG, 0L)

        if (success == 0 || actualBytes != 4L) {
            return 0
        }

        return buffer.get(ValueLayout.JAVA_INT, 0L)
    }

    fun readGameSceneInstance(): Long {
        val classAddress = gameAssemblyBase + addressContainer.gameScene.classRVA

        val gameSceneClass = readPointerOrNull(classAddress)
            ?: return 0L

        // IL2CPP 初始化完成之前，这个位置可能暂时不是有效指针。
        // Windows x64 正常的模块/堆地址通常远高于 4GB。
        if (gameSceneClass < 0x1_0000_0000L) {
            return 0L
        }

        val staticFields = readPointerOrNull(gameSceneClass + addressContainer.gameScene.staticFieldsOffset)
            ?: return 0L

        if (staticFields < 0x1_0000_0000L) {
            return 0L
        }

        return readPointerOrNull(staticFields + addressContainer.gameScene.instanceReferenceOffset)
            ?: 0L
    }

    fun readResultsSceneInstance(): Long {
        // ResultsScene Il2CppClass*
        val resultsSceneClass = readPointerOrNull(gameAssemblyBase + addressContainer.resultsScene.classRVA)
            ?: return 0L

        if (resultsSceneClass < 0x1_0000_0000L) {
            return 0L
        }

        // Manager Il2CppClass*
        val managerClass = readPointerOrNull(gameAssemblyBase + addressContainer.resultsSceneManager.classRVA)
            ?: return 0L

        if (managerClass < 0x1_0000_0000L) {
            return 0L
        }

        // Manager static_fields
        val managerStatics = readPointerOrNull(managerClass + addressContainer.resultsSceneManager.staticFieldsOffset)
            ?: return 0L

        if (managerStatics < 0x1_0000_0000L) {
            return 0L
        }

        // manager instance
        val manager = readPointerOrNull(managerStatics + addressContainer.resultsSceneManager.instanceReferenceOffset)
            ?: return 0L

        if (manager < 0x1_0000_0000L) {
            return 0L
        }

        // 两个注册表
        for (arrayOffset in longArrayOf(
            addressContainer.resultsSceneManager.array1Offset,
            addressContainer.resultsSceneManager.array2Offset,
        )) {
            val array = readPointerOrNull(manager + arrayOffset)
                ?: continue

            if (array < 0x1_0000_0000L) {
                continue
            }

            val count = readInt(array + addressContainer.resultsSceneManager.arrayCountOffset)

            if (count !in 1..10000) {
                continue
            }

            for (i in 0 until count) {
                val node = readPointerOrNull(array + addressContainer.resultsSceneManager.arrayDataOffset + i * 8L)
                    ?: continue

                val instance = readPointerOrNull(node + addressContainer.resultsSceneManager.arrayNodeInstanceOffset)
                    ?: continue

                if (instance < 0x1_0000_0000L) {
                    continue
                }

                val klass = readPointerOrNull(instance)
                    ?: continue

                if (klass == resultsSceneClass) {
                    return instance
                }
            }
        }

        return 0L
    }

    fun findModuleBase(moduleName: String): Long {
        val enumProcessModulesEx = linker.downcallHandle(
            psapi.find("EnumProcessModulesEx").orElseThrow(),
            ENUM_PROCESS_MODULES_EX,
        )

        val getModuleBaseNameW = linker.downcallHandle(
            psapi.find("GetModuleBaseNameW").orElseThrow(),
            GET_MODULE_BASE_NAME_W,
        )

        val modules = arena.allocate(ValueLayout.ADDRESS, 1024)
        val bytesNeeded = arena.allocate(ValueLayout.JAVA_INT)

        val ok = enumProcessModulesEx.invoke(
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
            val module = modules.get(
                ValueLayout.ADDRESS,
                i.toLong() * ValueLayout.ADDRESS.byteSize(),
            )

            val nameBuffer = arena.allocate(ValueLayout.JAVA_CHAR, 260)

            val length = getModuleBaseNameW.invoke(
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

            val name = String(bytes, StandardCharsets.UTF_16LE)

            if (name.equals(moduleName, ignoreCase = true)) {
                return module.address()
            }
        }

        return 0L
    }

    override fun close() {
        if (processHandle != MemorySegment.NULL) {
            closeHandle.invoke(processHandle)
        }

        arena.close()
    }

    companion object {
        const val PROCESS_QUERY_INFORMATION = 0x0400
        const val PROCESS_VM_READ = 0x0010
        const val PROCESS_ACCESS = PROCESS_QUERY_INFORMATION or PROCESS_VM_READ

        private val linker = Linker.nativeLinker()

        private val symbolArena = Arena.ofShared()

        private val kernel32 = SymbolLookup.libraryLookup("kernel32.dll", symbolArena)

        private val OPEN_PROCESS = FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.JAVA_INT,
            ValueLayout.JAVA_INT,
            ValueLayout.JAVA_INT,
        )

        private val READ_PROCESS_MEMORY = FunctionDescriptor.of(
            ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
            ValueLayout.JAVA_LONG,
            ValueLayout.ADDRESS,
        )

        private val CLOSE_HANDLE = FunctionDescriptor.of(
            ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS,
        )

        private val ENUM_PROCESS_MODULES_EX = FunctionDescriptor.of(
            ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS, // hProcess
            ValueLayout.ADDRESS, // lphModule
            ValueLayout.JAVA_INT, // cb
            ValueLayout.ADDRESS, // lpcbNeeded
            ValueLayout.JAVA_INT, // dwFilterFlag
        )

        private val GET_MODULE_BASE_NAME_W = FunctionDescriptor.of(
            ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS, // hProcess
            ValueLayout.ADDRESS, // hModule
            ValueLayout.ADDRESS, // lpBaseName
            ValueLayout.JAVA_INT, // nSize
        )

        fun tryOpen(pid: Long, addressContainer: AddressContainer): ProcessMemory? {
            runCatching {
                val arena = Arena.ofShared()

                val openProcess = linker.downcallHandle(
                    kernel32.find("OpenProcess").orElseThrow(),
                    OPEN_PROCESS,
                )

                val readProcessMemory = linker.downcallHandle(
                    kernel32.find("ReadProcessMemory").orElseThrow(),
                    READ_PROCESS_MEMORY,
                )

                val closeHandle = linker.downcallHandle(
                    kernel32.find("CloseHandle").orElseThrow(),
                    CLOSE_HANDLE,
                )

                val handle = openProcess.invoke(PROCESS_ACCESS, 0, pid.toInt()) as MemorySegment

                if (handle == MemorySegment.NULL || handle.address() == 0L) {
                    arena.close()
                    error("OpenProcess failed: pid=$pid")
                }

                val memory = ProcessMemory(
                    addressContainer = addressContainer,
                    arena = arena,
                    processHandle = handle,
                    pid = pid,
                    readProcessMemory = readProcessMemory,
                    closeHandle = closeHandle,
                )

                when (val base = memory.findModuleBase("GameAssembly.dll")) {
                    0L -> {
                        println("[GameSceneMonitor] GameAssembly.dll not ready")
                        memory.close()
                    }

                    else -> {
                        memory.gameAssemblyBase = base
                        return memory
                    }
                }
            }.onFailure { e ->
                println("[GameSceneMonitor] Failed to open process: ${e::class.simpleName}: ${e.message}")
            }

            return null
        }
    }
}