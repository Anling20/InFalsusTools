package cn.anling.inftool.page

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.anling.infalsustools.generated.resources.Res
import cn.anling.infalsustools.generated.resources.folder_open
import cn.anling.inftool.DiveCardFlat
import cn.anling.inftool.ProcessedDiveRecord
import cn.anling.inftool.component.ClickableTextField
import cn.anling.inftool.getWorkingFolder
import cn.anling.inftool.localization.Language
import cn.anling.inftool.localization.LocalizationContext
import cn.anling.inftool.localization.currentLanguage
import cn.anling.inftool.localization.i18n
import cn.anling.inftool.model.*
import cn.anling.inftool.parser.ImageGenerationType
import io.github.vinceglb.filekit.*
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@Composable
context(model: ModulePotentialWindowModel, _: LocalizationContext)
fun ColumnScope.ModulePotentialWindowContent(
    started: Boolean,
    onSwitchStarted: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier
            .weight(1.0F)
            .fillMaxWidth(),
    ) {
        val state = rememberTextFieldState(model.scale.toString())
        TextField(
            state = state,
            label = { Text(i18n("module.potential_window.scale")) },
            inputTransformation = InputTransformation {
                toString().toFloatOrNull()?.let {
                    when (it > 0) {
                        true -> it
                        else -> null
                    }
                } ?: revertAllChanges()
            },
            lineLimits = TextFieldLineLimits.SingleLine,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = TextFieldDefaults.colors().focusedContainerColor.copy(alpha = 0.6F),
                unfocusedContainerColor = TextFieldDefaults.colors().unfocusedContainerColor.copy(alpha = 0.6F),
            ),
            modifier = Modifier.onFocusChanged { focusState ->
                if (!focusState.isFocused) {
                    model.scale = state.text.toString().toFloat()
                }
            },
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = i18n("module.potential_window.only_display_in_results_scene"),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Checkbox(
                checked = model.onlyDisplayInResultsScene,
                onCheckedChange = { model.onlyDisplayInResultsScene = it },
            )
        }
        ClickableTextField(
            value = model.addressFile.absolutePath(),
            label = { Text(i18n("module.potential_window.address_file_path")) },
            trailingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.folder_open),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = TextFieldDefaults.colors().focusedContainerColor.copy(alpha = 0.6F),
                unfocusedContainerColor = TextFieldDefaults.colors().unfocusedContainerColor.copy(alpha = 0.6F),
            ),
            onClick = {
                val parent = model.addressFile.parent()
                val directory = when {
                    parent?.exists() == true -> parent
                    else -> getWorkingFolder()
                }
                val file = FileKit.openFilePicker(
                    type = FileKitType.File("json"),
                    mode = FileKitMode.Single,
                    directory = directory,
                )
                if (file != null) {
                    model.addressFile = file
                }
            },
        )
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.End,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(160.dp, 64.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6F))
                .clickable(onClick = onSwitchStarted),
        ) {
            AnimatedContent(started) {
                Text(
                    text = when (started) {
                        true -> i18n("module.potential_window.stop")
                        else -> i18n("module.potential_window.start")
                    },
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 24.sp,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
context(model: ModulePotentialImageModel, _: LocalizationContext)
fun ColumnScope.ModulePotentialImageContent(
    onOpenPreview: () -> Unit,
    onGenerate: (ImageGenerationType) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier
            .weight(1.0F)
            .fillMaxWidth(),
    ) {
        val state = rememberTextFieldState(model.quality.toString())
        TextField(
            state = state,
            label = { Text(i18n("module.potential_image.quality")) },
            inputTransformation = InputTransformation {
                toString().toIntOrNull()?.let {
                    if (it !in 1..100) {
                        replace(0, length, it.coerceIn(1, 100).toString())
                    }
                } ?: revertAllChanges()
            },
            lineLimits = TextFieldLineLimits.SingleLine,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = TextFieldDefaults.colors().focusedContainerColor.copy(alpha = 0.6F),
                unfocusedContainerColor = TextFieldDefaults.colors().unfocusedContainerColor.copy(alpha = 0.6F),
            ),
            modifier = Modifier.onFocusChanged { focusState ->
                if (!focusState.isFocused) {
                    model.quality = state.text.toString().toInt()
                }
            },
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = i18n("module.potential_image.use_neo_card"),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Checkbox(
                checked = model.useNeoCard,
                onCheckedChange = { model.useNeoCard = it },
            )
        }
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.End,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(340.dp, 64.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6F))
                .clickable(onClick = onOpenPreview),
        ) {
            Text(
                text = i18n("module.potential_image.preview"),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 24.sp,
                maxLines = 1,
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(340.dp, 64.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6F))
                .clickable { onGenerate(ImageGenerationType.File) },
        ) {
            Text(
                text = i18n("module.potential_image.generate_to_file"),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 24.sp,
                maxLines = 1,
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(340.dp, 64.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6F))
                .clickable { onGenerate(ImageGenerationType.Clipboard) },
        ) {
            Text(
                text = i18n("module.potential_image.generate_to_clipboard"),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 24.sp,
                maxLines = 1,
            )
        }
    }
}

