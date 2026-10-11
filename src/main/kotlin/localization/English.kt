package cn.anling.inftool.localization

val LocalizationContainer.Companion.english
    get() = Language.English to container

private val container = LocalizationContainer(
    "title" to "In Falsus Tools",
    "sidebar.module.back" to "Back",
    "sidebar.module.potential_window" to "Potential Window",
    "sidebar.module.best30_image" to "Best30 Image",
    "sidebar.module.achievements_image" to "Achievements Image",
    "sidebar.module.play_histories" to "Play Histories",
    "module.potential_window.scale" to "Scale",
    "module.potential_window.stop" to "Stop",
    "module.potential_window.start" to "Start",
    "module.best30_image.quality" to "Quality",
    "module.best30_image.preview" to "Preview",
    "module.best30_image.generate_to_file" to "Generate to file",
    "module.best30_image.generate_to_clipboard" to "Generate to clipboard",
    "module.achievements_image.quality" to "Quality",
    "module.achievements_image.minimal_level_limit" to "Minimal level limit",
    "module.achievements_image.maximum_level_limit" to "Maximum level limit",
    "module.achievements_image.difficulties_limit" to "Difficulties limit",
    "module.achievements_image.preview" to "Preview",
    "module.achievements_image.generate_to_file" to "Generate to file",
    "module.achievements_image.generate_to_clipboard" to "Generate to clipboard",
    "setting.username" to "Username",
    "setting.save_file_path" to "Save file path",
    "setting.use_experimental_level" to "Use experimental level",
    "setting.potential_algorithm" to "Potential algorithm",
    "setting.language" to "Language",
    "potential_algorithm.in_falsus_tools" to "In Falsus Tools",
    "submodule.potential_window.play_potential" to "Play potential",
)