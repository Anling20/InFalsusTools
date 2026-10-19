import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform") version "2.4.21"
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.21"
    id("org.jetbrains.compose") version "1.12.1"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.21"
}

group = "cn.anling"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
}

kotlin {
    jvmToolchain(25)

    jvm()

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    sourceSets {
        commonMain {
            kotlin.srcDir("src/main/kotlin")
            resources.setSrcDirs(listOf("src/main/resources"))
            generatedKotlin.setSrcDirs(
                listOf(
                    "build/generated/compose/resourceGenerator/kotlin/commonResClass",
                    "build/generated/compose/resourceGenerator/kotlin/commonMainResourceCollectors",
                    "build/generated/compose/resourceGenerator/kotlin/commonMainResourceAccessors",
                )
            )
        }

        jvmMain {
            kotlin.srcDir("src/main@jvm/kotlin")
            resources.setSrcDirs(
                listOf(
                    "src/main@jvm/resources",
                    "build/generated/compose/resourceGenerator/assembledResources/jvmMain",
                )
            )
            generatedKotlin.srcDir("build/generated/compose/resourceGenerator/kotlin/jvmMainResourceCollectors")
        }

        commonMain.dependencies {
            implementation("org.jetbrains.compose.runtime:runtime:1.12.1")
            implementation("org.jetbrains.compose.ui:ui:1.12.1")
            implementation("org.jetbrains.compose.foundation:foundation:1.12.1")
            implementation("org.jetbrains.compose.material3:material3:1.12.0-alpha03")
            implementation("org.jetbrains.compose.components:components-resources:1.12.1")

            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.12.0-RC")

            implementation("com.github.ajalt.clikt:clikt:5.1.0")
            implementation("com.github.ajalt.clikt:clikt-markdown:5.1.0")
            implementation("dev.chrisbanes.haze:haze-blur:2.0.1")
            implementation("dev.chrisbanes.haze:haze-blur-material3:2.0.1")
            implementation("com.russhwolf:multiplatform-settings:1.3.0")
            implementation("io.github.vinceglb:filekit-core:0.16.0")
            implementation("io.github.vinceglb:filekit-dialogs-compose:0.16.0")

            implementation("org.jetbrains.kotlin:kotlin-scripting-common:2.4.21")
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs) {
                exclude("org.jetbrains.compose.material", "material")
            }

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.0")

            implementation("org.jetbrains.kotlin:kotlin-scripting-jvm:2.4.21")
            implementation("org.jetbrains.kotlin:kotlin-scripting-jvm-host:2.4.21")
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            val os = System.getProperty("os.name")
            when {
                os.contains("Windows") -> targetFormats(TargetFormat.Msi, TargetFormat.Exe, TargetFormat.AppImage)
                os.contains("Linux") -> targetFormats(TargetFormat.Deb, TargetFormat.Rpm, TargetFormat.AppImage)
                os.contains("Mac OS") -> targetFormats(TargetFormat.Dmg, TargetFormat.Pkg)
                else -> error("Unsupported OS: $os")
            }
            packageName = "In Falsus Tools"
            packageVersion = "1.0.0"
            jvmArgs("-Dfile.encoding=UTF-8")
            includeAllModules = true

            linux {
                modules("jdk.security.auth")
            }
        }

        buildTypes.release.proguard {
            configurationFiles.from(project.file("src/main@jvm/rules.pro"))
            optimize.set(false)
        }
    }
}

compose.resources {
    customDirectory(
        sourceSetName = "commonMain",
        directoryProvider = provider { layout.projectDirectory.dir("src/main/composeResources") }
    )
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

tasks.register<Copy>("copyExtraResources") {
    description = "Copy extra resources"

    from("config")

    into(layout.buildDirectory.dir("compose/binaries/main/app/In Falsus Tools/config"))
}

tasks.register<Copy>("copyExtraResourcesRelease") {
    description = "Copy extra resources"

    from("config")

    into(layout.buildDirectory.dir("compose/binaries/main-release/app/In Falsus Tools/config"))
}

afterEvaluate {
    tasks.named("packageAppImage") {
        finalizedBy("copyExtraResources")
    }
    tasks.named("packageReleaseAppImage") {
        finalizedBy("copyExtraResourcesRelease")
    }
}