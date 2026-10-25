package cn.anling.inftool.setting

import cn.anling.inftool.getConfigDirectory
import com.russhwolf.settings.PropertiesSettings
import com.russhwolf.settings.Settings
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.parent
import java.nio.file.Files
import java.util.*

private val propertiesFile = getConfigDirectory() / "config.properties"

actual fun createPlatformSettings(): Settings = PropertiesSettings(
    delegate = Properties().apply {
        if (propertiesFile.exists()) {
            Files.newInputStream(propertiesFile.file.toPath()).use(::load)
        }
    },
    onModify = { properties ->
        if (!propertiesFile.exists()) {
            if (!propertiesFile.parent()!!.exists()) {
                propertiesFile.parent()!!.createDirectories()
            }
            propertiesFile.file.createNewFile()
        }
        properties.store(propertiesFile.file.outputStream(), null)
    }
)