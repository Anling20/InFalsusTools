#!/usr/bin/env kotlin

@file:Repository("https://repo.maven.apache.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:4.0.0")
@file:Repository("https://bindings.krzeminski.it")
@file:DependsOn("actions:checkout:v7")
@file:DependsOn("actions:setup-java:v6")
@file:DependsOn("actions:upload-artifact:v7")
@file:DependsOn("actions:setup-node:v7")
@file:DependsOn("actions:download-artifact:v7")
@file:DependsOn("gradle:actions__setup-gradle:v6")
@file:DependsOn("NucleusFramework:Nucleus__.github__actions__setup-nucleus:main")
@file:DependsOn("NucleusFramework:Nucleus__.github__actions__generate-update-yml:main")
@file:DependsOn("NucleusFramework:Nucleus__.github__actions__publish-release:main")

import io.github.typesafegithub.workflows.actions.actions.Checkout
import io.github.typesafegithub.workflows.actions.actions.DownloadArtifact
import io.github.typesafegithub.workflows.actions.actions.UploadArtifact
import io.github.typesafegithub.workflows.actions.nucleusframework.NucleusGithubActionsGenerateUpdateYml_Untyped
import io.github.typesafegithub.workflows.actions.nucleusframework.NucleusGithubActionsPublishRelease_Untyped
import io.github.typesafegithub.workflows.actions.nucleusframework.NucleusGithubActionsSetupNucleus_Untyped
import io.github.typesafegithub.workflows.domain.Mode
import io.github.typesafegithub.workflows.domain.Permission
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.ConsistencyCheckJobConfig

workflow(
	name = "Package",
	on = listOf(
		Push(
			pathsIgnore = listOf("README.md"),
		),
	),
	sourceFile = __FILE__,
	targetFileName = "package.yml",
	consistencyCheckJobConfig = ConsistencyCheckJobConfig.Disabled,
	permissions = mapOf(
		Permission.Contents to Mode.Write,
	),
) {
	val build = job(
		id = "build",
		runsOn = RunnerType.Custom(expr("matrix.os")),
		env = mapOf(
			"GITHUB_TOKEN" to expr { secrets.GITHUB_TOKEN },
		),
		_customArguments = mapOf(
			"strategy" to mapOf(
				"fail-fast" to false,
				"matrix" to mapOf(
					"include" to listOf(
						/*mapOf(
							"os" to "ubunut-latest",
							"arch" to "amd64",
						),
						mapOf(
							"os" to "ubunut-24.04-arm",
							"arch" to "arm64",
						),*/
						mapOf(
							"os" to "windows-latest",
							"arch" to "amd64",
						),
						mapOf(
							"os" to "windows-11-arm",
							"arch" to "arm64",
							"node-version" to "22",
						),
						/*mapOf(
							"os" to "macos-latest",
							"arch" to "arm64",
						),
						mapOf(
							"os" to "macos-15-intel",
							"arch" to "amd64",
						),*/
					),
				),
			),
		),
	) {
		uses(action = Checkout())
		uses(
			action = NucleusGithubActionsSetupNucleus_Untyped(
				javaVersion_Untyped = "25",
				flatpak_Untyped = "true",
				snap_Untyped = "true",
				graalvm_Untyped = "true",
				nodeVersion_Untyped = expr("matrix.node-version || '24'"),
			),
		)
		run(
			command = """
				chmod +x ./gradlew
				./gradlew :generateComposeResClass
				./gradlew :generateResourceAccessorsForCommonMain
				./gradlew :generateExpectResourceCollectorsForCommonMain
				./gradlew :generateActualResourceCollectorsForJvmMain
				./gradlew :assembleJvmMainResources
				./gradlew :packageReleaseDistributionForCurrentOS --stacktrace --no-daemon
			""".trimIndent(),
		)
		uses(
			action = UploadArtifact(
				name = "release-assets-" + expr { runner.os } + "-" + expr("matrix.arch"),
				path = listOf("build/compose/binaries/main/**/*"),
			),
		)
	}

	job(
		id = "publish",
		runsOn = RunnerType.UbuntuLatest,
		needs = listOf(build),
		env = mapOf(
			"VERSION" to expr { github.ref_name },
			"CHANNEL" to "alpha",
			"TAG" to "0.0.1",
			"RELEASE_TYPE" to "prerelease",
		),
	) {
		uses(
			action = DownloadArtifact(
				pattern = "release-assets-*",
				path = "artifacts",
			)
		)
		uses(
			action = NucleusGithubActionsGenerateUpdateYml_Untyped(
				artifactsPath_Untyped = "artifacts",
				version_Untyped = expr { env["VERSION"]!! },
				channel_Untyped = expr { env["CHANNEL"]!! },
			),
		)
		uses(
			action = NucleusGithubActionsPublishRelease_Untyped(
				artifactsPath_Untyped = "artifacts",
				tag_Untyped = expr { env["TAG"]!! },
				releaseType_Untyped = expr { env["RELEASE_TYPE"]!! },
			)
		)
	}
}