package cn.anling.inftool.localization

val LocalizationContainer.Companion.simplifiedChinese
    get() = Language.SimplifiedChinese to container

private val container = LocalizationContainer(
    "title" to "In Falsus Tools",
    "sidebar.module.back" to "返回",
    "sidebar.module.potential_window" to "潜力值悬浮窗",
    "sidebar.module.best30_image" to "Best30 图片",
    "sidebar.module.achievements_image" to "Achievements 图片",
    "sidebar.module.play_histories" to "游玩记录",
    "module.potential_window.scale" to "缩放",
    "module.potential_window.stop" to "停止",
    "module.potential_window.start" to "启动",
    "module.best30_image.quality" to "质量",
    "module.best30_image.preview" to "预览",
    "module.best30_image.generate_to_file" to "生成到文件",
    "module.best30_image.generate_to_clipboard" to "生成到剪切板",
    "module.achievements_image.quality" to "质量",
    "module.achievements_image.minimal_level_limit" to "限制最低定数",
    "module.achievements_image.maximum_level_limit" to "限制最高定数",
    "module.achievements_image.difficulties_limit" to "限制难度",
    "module.achievements_image.preview" to "预览",
    "module.achievements_image.generate_to_file" to "生成到文件",
    "module.achievements_image.generate_to_clipboard" to "生成到剪切板",
    "setting.username" to "用户名",
    "setting.save_file_path" to "存档文件路径",
    "setting.use_experimental_level" to "使用实验性定数",
    "setting.potential_algorithm" to "潜力值算法",
    "setting.language" to "语言",
    "potential_algorithm.in_falsus_tools" to "In Falsus Tools",
    "submodule.potential_window.play_potential" to "游玩潜力值",
)