import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform") version "2.4.20"
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.20"
    id("org.jetbrains.compose") version "1.12.1"
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

    sourceSets {
        commonMain {
            kotlin.setSrcDirs(
                listOf(
                    "src/main/kotlin",
                    "build/generated/compose/resourceGenerator/kotlin/commonResClass",
                    "build/generated/compose/resourceGenerator/kotlin/commonMainResourceCollectors",
                    "build/generated/compose/resourceGenerator/kotlin/commonMainResourceAccessors",
                )
            )
            resources.setSrcDirs(listOf("src/main/resources"))
        }

        jvmMain {
            kotlin.setSrcDirs(
                listOf(
                    "src/main@jvm/kotlin",
                    "build/generated/compose/resourceGenerator/kotlin/jvmMainResourceCollectors",
                )
            )
            resources.setSrcDirs(
                listOf(
                    "src/main@jvm/resources",
                    "build/generated/compose/resourceGenerator/assembledResources/jvmMain",
                )
            )
        }

        commonMain.dependencies {
            implementation("org.jetbrains.compose.runtime:runtime:1.12.1")
            implementation("org.jetbrains.compose.ui:ui:1.12.1")
            implementation("org.jetbrains.compose.foundation:foundation:1.12.1")
            implementation("org.jetbrains.compose.material3:material3:1.12.0-alpha03")
            implementation("org.jetbrains.compose.components:components-resources:1.12.1")

            implementation("com.github.ajalt.clikt:clikt:5.1.0")
            implementation("com.github.ajalt.clikt:clikt-markdown:5.1.0")
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs) {
                exclude("org.jetbrains.compose.material", "material")
            }
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

            linux {
                modules("jdk.security.auth")
            }
        }

        buildTypes.release.proguard {
            configurationFiles.from(project.file("src/main@jvm/rules.pro"))
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