package cn.anling.inftool.model

import androidx.compose.runtime.*
import cn.anling.inftool.*
import cn.anling.inftool.address.AddressContainer
import cn.anling.inftool.address.BuiltinAddressContainer
import cn.anling.inftool.localization.Language
import cn.anling.inftool.parser.HistoryParser
import cn.anling.inftool.potential_algorithm.InFalsusToolsPotentialAlgorithm
import cn.anling.inftool.potential_algorithm.PotentialAlgorithm
import cn.anling.inftool.setting.toComposeStated
import cn.anling.inftool.song.BuiltinConstantTable
import cn.anling.inftool.song.ConstantTable
import cn.anling.inftool.song.ExternalConstantTable
import com.russhwolf.settings.*
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.isRegularFile
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.json.Json

class GameRecordsModel {
    val historyRecords = mutableStateListOf<ProcessedDiveRecord>()
    var potential by mutableDoubleStateOf(0.0)

    suspend fun update(saveFile: PlatformFile, algorithm: PotentialAlgorithm, levelMap: ConstantTable) {
        val histories = HistoryParser.parse(saveFile.readBytesWhenAvailable())
        historyRecords.clear()
        historyRecords.addAll(
            histories
                .map { it.toDiveRecord() }
                .map { record ->
                    record.process(
                        histories = histories.map { it.toDiveRecord() },
                        levelMap = levelMap,
                        algorithm = algorithm,
                    )
                }
        )
        potential = algorithm.calcPotential(historyRecords, levelMap)
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
    var renderRequestChannel = Channel<RenderRequest>()

    val modulePotentialWindowModel = ModulePotentialWindowModel(settings)
    val modulePotentialImageModel = ModulePotentialImageModel(settings)
    val moduleAchievementsImageModel = ModuleAchievementsImageModel(settings)
    val modulePlayHistoriesModel = ModulePlayHistoriesModel()

    val settingModel = SettingModel(settings, initialSaveFile)
}

class ModulePotentialWindowModel(settings: Settings) {
    var scale by settings.float("PotentialWindow.scale", 1.0F).toComposeStated()
    var onlyDisplayInResultsScene by settings.boolean(
        key = "PotentialWindow.onlyDisplayInResultsScene",
        defaultValue = false,
    ).toComposeStated()

    var addressContainerString by settings.string(
        key = "PotentialWindow.addressContainer",
        defaultValue = "builtin:default",
    ).toComposeStated()
    val addressContainer: AddressContainer
        get() {
            val (type, content) = addressContainerString.split(":", limit = 2)
            return runCatching {
                when (type) {
                    "builtin" -> when (content) {
                        "default" -> BuiltinAddressContainer.default
                        else -> error("Unknown builtin constant table: $content")
                    }

                    "file" -> {
                        val file = PlatformFile(content)
                        when (file.exists() && file.isRegularFile()) {
                            true -> Json.decodeFromString(file.readStringSync())
                            else -> error("File not found or is directory: $content")
                        }
                    }

                    else -> error("Unknown constant table type: $addressContainerString")
                }
            }.getOrElse { throwable ->
                println(throwable.message)
                throwable.printStackTrace()
                addressContainerString = "builtin:default"
                BuiltinAddressContainer.default
            }
        }
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

    private var _saveFile by settings.string("Setting.saveFile", initialSaveFile).toComposeStated()
    var saveFile: PlatformFile
        get() = PlatformFile(_saveFile)
        set(value) {
            _saveFile = value.absolutePath()
        }

    var constantTableString by settings.string("Setting.constantTable", "builtin:vanilla").toComposeStated()
    val constantTable: ConstantTable
        get() {
            val (type, content) = constantTableString.split(":", limit = 2)
            return runCatching {
                when (type) {
                    "builtin" -> when (content) {
                        "vanilla" -> BuiltinConstantTable.Vanilla
                        "experimental" -> BuiltinConstantTable.Experimental
                        else -> error("Unknown builtin constant table: $content")
                    }

                    "file" -> {
                        val file = PlatformFile(content)
                        when (file.exists() && file.isRegularFile()) {
                            true -> ExternalConstantTable.File(Json.decodeFromString(file.readStringSync()))
                            else -> error("File not found or is directory: $content")
                        }
                    }

                    else -> error("Unknown constant table type: $constantTableString")
                }
            }.getOrElse { throwable ->
                println(throwable.message)
                throwable.printStackTrace()
                constantTableString = "builtin:vanilla"
                BuiltinConstantTable.Vanilla
            }
        }

    var potentialAlgorithmString by settings.string(
        key = "Setting.potentialAlgorithm",
        defaultValue = "builtin:" + InFalsusToolsPotentialAlgorithm.id,
    ).toComposeStated()
    val potentialAlgorithm: PotentialAlgorithm
        get() {
            val (type, content) = potentialAlgorithmString.split(":", limit = 2)
            return runCatching {
                when (type) {
                    "builtin" -> PotentialAlgorithm.builtin.find { it.id == content }
                        ?: error("Unknown builtin potential algorithm: $content")

                    else -> error("Unknown potential algorithm type: $potentialAlgorithmString")
                }
            }.getOrElse { throwable ->
                println(throwable.message)
                throwable.printStackTrace()
                potentialAlgorithmString = "builtin:" + InFalsusToolsPotentialAlgorithm.id
                InFalsusToolsPotentialAlgorithm
            }
        }

    private var languageIndex by settings.int("Setting.language", 1).toComposeStated()
    var language: Language
        get() = Language.entries[languageIndex]
        set(value) {
            languageIndex = value.ordinal
        }
}