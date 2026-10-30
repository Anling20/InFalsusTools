import dev.nucleusframework.desktop.application.dsl.CompressionLevel
import dev.nucleusframework.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform") version "2.4.21"
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.21"
    id("org.jetbrains.compose") version "1.12.1"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.21"
    id("dev.nucleusframework") version "2.5.18"
}

group = "cn.anling"
version = "1.0.0"

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
            implementation("dev.chrisbanes.haze:haze-blur:2.0.1")
            implementation("dev.chrisbanes.haze:haze-blur-material3:2.0.1")
            implementation("com.russhwolf:multiplatform-settings:1.3.0")
            implementation("io.github.vinceglb:filekit-core:0.16.0")
            implementation("io.github.vinceglb:filekit-dialogs-compose:0.16.0")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.8.0")
            implementation("io.ktor:ktor-client-core:3.6.0")
            implementation("io.ktor:ktor-client-cio:3.6.0")
            implementation("io.ktor:ktor-client-content-negotiation:3.6.0")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.6.0")
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)

            implementation("dev.nucleusframework:nucleus.nucleus-application:2.5.18")
            implementation("dev.nucleusframework:nucleus.decorated-window-tao:2.5.18")
            implementation("dev.nucleusframework:nucleus.taskbar-progress-tao:2.5.18")
            implementation("dev.nucleusframework:nucleus.updater-runtime:2.5.18")
            implementation("dev.nucleusframework:nucleus.native-http:2.5.18")
            implementation("dev.nucleusframework:nucleus.global-hotkey:2.5.18")

            runtimeOnly("ch.qos.logback:logback-classic:1.6.6")
        }
    }
}

nucleus.application {
    mainClass = "cn.anling.inftool.MainKt"

    nativeDistributions {
        targetFormats(
            // macOS
            TargetFormat.Dmg,
//            TargetFormat.Pkg,
            // Windows
            TargetFormat.Exe,
            TargetFormat.Nsis,
            TargetFormat.Msi,
            TargetFormat.Portable,
//            TargetFormat.AppX,
            // Linux
            TargetFormat.Deb,
            TargetFormat.Rpm,
            TargetFormat.AppImage,
            TargetFormat.Pacman,
//            TargetFormat.Snap,
//            TargetFormat.Flatpak,
            // Archives
            TargetFormat.Zip,
            TargetFormat.Tar,
            TargetFormat.SevenZ,
        )

        packageName = "In Falsus Tools"
        packageVersion = System.getenv("RELEASE_VERSION") ?: "0.0.0"

        jvmArgs("-Dfile.encoding=UTF-8")
        cleanupNativeLibs = true
        compressionLevel = CompressionLevel.Ultra

        windows {
            dirChooser = true

            nsis {
                oneClick = false
                allowElevation = true
                allowToChangeInstallationDirectory = true
                multiLanguageInstaller = true
                installerLanguages = listOf("en_US", "zh_CN", "zh_TW")
            }

            msi {
                oneClick = false
            }

            portable {
                compressionLevel = CompressionLevel.Normal
            }
        }
    }

    graalvm {
        isEnabled = true
    }

    buildTypes.release.proguard {
        isEnabled = false
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