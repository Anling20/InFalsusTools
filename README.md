# 介绍

`In Falsus Tools` 是一个涵盖潜力值悬浮窗, B30/AHV 图片生成, 游玩记录且含有社区定数的非插件式辅助工具

# 快速开始

从 [Actions](https://github.com/Anling20/InFalsusTools/actions) 下载最新自动构建包(如果你不知道 UberJar 是什么, 请不要下它), 或是从 [QQ群](https://qm.qq.com/q/ndmzU21OyA) 下载手动构建包

如果下载非安装版, 解压到任意位置后直接双击主程序运行即可, 无需对游戏进行任何补丁操作

# 社区定数

由于游戏内不提供官方细分定数, 通过采集社区数据取平均值的方式设定一套定数<br>
若要在 `In Falsus Tools` 内使用社区定数, 请前往设置页面开启 `使用实验性定数`<br>
注意: 启用实验性定数会一同修改潜力值计算, 开关实验性定数的潜力值间会有较大差距

若有需要/提供数据, 请前往 [在线定数表](https://www.kdocs.cn/l/cocUhcII8C6N) 查看, 考虑到权限控制关闭编辑申请权, 请通过评论的方式像我们提供您的名称与定数

# 潜力值

考虑到游戏曲库较少, 使用 b30 + b10 算法而非 b50 + b10, 且没有 Dive Cleared 奖励, Perfect Dive 奖励 0.1 分

单曲潜力值计算:

- 分数 < 95M: 定数 * ((分数 / 95000000) ^ 10)
- 分数 < 98M: 定数 + ((分数 - 95000000) / 3000000)
- 分数 < 100M: 定数 + 1 + ((分数 - 98000000) / 2000000)
- 分数 > 100M: 定数 + 2.1

个人潜力值计算: (b30 + b10) / 40

# 疑难解答

- 潜力值悬浮窗仅在聚焦游戏窗口时显示, 且检测到开始游戏后会自动隐藏(或是鼠标移到悬浮窗位置时手动隐藏)
- 如果启动游戏后潜力值悬浮窗后日志一直显示 `[GameSceneMonitor] infalsus.exe not found`, 且聚焦游戏窗口时未看到潜力值悬浮窗, 请尝试使用管理员权限运行

# 反馈与建议

请通过 [Issues](https://github.com/Anling20/InFalsusTools/issues) 或在 [QQ群](https://qm.qq.com/q/ndmzU21OyA) 为我们提供反馈与建议

# 其他页面

[潜力值函数](https://www.desmos.com/calculator/fbyae8c2un)<br>
[UI 设计稿](https://mastergo.com/goto/WONSik65?page_id=M&file=204973649365725)

# 项目架构

[Kotlin](https://kotlinlang.org/)<br>
现代化简洁多平台编程语言

[Gradle](https://gradle.org/)<br>
深度可定制版本管理及构建工具

[Compose Multiplatform](https://kotlinlang.org/compose-multiplatform/)<br>
多平台声明式 UI 框架

[clikt](https://github.com/ajalt/clikt)<br>
Kotlin Multiplatform 命令行参数解析

[haze](https://github.com/chrisbanes/haze)<br>
Compose Multiplatform 背景模糊

[multiplatform-settings](https://github.com/russhwolf/multiplatform-settings)<br>
Kotlin Multiplatform 配置保存读取

[FileKit](https://github.com/vinceglb/FileKit)<br>
Kotlin/Compose Multiplatform 文件操作(读写, 选取/保存)

[github-workflows-kt](https://github.com/typesafegithub/github-workflows-kt)<br>
Kotlin Script GitHub Actions 工作流文件生成

# 特别感谢

[REDDRAGON-HL/InFalsus-SaveData-Parser](https://github.com/REDDRAGON-HL/InFalsus-SaveData-Parser)<br>
提供存档文件解析(由 ChatGPT 将 Python 转换为 Kotlin)

[ATres001/InFalsusRating](https://github.com/ATres001/InFalsusRating)<br>
提供检测当前游戏场景思路(由 ChatGPT 生成内存读取及查找内存地址)