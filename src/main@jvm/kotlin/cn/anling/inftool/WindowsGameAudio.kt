package cn.anling.inftool

import java.lang.foreign.*
import java.lang.invoke.MethodHandle
import java.nio.ByteOrder

object WindowsGameAudio {
    private val linker = Linker.nativeLinker()

    private val address = ValueLayout.ADDRESS
    private val intLayout = ValueLayout.JAVA_INT
    private val shortLayout = ValueLayout.JAVA_SHORT
    private val byteLayout = ValueLayout.JAVA_BYTE

    private val pointerSize = address.byteSize()

    private const val CLSCTX_ALL = 0x17
    private const val E_RENDER = 0
    private const val E_CONSOLE = 0

    private const val S_OK = 0
    private const val S_FALSE = 1
    private const val RPC_E_CHANGED_MODE = 0x80010106.toInt()

    private val oleArena = Arena.ofShared()
    private val ole32 = SymbolLookup.libraryLookup("ole32", oleArena)

    private fun nativeFunction(
        name: String,
        descriptor: FunctionDescriptor
    ): MethodHandle {
        val symbol = ole32.find(name).orElseThrow {
            IllegalStateException("Cannot find ole32!$name")
        }
        return linker.downcallHandle(symbol, descriptor)
    }

    private val coInitializeEx = nativeFunction(
        "CoInitializeEx",
        FunctionDescriptor.of(
            intLayout, address, intLayout
        )
    )

    private val coUninitialize = nativeFunction(
        "CoUninitialize",
        FunctionDescriptor.ofVoid()
    )

    private val coCreateInstance = nativeFunction(
        "CoCreateInstance",
        FunctionDescriptor.of(
            intLayout,
            address, address, intLayout, address, address
        )
    )

    private fun guid(arena: Arena, value: String): MemorySegment {
        val uuid = java.util.UUID.fromString(value)
        val result = arena.allocate(16, 4)

        // Windows GUID 内存布局：Data1、Data2、Data3 使用小端序。
        result.set(
            ValueLayout.JAVA_INT.withOrder(ByteOrder.LITTLE_ENDIAN),
            0, (uuid.mostSignificantBits ushr 32).toInt()
        )
        result.set(
            ValueLayout.JAVA_SHORT.withOrder(ByteOrder.LITTLE_ENDIAN),
            4, (uuid.mostSignificantBits ushr 16).toShort()
        )
        result.set(
            ValueLayout.JAVA_SHORT.withOrder(ByteOrder.LITTLE_ENDIAN),
            6, uuid.mostSignificantBits.toShort()
        )

        val tail = uuid.leastSignificantBits
        for (i in 0 until 8) {
            result.set(
                byteLayout,
                8L + i,
                (tail ushr (56 - i * 8)).toByte()
            )
        }
        return result
    }

    private fun call(
        instance: MemorySegment,
        index: Int,
        descriptor: FunctionDescriptor,
        vararg args: Any
    ): Any? {
        val vtable = instance.reinterpret(pointerSize)
            .get(address, 0)
            .reinterpret((index + 1L) * pointerSize)

        val functionPointer = vtable.get(address, index * pointerSize)
        val handle = linker.downcallHandle(functionPointer, descriptor)

        return handle.invokeWithArguments(listOf(instance, *args))
    }

    private fun check(hr: Int, operation: String) {
        check(hr >= 0) {
            "$operation failed: HRESULT=0x${hr.toUInt().toString(16)}"
        }
    }

    private fun outPointer(arena: Arena): MemorySegment {
        val result = arena.allocate(address)
        result.set(address, 0, MemorySegment.NULL)
        return result
    }

    private fun readPointer(out: MemorySegment): MemorySegment =
        out.get(address, 0)

    private fun release(instance: MemorySegment) {
        if (instance.address() != 0L) {
            call(
                instance,
                2,
                FunctionDescriptor.of(intLayout)
            )
        }
    }

    fun setMuted(processId: Int, muted: Boolean): Int {
        require(processId > 0)

        val initHr = coInitializeEx.invokeWithArguments(
            MemorySegment.NULL, 0
        ) as Int

        val uninitialize = initHr == S_OK || initHr == S_FALSE

        if (initHr < 0 && initHr != RPC_E_CHANGED_MODE) {
            check(initHr, "CoInitializeEx")
        }

        try {
            return Arena.ofConfined().use { arena ->
                setMutedInternal(arena, processId, muted)
            }
        } finally {
            if (uninitialize) {
                coUninitialize.invokeWithArguments()
            }
        }
    }