@Composable
context(model: ModuleAchievementsImageModel, _: LocalizationContext)
fun ColumnScope.ModuleAchievementsImageContent(
    onOpenPreview: () -> Unit,
    onGenerate: (ImageGenerationType) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier
            .weight(1.0F)
            .fillMaxWidth(),
    ) {
        val qualityState = rememberTextFieldState(model.quality.toString())
        val minState = rememberTextFieldState(model.minLevelLimit.toString())
        val maxState = rememberTextFieldState(model.maxLevelLimit.toString())
        TextField(
            state = qualityState,
            label = { Text(i18n("module.achievements_image.quality")) },
            inputTransformation = InputTransformation {
                toString().toIntOrNull()?.let {
                    if (it !in 1..100) {
                        replace(0, length, it.coerceIn(1, 100).toString())
                    }
                } ?: revertAllChanges()
            },
            lineLimits = TextFieldLineLimits.SingleLine,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = TextFieldDefaults.colors().focusedContainerColor.copy(alpha = 0.6F),
                unfocusedContainerColor = TextFieldDefaults.colors().unfocusedContainerColor.copy(alpha = 0.6F),
            ),
            modifier = Modifier.onFocusChanged { focusState ->
                if (!focusState.isFocused) {
                    model.quality = qualityState.text.toString().toInt()
                }
            },
        )
        TextField(
            state = minState,
            label = { Text(i18n("module.achievements_image.minimal_level_limit")) },
            inputTransformation = InputTransformation {
                toString().toDoubleOrNull()?.let {
                    if (it !in 1.0..model.maxLevelLimit) {
                        replace(0, length, it.coerceIn(1.0, model.maxLevelLimit).toString())
                    }
                } ?: revertAllChanges()
            },
            lineLimits = TextFieldLineLimits.SingleLine,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = TextFieldDefaults.colors().focusedContainerColor.copy(alpha = 0.6F),
                unfocusedContainerColor = TextFieldDefaults.colors().unfocusedContainerColor.copy(alpha = 0.6F),
            ),
            modifier = Modifier.onFocusChanged { focusState ->
                if (!focusState.isFocused) {
                    model.minLevelLimit = minState.text.toString().toDouble()
                }
            },
        )
        TextField(
            state = maxState,
            label = { Text(i18n("module.achievements_image.maximum_level_limit")) },
            inputTransformation = InputTransformation {
                toString().toDoubleOrNull()?.let {
                    if (it !in model.minLevelLimit..15.0) {
                        replace(0, length, it.coerceIn(model.minLevelLimit, 15.0).toString())
                    }
                } ?: revertAllChanges()
            },
            lineLimits = TextFieldLineLimits.SingleLine,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = TextFieldDefaults.colors().focusedContainerColor.copy(alpha = 0.6F),
                unfocusedContainerColor = TextFieldDefaults.colors().unfocusedContainerColor.copy(alpha = 0.6F),
            ),
            modifier = Modifier.onFocusChanged { focusState ->
                if (!focusState.isFocused) {
                    model.maxLevelLimit = maxState.text.toString().toDouble()
                }
            },
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(64.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = i18n("module.achievements_image.difficulties_limit"),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "MIN",
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Checkbox(
                        checked = model.minDifficultSelect,
                        onCheckedChange = { model.minDifficultSelect = it },
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "EVO",
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Checkbox(
                        checked = model.evoDifficultSelect,
                        onCheckedChange = { model.evoDifficultSelect = it },
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "ULT",
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Checkbox(
                        checked = model.ultDifficultSelect,
                        onCheckedChange = { model.ultDifficultSelect = it },
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "FBD",
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Checkbox(
                        checked = model.fbdDifficultSelect,
                        onCheckedChange = { model.fbdDifficultSelect = it },
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = i18n("module.achievements_image.use_neo_card"),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Checkbox(
                checked = model.useNeoCard,
                onCheckedChange = { model.useNeoCard = it },
            )
        }
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.End,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(340.dp, 64.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6F))
                .clickable(onClick = onOpenPreview),
        ) {
            Text(
                text = i18n("module.achievements_image.preview"),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 24.sp,
                maxLines = 1,
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(340.dp, 64.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6F))
                .clickable { onGenerate(ImageGenerationType.File) },
        ) {
            Text(
                text = i18n("module.achievements_image.generate_to_file"),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 24.sp,
                maxLines = 1,
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(340.dp, 64.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6F))
                .clickable { onGenerate(ImageGenerationType.Clipboard) },
        ) {
            Text(
                text = i18n("module.achievements_image.generate_to_clipboard"),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 24.sp,
                maxLines = 1,
            )
        }
    }
}

