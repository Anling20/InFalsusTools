package cn.anling.inftool.model

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import cn.anling.inftool.*
import cn.anling.inftool.page.SettingPage
import cn.anling.inftool.parser.HistoryParser
import cn.anling.inftool.potential_algorithm.PotentialAlgorithm
import cn.anling.inftool.setting.toComposeStated
import cn.anling.inftool.song.ConstantTable
import com.russhwolf.settings.*
import io.github.vinceglb.filekit.PlatformFile
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.channels.Channel

class GameRecordsModel {
    val historyRecords = mutableStateListOf<ProcessedDiveRecord>()
    var potential by mutableDoubleStateOf(0.0)

    suspend fun update(saveFile: PlatformFile, algorithm: PotentialAlgorithm, constantTable: ConstantTable) {
        val histories = HistoryParser.parse(saveFile.readBytesWhenAvailable())
        historyRecords.clear()
        historyRecords.addAll(
            histories
                .map { it.toDiveRecord() }
                .map { record ->
                    record.process(
                        histories = histories.map { it.toDiveRecord() },
                        levelMap = constantTable,
                        algorithm = algorithm,
                    )
                }
        )
        potential = algorithm.calcPotential(historyRecords, constantTable)
    }

    suspend fun update(model: SettingPage.ViewModel) = update(
        saveFile = model.saveFile,
        algorithm = model.potentialAlgorithm,
        constantTable = model.constantTable,
    )
}

class HomeModel(
    settings: Settings,
    initialSaveFile: String,
) {
    val httpClient = HttpClient(CIO)
    val snackbarHostState = SnackbarHostState()

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

    val settingModel = SettingPage.ViewModel(settings, initialSaveFile, httpClient)
}

class ModulePotentialWindowModel(settings: Settings) {
    var scale by settings.float("PotentialWindow.scale", 1.0F).toComposeStated()
    var onlyDisplayInResultsScene by settings.boolean(
        key = "PotentialWindow.onlyDisplayInResultsScene",
        defaultValue = false,
    ).toComposeStated()
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