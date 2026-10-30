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
import io.github.typesafegithub.workflows.domain.actions.CustomAction
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.domain.triggers.WorkflowDispatch
import io.github.typesafegithub.workflows.dsl.WorkflowBuilder
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
        cancelInProgress = false,
    ),
    consistencyCheckJobConfig = ConsistencyCheckJobConfig.Disabled,
    permissions = mapOf(
        Permission.Actions to Mode.Read,
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
        run(
            command = $$"""
                set -euo pipefail
                TAG="${GITHUB_REF_NAME}"
                VERSION="${TAG#v}"
                RELEASE_VERSION="${VERSION%%-*}"
                echo "RELEASE_VERSION=$RELEASE_VERSION" >> "$GITHUB_ENV"
			""".trimIndent(),
            name = "Determine version and channel",
            shell = Shell.Bash,
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
                TAG="${GITHUB_REF_NAME}"
                VERSION="${TAG#v}"
                echo "TAG=$TAG" >> "$GITHUB_ENV"
                echo "VERSION=$VERSION" >> "$GITHUB_ENV"

                if [[ "$VERSION" == *"-alpha"* ]]; then
                  echo "CHANNEL=alpha" >> "$GITHUB_ENV"
                elif [[ "$VERSION" == *"-beta"* ]]; then
                  echo "CHANNEL=beta" >> "$GITHUB_ENV"
                else
                  echo "CHANNEL=latest" >> "$GITHUB_ENV"
                fi

                if [[ "$VERSION" == *"-alpha"* ]] || [[ "$VERSION" == *"-beta"* ]]; then
                  echo "RELEASE_TYPE=prerelease" >> "$GITHUB_ENV"
                else
                  echo "RELEASE_TYPE=release" >> "$GITHUB_ENV"
                fi
			""".trimIndent(),
            name = "Determine version and channel",
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

workflow(
    name = "Sync Release",
    on = listOf(
        WorkflowDispatch(
            inputs = mapOf(
                "tag_name" to WorkflowDispatch.Input(
                    description = "GitHub release tag to sync",
                    required = true,
                    type = WorkflowDispatch.Input.Type.String,
                ),
            ),
        ),
    ),
    sourceFile = __FILE__,
    targetFileName = "sync_release.yml",
    concurrency = Concurrency(
        group = "gitcode-release-" + expr("inputs.tag_name"),
        cancelInProgress = false,
    ),
    consistencyCheckJobConfig = ConsistencyCheckJobConfig.Disabled,
    permissions = mapOf(
        Permission.Actions to Mode.Read,
        Permission.Contents to Mode.Read,
    ),
) {
    syncReleaseToGitee()
    syncReleaseToGitCode(
        releaseTag = expr("inputs.tag_name"),
    )
}

fun WorkflowBuilder.syncReleaseToGitee() {
    job(
        id = "sync_release_gitee",
        name = "Sync release(Gitee)",
        runsOn = RunnerType.Ubuntu2204,
    ) {
        uses(
            action = CustomAction(
                actionOwner = "trustedinster",
                actionName = "sync-release-gitee",
                actionVersion = "v1.3",
                inputs = mapOf(
                    "gitee_owner" to "ANMSakura",
                    "gitee_repo" to "InFalsusTools",
                    "gitee_token" to expr { secrets["GITEE_TOKEN"]!! },
                    "github_owner" to "Anling20",
                    "github_repo" to "InFalsusTools",
                    "gitee_upload_retry_times" to "3",
                ),
            ),
        )
    }
}

fun WorkflowBuilder.syncReleaseToGitCode(releaseTag: String) {
    job(
        id = "sync_release_gitcode",
        name = "Sync release(GitCode)",
        runsOn = RunnerType.Ubuntu2204,
        env = mapOf(
            "GH_TOKEN" to expr { secrets.GITHUB_TOKEN },
            "GITCODE_REPOSITORY" to "Anling20/InFalsusTools",
            "GITCODE_TOKEN" to expr { secrets["GITCODE_TOKEN"]!! },
            "RELEASE_TAG" to releaseTag,
        ),
    ) {
        run(
            command = $$"""
                set -euo pipefail

                release_json="$(
                  gh release view "$RELEASE_TAG" \
                    --repo "$GITHUB_REPOSITORY" \
                    --json body,isPrerelease
                )"
                release_body="$(jq -r '.body // ""' <<< "$release_json" | sed "s/'/’/g")"
                release_prerelease="$(jq -r '.isPrerelease' <<< "$release_json")"
                if [[ "$release_prerelease" == "true" ]]; then
                  release_status=pre
                else
                  release_status=latest
                fi

                gitcode_release_json="$(mktemp)"
                gitcode_release_status="$(
                  curl -L -sS -o "$gitcode_release_json" -w "%{http_code}" \
                    -H "PRIVATE-TOKEN: $GITCODE_TOKEN" \
                    -H "Accept: application/json" \
                    "https://api.gitcode.com/api/v5/repos/$GITCODE_REPOSITORY/releases/tags/$RELEASE_TAG"
                )"

                request_json="$(mktemp)"
                case "$gitcode_release_status" in
                  200)
                    request_method=PATCH
                    request_url="https://api.gitcode.com/api/v5/repos/$GITCODE_REPOSITORY/releases/$RELEASE_TAG"
                    request_action=update
                    success_action=updated
                    jq -n \
                      --arg tag_name "$RELEASE_TAG" \
                      --arg name "$RELEASE_TAG" \
                      --arg body "$release_body" \
                      --arg release_status "$release_status" \
                      '{
                        tag_name: $tag_name,
                        name: $name,
                        body: $body,
                        release_status: $release_status
                      }' > "$request_json"
                    ;;
                  404)
                    if ! git ls-remote --exit-code --tags \
                      "https://gitcode.com/$GITCODE_REPOSITORY.git" \
                      "refs/tags/$RELEASE_TAG" > /dev/null; then
                      echo "::error::Tag $RELEASE_TAG has not been mirrored to GitCode yet. Retry this workflow after the tag appears on GitCode."
                      exit 1
                    fi

                    request_method=POST
                    request_url="https://api.gitcode.com/api/v5/repos/$GITCODE_REPOSITORY/releases"
                    request_action=create
                    success_action=created
                    jq -n \
                      --arg tag_name "$RELEASE_TAG" \
                      --arg name "$RELEASE_TAG" \
                      --arg body "$release_body" \
                      --arg release_status "$release_status" \
                      '{
                        tag_name: $tag_name,
                        name: $name,
                        body: $body,
                        release_status: $release_status
                      }' > "$request_json"
                    ;;
                  *)
                    echo "::error::Failed to inspect GitCode release. Response code: $gitcode_release_status"
                    cat "$gitcode_release_json"
                    exit 1
                    ;;
                esac

                gitcode_response_json="$(mktemp)"
                request_status="$(
                  curl -L -sS -o "$gitcode_response_json" -w "%{http_code}" \
                    -X "$request_method" \
                    -H "PRIVATE-TOKEN: $GITCODE_TOKEN" \
                    -H "Content-Type: application/json" \
                    -H "Accept: application/json" \
                    --data-binary "@$request_json" \
                    "$request_url"
                )"
                if [[ "$request_status" != "200" ]]; then
                  echo "::error::Failed to $request_action GitCode release. Response code: $request_status"
                  cat "$gitcode_response_json"
                  exit 1
                fi
                echo "GitCode release $success_action successfully"

                delimiter="release-body-$(openssl rand -hex 16)"
                {
                  echo "tag=$RELEASE_TAG"
                  echo "body<<$delimiter"
                  echo "$release_body"
                  echo "$delimiter"
                  echo "prerelease=$release_prerelease"
                } >> "$GITHUB_OUTPUT"
            """.trimIndent(),
            name = "Create or update GitCode release",
        )
        run(
            command = $$"""
                set -euo pipefail

                mkdir release-assets
                gh release download "$RELEASE_TAG" \
                  --repo "$GITHUB_REPOSITORY" \
                  --dir release-assets

                if ! find release-assets -maxdepth 1 -type f -print -quit | grep -q .; then
                  echo "::error::GitHub release has no downloadable assets"
                  exit 1
                fi

                while IFS= read -r -d '' file; do
                  directory="$(dirname "$file")"
                  filename="$(basename "$file")"
                  safe_filename="$(sed 's/[^A-Za-z0-9._+-]/-/g' <<< "$filename")"
                  if [[ "$filename" != "$safe_filename" ]]; then
                    if [[ -e "$directory/$safe_filename" ]]; then
                      echo "::error::Asset filename collision after normalization: $safe_filename"
                      exit 1
                    fi
                    mv -- "$file" "$directory/$safe_filename"
                  fi
                done < <(find release-assets -maxdepth 1 -type f -print0)

                find release-assets -maxdepth 1 -type f -print | sort
            """.trimIndent(),
            name = "Download GitHub release assets",
        )
        run(
            command = $$"""
                set -euo pipefail

                local_assets="$(
                  find release-assets -maxdepth 1 -type f -printf '%s\t%f\n' \
                    | sort -n -k1,1 \
                    | cut -f2- \
                    | jq -Rsc 'split("\n") | map(select(length > 0))'
                )"

                gitcode_release_json="$(mktemp)"
                gitcode_release_status="$(
                  curl -L -sS -o "$gitcode_release_json" -w "%{http_code}" \
                    -H "PRIVATE-TOKEN: $GITCODE_TOKEN" \
                    -H "Accept: application/json" \
                    "https://api.gitcode.com/api/v5/repos/$GITCODE_REPOSITORY/releases/tags/$RELEASE_TAG"
                )"
                if [[ "$gitcode_release_status" != "200" ]]; then
                  echo "::error::Failed to inspect GitCode release assets. Response code: $gitcode_release_status"
                  cat "$gitcode_release_json"
                  exit 1
                fi

                existing_assets="$(jq -c '[.assets[]?.name]' "$gitcode_release_json")"
                missing_assets="$(
                  jq -cn \
                    --argjson local_assets "$local_assets" \
                    --argjson existing_assets "$existing_assets" \
                    '$local_assets - $existing_assets'
                )"
                jq -rn \
                  --argjson local_assets "$local_assets" \
                  --argjson missing_assets "$missing_assets" \
                  '$local_assets - $missing_assets | .[] | "Skipping existing GitCode asset: \(.)"'

                printf '%s' "$missing_assets" > missing-assets.json
            """.trimIndent(),
            name = "Prepare GitCode release assets",
        )
        run(
            command = $$"""
                set -euo pipefail

                while IFS= read -r file; do
                  [ -n "$file" ] || continue

                  echo "Uploading $file..."

                  encoded_file="$(jq -rn --arg value "$file" '$value | @uri')"

                  upload_info="$(
                    curl -fsS \
                      -H "PRIVATE-TOKEN: $GITCODE_TOKEN" \
                      -H "Accept: application/json" \
                      "https://api.gitcode.com/api/v5/repos/$GITCODE_REPOSITORY/releases/$RELEASE_TAG/upload_url?file_name=$encoded_file"
                  )"

                  upload_url="$(jq -er '.url' <<< "$upload_info")"

                  curl_headers=()
                  while IFS= read -r line; do
                    [ -n "$line" ] && curl_headers+=(-H "$line")
                  done < <(
                    jq -r '.headers // {} | to_entries[] | "\(.key): \(.value)"' <<< "$upload_info"
                  )

                  temp_response="$(mktemp)"
                  http_code="$(
                    curl -sS \
                      -X PUT \
                      -w "%{http_code} %{speed_upload}\n" \
                      -o "$temp_response" \
                      --retry 3 \
                      -T "release-assets/$file" \
                      "${curl_headers[@]}" \
                      "$upload_url"
                  )"

                  if [[ "$http_code" == "200" ]]; then
                    echo "Uploaded $file successfully"
                  else
                    echo "::error::Failed to upload $file. HTTP: $http_code"
                    cat "$temp_response"
                    rm -f "$temp_response"
                    exit 1
                  fi

                  rm -f "$temp_response"
                done < <(jq -r '.[]' missing-assets.json)
            """.trimIndent(),
            name = "Upload missing GitCode release assets",
            shell = Shell.Bash,
        )
    }
}