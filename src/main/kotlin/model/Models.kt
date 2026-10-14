package cn.anling.inftool.model

import androidx.compose.runtime.*
import cn.anling.inftool.*
import cn.anling.inftool.localization.Language
import cn.anling.inftool.parser.SaveFileParser
import cn.anling.inftool.potential_algorithm.PotentialAlgorithm
import cn.anling.inftool.setting.toComposeStated
import com.russhwolf.settings.*
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath

class GameRecordsModel {
    val highScoreRecords = mutableStateListOf<ProcessedDiveRecord>()
    val historyRecords = mutableStateListOf<ProcessedDiveRecord>()
    var potential by mutableDoubleStateOf(0.0)

    suspend fun update(saveFile: PlatformFile, algorithm: PotentialAlgorithm, useExperimentalLevel: Boolean) {
        val parsedData = SaveFileParser.parse(saveFile)
        highScoreRecords.clear()
        highScoreRecords.addAll(
            parsedData.toDiveRecordList().map { record ->
                record.process(
                    highScores = parsedData.highScores.map { it.toDiveRecord() },
                    histories = parsedData.history.map { it.toDiveRecord() },
                    algorithm = algorithm,
                    useExperimentalLevel = useExperimentalLevel,
                )
            }
        )
        historyRecords.clear()
        historyRecords.addAll(
            parsedData.history.map { record ->
                record.toDiveRecord().process(
                    highScores = parsedData.highScores.map { it.toDiveRecord() },
                    histories = parsedData.history.map { it.toDiveRecord() },
                    algorithm = algorithm,
                    useExperimentalLevel = useExperimentalLevel,
                )
            }
        )
        potential = algorithm.calcPotential(highScoreRecords, historyRecords)
    }
}

class HomeModel(
    settings: Settings,
    initialSaveFile: String,
) {
    var destination by mutableStateOf<HomeDestination>(HomeDestination.Home)
    var startedPotentialWindow by mutableStateOf(false)
    var waiting by mutableStateOf(false)
    var displayingB30Preview by mutableStateOf(false)
    var displayingAchievementsPreview by mutableStateOf(false)

    val modulePotentialWindowModel = ModulePotentialWindowModel(settings)
    val modulePotentialImageModel = ModulePotentialImageModel(settings)
    val moduleAchievementsImageModel = ModuleAchievementsImageModel(settings)
    val modulePlayHistoriesModel = ModulePlayHistoriesModel()

    val settingModel = SettingModel(settings, initialSaveFile)
}

class ModulePotentialWindowModel(settings: Settings) {
    var scale by settings.float("PotentialWindow.scale", 1.0F).toComposeStated()
}

class ModulePotentialImageModel(settings: Settings) {
    var quality by settings.int("PotentialImage.quality", 100).toComposeStated()
    var useNeoCard by settings.boolean("PotentialImage.useNeoCard", true).toComposeStated()
}

class ModuleAchievementsImageModel(settings: Settings) {
    var quality by settings.int("AchievementsImage.quality", 100).toComposeStated()
    var minLevelLimit by settings.double("AchievementsImage.minLevelLimit", 1.0).toComposeStated()
    var maxLevelLimit by settings.double("AchievementsImage.maxLevelLimit", 15.0).toComposeStated()
    var minDifficultSelect by settings.boolean("AchievementsImage.minDifficultSelect", true).toComposeStated()
    var evoDifficultSelect by settings.boolean("AchievementsImage.evoDifficultSelect", true).toComposeStated()
    var ultDifficultSelect by settings.boolean("AchievementsImage.ultDifficultSelect", true).toComposeStated()
    var fbdDifficultSelect by settings.boolean("AchievementsImage.fbdDifficultSelect", true).toComposeStated()
    var useNeoCard by settings.boolean("AchievementsImage.useNeoCard", true).toComposeStated()
}

class ModulePlayHistoriesModel

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
            potentialAlgorithmIndex = PotentialAlgorithm.entries.indexOf(value)
        }

    private var languageIndex by settings.int("Setting.language", 1).toComposeStated()
    var language
        get() = Language.entries[languageIndex]
        set(value) {
            languageIndex = value.ordinal
        }
}