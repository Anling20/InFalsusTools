package cn.anling.inftool.potential_algorithm.script

import cn.anling.inftool.potential_algorithm.PotentialAlgorithm
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ScriptAcceptedLocation
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.acceptedLocations
import kotlin.script.experimental.api.ide
import kotlin.script.experimental.jvm.dependenciesFromClassloader
import kotlin.script.experimental.jvm.jvm

@KotlinScript(
    fileExtension = "potential.kts",
    compilationConfiguration = PotentialAlgorithmScriptConfiguration::class,
)
abstract class PotentialAlgorithmScript

object PotentialAlgorithmScriptConfiguration : ScriptCompilationConfiguration({
    jvm {
        dependenciesFromClassloader(
            classLoader = PotentialAlgorithm::class.java.classLoader,
            wholeClasspath = true,
        )
    }

    ide {
        acceptedLocations(ScriptAcceptedLocation.Everywhere)
    }
}) {
    private fun readResolve(): Any = PotentialAlgorithmScriptConfiguration
}