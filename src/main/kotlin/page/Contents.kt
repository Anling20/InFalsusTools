package cn.anling.inftool.page

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
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
import cn.anling.inftool.PotentialAlgorithm
import cn.anling.inftool.localization.Language
import cn.anling.inftool.localization.LocalizationContext
import cn.anling.inftool.localization.currentLanguage
import cn.anling.inftool.localization.i18n
import cn.anling.inftool.model.AchievementsImageModel
import cn.anling.inftool.model.Best30ImageModel
import cn.anling.inftool.model.PotentialWindowModel
import cn.anling.inftool.model.SettingModel
import cn.anling.inftool.parser.ImageGenerationType
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.parent
import org.jetbrains.compose.resources.painterResource

@Composable
context(model: PotentialWindowModel, _: LocalizationContext)
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
context(model: Best30ImageModel, _: LocalizationContext)
fun ColumnScope.ModuleBest30ImageContent(
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
            label = { Text(i18n("module.best30_image.quality")) },
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
                text = i18n("module.best30_image.preview"),
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
                text = i18n("module.best30_image.generate_to_file"),
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
                text = i18n("module.best30_image.generate_to_clipboard"),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 24.sp,
                maxLines = 1,
            )
        }
    }
}

@Composable
context(model: AchievementsImageModel, _: LocalizationContext)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
context(model: SettingModel, _: LocalizationContext)
fun SettingContent() {
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
        TextField(
            value = model.saveFile.absolutePath(),
            onValueChange = {},
            readOnly = true,
            label = { Text(i18n("setting.save_file_path")) },
            trailingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.folder_open),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            },
            singleLine = true,
            interactionSource = remember {
                MutableInteractionSource()
            }.also { interactionSource ->
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect {
                        if (it is PressInteraction.Release) {
                            val file = FileKit.openFilePicker(
                                type = FileKitType.File("sav"),
                                mode = FileKitMode.Single,
                                directory = PlatformFile(model.saveFile.parent()!!.absolutePath()),
                            )
                            if (file != null) {
                                model.saveFile = file
                            }
                        }
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = TextFieldDefaults.colors().focusedContainerColor.copy(alpha = 0.6F),
                unfocusedContainerColor = TextFieldDefaults.colors().unfocusedContainerColor.copy(alpha = 0.6F),
            ),
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
                onCheckedChange = { model.useExperimentalLevel = it },
            )
        }
        run {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                TextField(
                    value = i18n(model.potentialAlgorithm.i18nKey),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(i18n("setting.potential_algorithm")) },
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
                    PotentialAlgorithm.entries.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(i18n(item.i18nKey)) },
                            onClick = {
                                expanded = false
                                model.potentialAlgorithm = item
                            },
                        )
                    }
                }
            }
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