    private fun setMutedInternal(
        arena: Arena,
        processId: Int,
        muted: Boolean
    ): Int {
        val clsid = guid(
            arena, "BCDE0395-E52F-467C-8E3D-C4579291692E"
        )
        val iidEnumerator = guid(
            arena, "A95664D2-9614-4F35-A746-DE8DB63617E6"
        )
        val iidManager = guid(
            arena, "77AA99A0-1BD6-484F-8BC7-2C654C9A9B6F"
        )
        val iidControl2 = guid(
            arena, "BFB7FF88-7239-4FC9-8FA2-07C950BE9C6D"
        )
        val iidVolume = guid(
            arena, "87CE5498-68D6-44E5-9215-6DA47EF883D8"
        )

        val deviceEnumeratorOut = outPointer(arena)
        val deviceOut = outPointer(arena)
        val managerOut = outPointer(arena)
        val sessionsOut = outPointer(arena)

        var deviceEnumerator = MemorySegment.NULL
        var device = MemorySegment.NULL
        var manager = MemorySegment.NULL
        var sessions = MemorySegment.NULL

        try {
            check(
                coCreateInstance.invokeWithArguments(
                    clsid,
                    MemorySegment.NULL,
                    CLSCTX_ALL,
                    iidEnumerator,
                    deviceEnumeratorOut
                ) as Int,
                "CoCreateInstance"
            )
            deviceEnumerator = readPointer(deviceEnumeratorOut)

            check(
                call(
                    deviceEnumerator,
                    4,
                    FunctionDescriptor.of(
                        intLayout, intLayout, intLayout, address
                    ),
                    E_RENDER, E_CONSOLE, deviceOut
                ) as Int,
                "GetDefaultAudioEndpoint"
            )
            device = readPointer(deviceOut)

            check(
                call(
                    device,
                    3,
                    FunctionDescriptor.of(
                        intLayout, address, intLayout, address, address
                    ),
                    iidManager, CLSCTX_ALL, MemorySegment.NULL, managerOut
                ) as Int,
                "IMMDevice.Activate"
            )
            manager = readPointer(managerOut)

            check(
                call(
                    manager,
                    5,
                    FunctionDescriptor.of(intLayout, address),
                    sessionsOut
                ) as Int,
                "GetSessionEnumerator"
            )
            sessions = readPointer(sessionsOut)

            val countOut = arena.allocate(intLayout)
            check(
                call(
                    sessions,
                    3,
                    FunctionDescriptor.of(intLayout, address),
                    countOut
                ) as Int,
                "GetCount"
            )

            val count = countOut.get(intLayout, 0)
            var changed = 0

            for (index in 0 until count) {
                val sessionOut = outPointer(arena)
                var session = MemorySegment.NULL
                var control2 = MemorySegment.NULL
                var volume = MemorySegment.NULL

                try {
                    check(
                        call(
                            sessions,
                            4,
                            FunctionDescriptor.of(
                                intLayout, intLayout, address
                            ),
                            index, sessionOut
                        ) as Int,
                        "GetSession"
                    )
                    session = readPointer(sessionOut)

                    val controlOut = outPointer(arena)
                    check(
                        call(
                            session,
                            0,
                            FunctionDescriptor.of(
                                intLayout, address, address
                            ),
                            iidControl2, controlOut
                        ) as Int,
                        "QueryInterface(IAudioSessionControl2)"
                    )
                    control2 = readPointer(controlOut)

                    val pidOut = arena.allocate(intLayout)
                    check(
                        call(
                            control2,
                            14,
                            FunctionDescriptor.of(intLayout, address),
                            pidOut
                        ) as Int,
                        "GetProcessId"
                    )

                    if (pidOut.get(intLayout, 0) != processId) {
                        continue
                    }

                    val volumeOut = outPointer(arena)
                    check(
                        call(
                            control2,
                            0,
                            FunctionDescriptor.of(
                                intLayout, address, address
                            ),
                            iidVolume, volumeOut
                        ) as Int,
                        "QueryInterface(ISimpleAudioVolume)"
                    )
                    volume = readPointer(volumeOut)

                    check(
                        call(
                            volume,
                            5,
                            FunctionDescriptor.of(
                                intLayout, intLayout, address
                            ),
                            if (muted) 1 else 0,
                            MemorySegment.NULL
                        ) as Int,
                        "SetMute"
                    )
                    changed++
                } finally {
                    release(volume)
                    release(control2)
                    release(session)
                }
            }

            return changed
        } finally {
            release(sessions)
            release(manager)
            release(device)
            release(deviceEnumerator)
        }
    }
}
