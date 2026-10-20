package cn.anling.inftool.potential_algorithm.script

import cn.anling.inftool.potential_algorithm.PotentialAlgorithm
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readString
import kotlinx.coroutines.runBlocking
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.valueOrThrow
import kotlin.script.experimental.host.StringScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

object PotentialAlgorithmScriptLoader {
    private val host = BasicJvmScriptingHost()
    private val compilationConfiguration =
        createJvmCompilationConfigurationFromTemplate<PotentialAlgorithmScript>()

    fun load(file: PlatformFile): PotentialAlgorithm? {
        return runCatching {
            val result = host.eval(
                script = StringScriptSource(
                    source = runBlocking { file.readString() },
                    name = file.name,
                ),
                compilationConfiguration = compilationConfiguration,
                evaluationConfiguration = null,
            )

            result.reports
                .filter { it.severity >= ScriptDiagnostic.Severity.ERROR }
                .forEach { diagnostic ->
                    System.err.println(
                        "${file.name}:${diagnostic.location?.start?.line}: " +
                                diagnostic.message
                    )
                    diagnostic.exception?.printStackTrace()
                }

            val evaluationResult = result.valueOrThrow()

            val algorithm = when (val returnValue = evaluationResult.returnValue) {
                is ResultValue.Value -> returnValue.value
                is ResultValue.Unit -> null
                is ResultValue.Error -> throw returnValue.error
                ResultValue.NotEvaluated -> error("Not evaluated")
            }

            algorithm as? PotentialAlgorithm
                ?: error("Script must return an instance of PotentialAlgorithm")
        }.getOrElse { throwable ->
            println(throwable.localizedMessage)
            throwable.printStackTrace()
            null
        }

    }
}