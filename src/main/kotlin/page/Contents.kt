package cn.anling.inftool.page

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.anling.inftool.DiveCardFlat
import cn.anling.inftool.ProcessedDiveRecord
import cn.anling.inftool.localization.LocalizationContext
import cn.anling.inftool.localization.i18n
import cn.anling.inftool.model.ModuleAchievementsImageModel
import cn.anling.inftool.model.ModulePotentialImageModel
import cn.anling.inftool.model.ModulePotentialWindowModel
import cn.anling.inftool.parser.ImageGenerationType
import cn.anling.inftool.unfocusOnPressEnter

@Composable
context(model: ModulePotentialWindowModel, _: LocalizationContext)
fun ColumnScope.ModulePotentialWindowContent(
    started: Boolean,
    onSwitchStarted: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
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
            lineLimits = TextFieldLineLimits.SingleLine,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = TextFieldDefaults.colors().focusedContainerColor.copy(alpha = 0.6F),
                unfocusedContainerColor = TextFieldDefaults.colors().unfocusedContainerColor.copy(alpha = 0.6F),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .unfocusOnPressEnter(focusManager)
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) {
                        val value = state.text.toString().toFloatOrNull()?.let {
                            when (it > 0) {
                                true -> it
                                else -> null
                            }
                        }
                        when (value) {
                            null -> state.edit { replace(0, length, model.scale.toString()) }
                            else -> model.scale = value
                        }
                    }
                },
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
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
    val focusManager = LocalFocusManager.current
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
            lineLimits = TextFieldLineLimits.SingleLine,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = TextFieldDefaults.colors().focusedContainerColor.copy(alpha = 0.6F),
                unfocusedContainerColor = TextFieldDefaults.colors().unfocusedContainerColor.copy(alpha = 0.6F),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .unfocusOnPressEnter(focusManager)
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) {
                        val value = state.text.toString().toIntOrNull()?.let {
                            when (it !in 1..100) {
                                true -> null
                                else -> it
                            }
                        }
                        when (value) {
                            null -> state.edit { replace(0, length, model.quality.toString()) }
                            else -> model.quality = value
                        }
                    }
                },
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
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
    val focusManager = LocalFocusManager.current
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
            lineLimits = TextFieldLineLimits.SingleLine,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = TextFieldDefaults.colors().focusedContainerColor.copy(alpha = 0.6F),
                unfocusedContainerColor = TextFieldDefaults.colors().unfocusedContainerColor.copy(alpha = 0.6F),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .unfocusOnPressEnter(focusManager)
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) {
                        val value = qualityState.text.toString().toIntOrNull()?.let {
                            when (it !in 1..100) {
                                true -> null
                                else -> it
                            }
                        }
                        when (value) {
                            null -> qualityState.edit { replace(0, length, model.quality.toString()) }
                            else -> model.quality = value
                        }
                    }
                },
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            TextField(
                state = minState,
                label = { Text(i18n("module.achievements_image.minimal_level_limit")) },
                lineLimits = TextFieldLineLimits.SingleLine,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = TextFieldDefaults.colors().focusedContainerColor.copy(alpha = 0.6F),
                    unfocusedContainerColor = TextFieldDefaults.colors().unfocusedContainerColor.copy(alpha = 0.6F),
                ),
                modifier = Modifier
                    .weight(1.0F)
                    .unfocusOnPressEnter(focusManager)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            val value = minState.text.toString().toDoubleOrNull()?.let {
                                when (it !in 0.0..model.maxLevelLimit) {
                                    true -> null
                                    else -> it
                                }
                            }
                            when (value) {
                                null -> minState.edit { replace(0, length, model.minLevelLimit.toString()) }
                                else -> model.minLevelLimit = value
                            }
                        }
                    },
            )
            TextField(
                state = maxState,
                label = { Text(i18n("module.achievements_image.maximum_level_limit")) },
                lineLimits = TextFieldLineLimits.SingleLine,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = TextFieldDefaults.colors().focusedContainerColor.copy(alpha = 0.6F),
                    unfocusedContainerColor = TextFieldDefaults.colors().unfocusedContainerColor.copy(alpha = 0.6F),
                ),
                modifier = Modifier
                    .weight(1.0F)
                    .unfocusOnPressEnter(focusManager)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            val value = maxState.text.toString().toDoubleOrNull()?.let {
                                when (it < model.minLevelLimit) {
                                    true -> null
                                    else -> it
                                }
                            }
                            when (value) {
                                null -> maxState.edit { replace(0, length, model.maxLevelLimit.toString()) }
                                else -> model.maxLevelLimit = value
                            }
                        }
                    },
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
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