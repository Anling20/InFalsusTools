package cn.anling.inftool.setting

import com.russhwolf.settings.PropertiesSettings
import com.russhwolf.settings.Settings
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

private val path = when {
    System.getenv("APPDATA") != null -> Path.of(
        System.getenv("APPDATA"),
        "In Falsus Tools",
        "config.properties",
    )

    System.getProperty("user.home") != null -> Path.of(
        System.getProperty("user.home"),
        ".config",
        "In Falsus Tools",
        "config.properties",
    )

    else -> Path.of(
        "config",
        "config.properties",
    )
}

actual fun createPlatformSettings(): Settings = PropertiesSettings(
    delegate = Properties().apply {
        if (Files.exists(path)) {
            Files.newInputStream(path).use(::load)
        }
    },
    onModify = { properties ->
        if (Files.notExists(path)) {
            if (Files.notExists(path.parent)) {
                Files.createDirectories(path.parent)
            }
            Files.createFile(path)
        }
        properties.store(Files.newOutputStream(path), null)
    }
)