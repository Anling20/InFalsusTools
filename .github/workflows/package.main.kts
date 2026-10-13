#!/usr/bin/env kotlin

@file:Repository("https://repo.maven.apache.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:4.0.0")
@file:Repository("https://bindings.krzeminski.it")
@file:DependsOn("actions:checkout:v7")
@file:DependsOn("actions:setup-java:v6")
@file:DependsOn("actions:upload-artifact:v7")
@file:DependsOn("gradle:actions__setup-gradle:v6")

import io.github.typesafegithub.workflows.actions.actions.Checkout
import io.github.typesafegithub.workflows.actions.actions.SetupJava
import io.github.typesafegithub.workflows.actions.actions.UploadArtifact
import io.github.typesafegithub.workflows.actions.gradle.ActionsSetupGradle
import io.github.typesafegithub.workflows.domain.JobOutputs
import io.github.typesafegithub.workflows.domain.Mode
import io.github.typesafegithub.workflows.domain.Permission
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.domain.triggers.Release
import io.github.typesafegithub.workflows.dsl.JobBuilder
import io.github.typesafegithub.workflows.dsl.WorkflowBuilder
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.ConsistencyCheckJobConfig

val content: WorkflowBuilder.() -> Unit = {
	publish(
		runsOn = RunnerType.WindowsLatest,
		system = "windows",
		arch = "amd64",
		publishes = listOf(
			"uberjar" to "UberJarForCurrentOS" to "jars",
			"portable" to "AppImage" to "binaries/main-release/app",
			"installer-msi" to "Msi" to "binaries/main-release/msi",
			"installer-exe" to "Exe" to "binaries/main-release/exe",
		),
	)
	publish(
		runsOn = RunnerType.Custom("windows-11-arm"),
		system = "windows",
		arch = "aarch64",
		publishes = listOf(
			"uberjar" to "UberJarForCurrentOS" to "jars",
			"portable" to "AppImage" to "binaries/main-release/app",
			"installer-msi" to "Msi" to "binaries/main-release/msi",
			"installer-exe" to "Exe" to "binaries/main-release/exe",
		),
	)
	/*publish(
		runsOn = RunnerType.UbuntuLatest,
		system = "linux",
		arch = "amd64",
		publishes = listOf(
			"uberjar" to "UberJarForCurrentOS" to "jars",
			"portable" to "AppImage" to "binaries/main-release/app",
			"installer-deb" to "Deb" to "binaries/main-release/deb",
			"installer-rpm" to "Rpm" to "binaries/main-release/rpm",
		),
	)
	publish(
		runsOn = RunnerType.Custom("ubuntu-22.04-arm"),
		system = "linux",
		arch = "aarch64",
		publishes = listOf(
			"uberjar" to "UberJarForCurrentOS" to "jars",
			"portable" to "AppImage" to "binaries/main-release/app",
			"installer-deb" to "Deb" to "binaries/main-release/deb",
			"installer-rpm" to "Rpm" to "binaries/main-release/rpm",
		),
	)
	publish(
		runsOn = RunnerType.MacosLatest,
		system = "macos",
		arch = "aarch64",
		publishes = listOf(
			"uberjar" to "UberJarForCurrentOS" to "jars",
			"installer-dmg" to "Dmg" to "binaries/main-release/dmg",
		),
	)*/
}

workflow(
	name = "Package snapshot",
	on = listOf(
		Push(
			pathsIgnore = listOf("README.md"),
		),
	),
	sourceFile = __FILE__,
	targetFileName = "package_snapshot.yml",
	consistencyCheckJobConfig = ConsistencyCheckJobConfig.Disabled,
	block = content,
)

workflow(
	name = "Package release",
	on = listOf(
		Release(
			types = listOf(Release.EventType.Created),
		),
	),
	sourceFile = __FILE__,
	targetFileName = "package_release.yml",
	consistencyCheckJobConfig = ConsistencyCheckJobConfig.Disabled,
	block = content,
)

infix fun <T1, T2, T3> Pair<T1, T2>.to(third: T3): Triple<T1, T2, T3> = Triple(this.first, this.second, third)

fun WorkflowBuilder.publish(runsOn: RunnerType, system: String, arch: String, publishes: List<Triple<String, String, String>>) {
	job(
		id = "package-$system-$arch",
		runsOn = runsOn,
		permissions = mapOf(
			Permission.Contents to Mode.Read,
			Permission.Packages to Mode.Write,
		)
	) {
		uses(
			action = Checkout(),
		)
		uses(
			action = SetupJava(
				javaVersion = "25",
				distribution = SetupJava.Distribution.Zulu,
			),
		)
		uses(
			action = ActionsSetupGradle(),
		)
		run(
			command = """
				chmod +x ./gradlew
				./gradlew :generateComposeResClass
				./gradlew :generateResourceAccessorsForCommonMain
				./gradlew :generateExpectResourceCollectorsForCommonMain
				./gradlew :generateActualResourceCollectorsForJvmMain
				./gradlew :assembleJvmMainResources
			""".trimIndent(),
		)
		publishes.forEach { publish ->
			publish(
				name = "$system-${publish.first}-$arch",
				task = publish.second,
				path = publish.third,
			)
		}
	}
}

fun JobBuilder<JobOutputs.EMPTY>.publish(name: String, task: String, path: String) {
	run(
		name = "package $name",
		command = "./gradlew :packageRelease$task",
	)
	uses(
		action = UploadArtifact(
			name = "InFalsusTools-$name",
			path = listOf("build/compose/$path/*"),
		),
	)
}