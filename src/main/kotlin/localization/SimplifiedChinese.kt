package cn.anling.inftool.localization

val LocalizationContainer.Companion.simplifiedChinese
    get() = Language.SimplifiedChinese to container

private val container = LocalizationContainer(
    "sidebar.module.back" to "返回",
    "sidebar.module.potential_window" to "潜力值悬浮窗",
    "sidebar.module.potential_image" to "潜力值图片",
    "sidebar.module.achievements_image" to "谱面达成图片",
    "sidebar.module.play_histories" to "游玩记录",

    "module.potential_window.scale" to "缩放",
    "module.potential_window.stop" to "停止",
    "module.potential_window.start" to "启动",

    "module.potential_image.quality" to "质量",
    "module.potential_image.use_neo_card" to "使用新卡片",
    "module.potential_image.preview" to "预览",
    "module.potential_image.generate_to_file" to "生成到文件",
    "module.potential_image.generate_to_clipboard" to "生成到剪切板",

    "module.achievements_image.quality" to "质量",
    "module.achievements_image.minimal_level_limit" to "限制最低定数",
    "module.achievements_image.maximum_level_limit" to "限制最高定数",
    "module.achievements_image.difficulties_limit" to "限制难度",
    "module.achievements_image.use_neo_card" to "使用新卡片",
    "module.achievements_image.preview" to "预览",
    "module.achievements_image.generate_to_file" to "生成到文件",
    "module.achievements_image.generate_to_clipboard" to "生成到剪切板",

    "setting.username" to "用户名",
    "setting.save_file_path" to "存档文件路径",
    "setting.use_experimental_level" to "使用实验性定数",
    "setting.potential_algorithm" to "潜力值算法",
    "setting.language" to "语言",

    "submodule.potential_window.play_potential" to "游玩潜力值",
    "submodule.achievements_image.potential_algorithm" to "潜力值算法",

    "component.dive_card.potential" to "潜力值",
    "component.dive_card.max_link" to "连击数",
    "component.dive_card.early" to "过早",
    "component.dive_card.late" to "过晚",

    "song_title.kirapico" to "キラピコ繚乱キラピコリン",
    "song_title.kiretsu" to "綺劣",
    "song_title.sunanokioku" to "砂の記憶",
    "song_title.worldender" to "魔王",
    "song_title.rainshower" to "驟雨の狭間",
    "song_title.kuukaku" to "空殻",
    "song_title.monitoring" to "モニタリング",
    "song_title.roki" to "ロキ",
    "song_title.lostonesweeping" to "ロストワンの号哭",
    "song_title.nonbreathoblige" to "ノンブレス・オブリージュ",
    "song_title.kakushicommand" to "幸せになれる隠しコマンドがあるらしい",
)