@Composable
context(_: LocalizationContext)
fun ModulePlayHistoriesContent(records: List<ProcessedDiveRecord>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(records.reversed()) { record ->
            DiveCardFlat(
                clearType = record.clearType,
                image = record.song.name,
                difficult = record.difficult,
                level = record.level,
                titleKey = "song_title.${record.song.name}",
                score = record.score,
                potential = record.potential,
                grading = record.grading,
                judgement = record.judgement,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
context(model: SettingModel, gameRecordsModel: GameRecordsModel, _: LocalizationContext)
fun SettingContent() {
    val scope = rememberCoroutineScope()
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        val state = rememberTextFieldState(model.username)
        TextField(
            state = state,
            label = { Text(i18n("setting.username")) },
            lineLimits = TextFieldLineLimits.SingleLine,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = TextFieldDefaults.colors().focusedContainerColor.copy(alpha = 0.6F),
                unfocusedContainerColor = TextFieldDefaults.colors().unfocusedContainerColor.copy(alpha = 0.6F),
            ),
            modifier = Modifier.onFocusChanged { focusState ->
                if (!focusState.isFocused) {
                    model.username = state.text.toString()
                }
            },
        )
        ClickableTextField(
            value = model.saveFile.absolutePath(),
            label = { Text(i18n("setting.save_file_path")) },
            trailingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.folder_open),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = TextFieldDefaults.colors().focusedContainerColor.copy(alpha = 0.6F),
                unfocusedContainerColor = TextFieldDefaults.colors().unfocusedContainerColor.copy(alpha = 0.6F),
            ),
            onClick = {
                val file = FileKit.openFilePicker(
                    type = FileKitType.File("sav"),
                    mode = FileKitMode.Single,
                    directory = model.saveFile.parent()!!,
                )
                if (file != null) {
                    model.saveFile = file
                    scope.launch(Dispatchers.IO) {
                        gameRecordsModel.update(
                            saveFile = model.saveFile,
                            algorithm = model.potentialAlgorithm,
                            levelMap = when (model.useExperimentalLevel) {
                                true -> model.experimentalLevels
                                else -> model.vanillaLevels
                            },
                        )
                    }
                }
            },
        )
        ClickableTextField(
            value = model.potentialAlgorithmFile.absolutePath(),
            label = { Text(i18n("setting.potential_algorithm")) },
            trailingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.folder_open),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = TextFieldDefaults.colors().focusedContainerColor.copy(alpha = 0.6F),
                unfocusedContainerColor = TextFieldDefaults.colors().unfocusedContainerColor.copy(alpha = 0.6F),
            ),
            onClick = {
                val file = FileKit.openFilePicker(
                    type = FileKitType.File("potential.kts"),
                    mode = FileKitMode.Single,
                    directory = getWorkingFolder() / "config/potential_algorithms",
                )
                if (file != null) {
                    model.potentialAlgorithmFile = file
                    scope.launch(Dispatchers.IO) {
                        gameRecordsModel.update(
                            saveFile = model.saveFile,
                            algorithm = model.potentialAlgorithm,
                            levelMap = when (model.useExperimentalLevel) {
                                true -> model.experimentalLevels
                                else -> model.vanillaLevels
                            },
                        )
                    }
                }
            },
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = i18n("setting.use_experimental_level"),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Checkbox(
                checked = model.useExperimentalLevel,
                onCheckedChange = {
                    model.useExperimentalLevel = it
                    scope.launch(Dispatchers.IO) {
                        gameRecordsModel.update(
                            saveFile = model.saveFile,
                            algorithm = model.potentialAlgorithm,
                            levelMap = when (model.useExperimentalLevel) {
                                true -> model.experimentalLevels
                                else -> model.vanillaLevels
                            },
                        )
                    }
                },
            )
        }
        run {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                TextField(
                    value = model.language.displayText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(i18n("setting.language")) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = TextFieldDefaults.colors().focusedContainerColor.copy(alpha = 0.6F),
                        unfocusedContainerColor = TextFieldDefaults.colors().unfocusedContainerColor.copy(alpha = 0.6F),
                    ),
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    Language.entries.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.displayText) },
                            onClick = {
                                expanded = false
                                model.language = item
                                currentLanguage = item
                            },
                        )
                    }
                }
            }
        }
    }
}