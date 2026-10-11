package cn.anling.inftool.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cn.anling.inftool.HomeDestination
import cn.anling.inftool.PotentialAlgorithm
import cn.anling.inftool.localization.Language
import cn.anling.inftool.setting.toComposeStated
import com.russhwolf.settings.*
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath

class HomeModel(
    settings: Settings,
    initialSaveFile: String,
) {
    var destination by mutableStateOf<HomeDestination>(HomeDestination.Home)
    var startedPotentialWindow by mutableStateOf(false)
    var waiting by mutableStateOf(false)
    var displayingB30Preview by mutableStateOf(false)
    var displayingAchievementsPreview by mutableStateOf(false)

    val potentialWindowModel: PotentialWindowModel = PotentialWindowModel(settings)
    val best30ImageModel: Best30ImageModel = Best30ImageModel(settings)
    val achievementsImageModel: AchievementsImageModel = AchievementsImageModel(settings)
    val settingModel: SettingModel = SettingModel(settings, initialSaveFile)
}

class PotentialWindowModel(settings: Settings) {
    var scale by settings.float("PotentialWindow.scale", 1.0F).toComposeStated()
}

class Best30ImageModel(settings: Settings) {
    var quality by settings.int("Best30Image.quality", 100).toComposeStated()
}

class AchievementsImageModel(settings: Settings) {
    var quality by settings.int("AchievementsImage.quality", 100).toComposeStated()
    var minLevelLimit by settings.double("AchievementsImage.minLevelLimit", 1.0).toComposeStated()
    var maxLevelLimit by settings.double("AchievementsImage.maxLevelLimit", 15.0).toComposeStated()
    var minDifficultSelect by settings.boolean("AchievementsImage.minDifficultSelect", true).toComposeStated()
    var evoDifficultSelect by settings.boolean("AchievementsImage.evoDifficultSelect", true).toComposeStated()
    var ultDifficultSelect by settings.boolean("AchievementsImage.ultDifficultSelect", true).toComposeStated()
    var fbdDifficultSelect by settings.boolean("AchievementsImage.fbdDifficultSelect", true).toComposeStated()
}

class SettingModel(
    settings: Settings,
    initialSaveFile: String,
) {
    var username by settings.string("Setting.username", "Player").toComposeStated()
    private var saveFilePath by settings.string("Setting.saveFile", initialSaveFile).toComposeStated()
    var saveFile
        get() = PlatformFile(saveFilePath)
        set(value) {
            saveFilePath = value.absolutePath()
        }

    var useExperimentalLevel by settings.boolean("Setting.useExperimentalLevel", false).toComposeStated()
    private var potentialAlgorithmIndex by settings.int("Setting.potentialAlgorithm", 0).toComposeStated()
    var potentialAlgorithm
        get() = PotentialAlgorithm.entries[potentialAlgorithmIndex]
        set(value) {
            potentialAlgorithmIndex = value.ordinal
        }

    private var languageIndex by settings.int("Setting.language", 1).toComposeStated()
    var language
        get() = Language.entries[languageIndex]
        set(value) {
            languageIndex = value.ordinal
        }
}