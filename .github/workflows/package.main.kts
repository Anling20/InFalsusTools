#!/usr/bin/env kotlin

@file:Repository("https://repo.maven.apache.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:4.0.0")
@file:Repository("https://bindings.krzeminski.it")
@file:DependsOn("actions:checkout:v7")
@file:DependsOn("actions:setup-java:v6")
@file:DependsOn("actions:upload-artifact:v7")
@file:DependsOn("actions:setup-node:v7")
@file:DependsOn("actions:download-artifact:v8")
@file:DependsOn("gradle:actions__setup-gradle:v6")
@file:DependsOn("NucleusFramework:Nucleus__.github__actions__validate-release-ref:main")
@file:DependsOn("NucleusFramework:Nucleus__.github__actions__setup-nucleus:main")
@file:DependsOn("NucleusFramework:Nucleus__.github__actions__generate-update-yml:main")
@file:DependsOn("NucleusFramework:Nucleus__.github__actions__publish-release:main")

import io.github.typesafegithub.workflows.actions.actions.Checkout
import io.github.typesafegithub.workflows.actions.actions.DownloadArtifact
import io.github.typesafegithub.workflows.actions.actions.UploadArtifact
import io.github.typesafegithub.workflows.actions.nucleusframework.NucleusGithubActionsGenerateUpdateYml_Untyped
import io.github.typesafegithub.workflows.actions.nucleusframework.NucleusGithubActionsPublishRelease_Untyped
import io.github.typesafegithub.workflows.actions.nucleusframework.NucleusGithubActionsSetupNucleus_Untyped
import io.github.typesafegithub.workflows.actions.nucleusframework.NucleusGithubActionsValidateReleaseRef_Untyped
import io.github.typesafegithub.workflows.domain.*
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.ConsistencyCheckJobConfig

workflow(
    name = "Package",
    on = listOf(
        Push(
            tags = listOf("v*"),
            pathsIgnore = listOf("README.md"),
        ),
    ),
    sourceFile = __FILE__,
    targetFileName = "package.yml",
    concurrency = Concurrency(
        group = "release-" + expr { github.ref },
        cancelInProgress = false
    ),
    consistencyCheckJobConfig = ConsistencyCheckJobConfig.Disabled,
    permissions = mapOf(
        Permission.Contents to Mode.Write,
    ),
) {
    val validateReleaseRef = job(
        id = "validate-release-ref",
        runsOn = RunnerType.Ubuntu2204,
    ) {
        uses(
            action = Checkout(
                fetchDepth = Checkout.FetchDepth.Value(0),
            ),
            name = "Checkout",
        )
        uses(
            action = NucleusGithubActionsValidateReleaseRef_Untyped(),
            name = "Validate release ref",
        )
    }

    val build = job(
        id = "build",
        runsOn = RunnerType.Custom(expr("matrix.os")),
        needs = listOf(validateReleaseRef),
        _customArguments = mapOf(
            "strategy" to mapOf(
                "fail-fast" to false,
                "matrix" to mapOf(
                    "include" to listOf(
                        /*mapOf(
                            "os" to "ubunut-22.04",
                            "arch" to "amd64",
                        ),
                        mapOf(
                            "os" to "ubunut-22.04-arm",
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
        uses(
            action = Checkout(
                fetchDepth = Checkout.FetchDepth.Value(0),
            ),
            name = "Checkout",
        )
        uses(
            action = NucleusGithubActionsSetupNucleus_Untyped(
                javaVersion_Untyped = "25",
                flatpak_Untyped = "true",
                snap_Untyped = "true",
                graalvm_Untyped = "true",
                nodeVersion_Untyped = expr("matrix.node-version || '24'"),
            ),
            name = "Setup nucleus",
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
            name = "Build",
        )
        uses(
            action = UploadArtifact(
                name = "release-assets-" + expr { runner.os } + "-" + expr("matrix.arch"),
                path = listOf(
                    "!build/compose/binaries/**/app/**",
                    "!build/compose/binaries/**/runtime/**",
                    "build/compose/binaries/**/*.deb",
                    "build/compose/binaries/**/*.rpm",
                    "build/compose/binaries/**/*.asc",
                    "build/compose/binaries/**/*.pacman",
                    "build/compose/binaries/**/*.snap",
                    "build/compose/binaries/**/*.flatpak",
                    "build/compose/binaries/**/*.AppImage",
                    "build/compose/binaries/**/*.appimage",
                    "build/compose/binaries/**/*.msi",
                    "build/compose/binaries/**/*.exe",
                    "build/compose/binaries/**/*.appx",
                    "build/compose/binaries/**/*.7z",
                    "build/compose/binaries/**/*.pkg",
                    "build/compose/binaries/**/*.dmg",
                    "build/compose/binaries/**/*.zip",
                    "build/compose/binaries/**/*.tar",
                    "build/compose/binaries/**/*.tar.gz",
                    "build/compose/binaries/**/*.blockmap",
                    "build/compose/binaries/**/signing-metadata.json",
                    "build/compose/binaries/**/packaging-metadata.json",
                    "build/compose/binaries/sandboxed-*.zip",
                ),
                ifNoFilesFound = UploadArtifact.BehaviorIfNoFilesFound.Error,
            ),
            name = "Upload artifacts",
        )
    }

    job(
        id = "publish",
        runsOn = RunnerType.Ubuntu2204,
        needs = listOf(build),
        env = mapOf(
            "GH_TOKEN" to expr { secrets.GITHUB_TOKEN },
            "GITHUB_TOKEN" to expr { secrets.GITHUB_TOKEN },
            "CHANNEL" to "alpha",
            "TAG" to "0.0.1",
            "RELEASE_TYPE" to "prerelease",
        ),
    ) {
        uses(
            action = Checkout(
                fetchDepth = Checkout.FetchDepth.Value(0),
            ),
            name = "Checkout",
        )
        uses(
            action = DownloadArtifact(
                path = "artifacts",
                pattern = "release-assets-*",
            ),
            name = "Download artifacts",
        )
        run(
            command = $$"""
                set -euo pipefail
                VERSION="${TAG#v}"
                echo "VERSION=$VERSION" >> "$GITHUB_ENV"
			""".trimIndent(),
            name = "Determine version",
            shell = Shell.Bash,
        )
        run(
            command = "find artifacts -type f | head -100",
            name = "List downloaded artifacts",
            shell = Shell.Bash,
        )
        uses(
            action = NucleusGithubActionsGenerateUpdateYml_Untyped(
                artifactsPath_Untyped = "artifacts",
                version_Untyped = expr { env["VERSION"]!! },
                channel_Untyped = expr { env["CHANNEL"]!! },
            ),
            name = "Generate update YML files",
        )
        run(
            command = $$"""
                echo "=== Generated update YML files ==="
                for f in artifacts/update-yml/*.yml; do
                  echo "--- $(basename "$f") ---"
                  cat "$f"
                  echo
                done
            """.trimIndent(),
            name = "Show generated YML files",
            shell = Shell.Bash,
        )
        uses(
            action = NucleusGithubActionsPublishRelease_Untyped(
                artifactsPath_Untyped = "artifacts",
                tag_Untyped = expr { env["TAG"]!! },
                releaseType_Untyped = expr { env["RELEASE_TYPE"]!! },
            ),
            name = "Publish release",
        )
    }
}