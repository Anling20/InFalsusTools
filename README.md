# 介绍

`In Falsus Tools` 是一个涵盖潜力值悬浮窗, B30/AHV 图片生成, 游玩记录且含有社区定数的非插件式辅助工具

# 快速开始

从 [Releases](https://github.com/Anling20/InFalsusTools/releases) 下载最新自动构建包<br>

## 分发类型

如果你不知道你在使用什么操作系统, 请选择 {os} 处为 win 且 {arch} 处为 x64 的文件<br>
如果你在使用 Windows 且不清楚你的处理器架构, 请选择 {arch} 处为 x64 的文件<br>
如果你在使用 MacOS 且不清楚你的处理器架构, 请选择 {arch} 处为 arm64 的文件<br>
如果你在使用 Linux 且不清楚你的处理器架构, 你应该先学会用 Windows

- in-falsus-tools-{version}-{os}-{arch}.exe: 使用 Electron Builder 分发的安装程序
- in-falsus-tools-{version}-{os}-{arch}-nsis.exe: 标准的 Windows 安装程序
- in-falsus-tools-{version}-{os}-{arch}.msi: 用于企业部署的 Windows 安装程序
- in-falsus-tools-{version}-{os}-{arch}-portable.exe: 无需安装的 Windows 单可执行文件
- in-falsus-tools-{version}-{os}-{arch}.dmg: macOS 安装程序
- in-falsus-tools-{version}-{os}-{arch}.deb: 适用于 Debian 系 Linux 发行版的安装程序
- in-falsus-tools-{version}-{os}-{arch}.rpm: 适用于 Red Hat Package Manager 的 Linux 安装程序
- in-falsus-tools-{version}-{os}-{arch}.pacman: 适用于 Arch Linux 系 Linux 发行版的安装程序
- in-falsus-tools-{version}-{os}-{arch}.AppImage/appimage: 无需安装的 Linux 单可执行文件
- in-falsus-tools-{version}-{os}-{arch}.zip: 无需安装的解压式文件
- in-falsus-tools-{version}-{os}-{arch}.tar.gz: 无需安装的解压式文件
- in-falsus-tools-{version}-{os}-{arch}.7z: 无需安装的解压式文件

# 社区定数

由于游戏内不提供官方细分定数, 通过采集社区数据取平均值(7舍8入)的方式设定一套定数<br>
若要在 `In Falsus Tools` 内使用社区定数, 请前往设置页面修改 `定数表`<br>

若有需要查看, 请前往 [在线定数表](https://www.kdocs.cn/l/cheDPToKxD5B)<br>
若需要提供数据, 请前往 [收集表](https://f.wps.cn/g/WFFMWvS5)

# 潜力值算法

## In Falsus Tools

考虑到游戏曲库较少, 使用 b30 算法而非 b50, 且没有 Dive Cleared 奖励, Perfect Dive 奖励 0.1 分

单曲潜力值计算:

- 分数 < 95M: 定数 * ((分数 / 95000000) ^ 10)
- 分数 < 98M: 定数 + ((分数 - 95000000) / 3000000)
- 分数 < 100M: 定数 + 1 + ((分数 - 98000000) / 2000000)
- 分数 > 100M: 定数 + 2.1

个人潜力值计算: (b30 + r10) / 40

- 当使用 `In Falsus Tools` 算法时, r10 视为 b10
- 当使用 `In Falsus Tools(Recent 10)` 算法时, r10 算法与 `Arcaea Classic` 一致

## Arcaea

该算法为游戏 [Arcaea](https://arcaea.lowiro.com) 所使用的潜力值计算法<br>
请参考 [计算方法](https://wiki.arcaea.cn/index.php?title=%E6%BD%9C%E5%8A%9B%E5%80%BC#%E8%AE%A1%E7%AE%97%E6%96%B9%E6%B3%95)

## Arcaea Classic

该算法为游戏 [Arcaea](https://arcaea.lowiro.com) 版本 7.0.0 前所使用的潜力值计算法<br>
请参考 [计算方法](https://wiki.arcaea.cn/index.php?title=%E6%BD%9C%E5%8A%9B%E5%80%BC&oldid=88386#%E8%AE%A1%E7%AE%97%E6%96%B9%E6%B3%95)

- 当使用 `Arcaea Classic(Max)` 算法时, r10 视为 b10
- 当使用 `Arcaea Classic(Min)` 算法时, r10 视为空集合

# 疑难解答

- 潜力值悬浮窗仅在聚焦游戏窗口时显示, 且检测到开始游戏后会自动隐藏(或是鼠标移到悬浮窗位置时手动隐藏)
- 如果启动游戏后潜力值悬浮窗后聚焦游戏窗口时未看到潜力值悬浮窗, 请尝试使用管理员权限运行
- 如果游戏更新后潜力值悬浮窗在进行游戏时不会自动隐藏且结算页面不显示游玩数据, 请检查 `地址文件` 是否与游戏版本匹配
- 如果使用微星小飞机 RTSS 时遇到页面卡死, 请关闭 RTSS 后重新启动软件

# 地址文件

潜力值悬浮窗的自动隐藏及结算状态通过读取游戏内存实现, 软件包内会默认携带 `已兼容的` 最新版本地址文件<br>
如需历史版本, 请前往 [此处](./address)

# 反馈与建议

请通过 [Issues](https://github.com/Anling20/InFalsusTools/issues) 或在 [QQ群](https://qm.qq.com/q/ndmzU21OyA) 为我们提供反馈与建议

# 其他页面

[单曲潜力值函数(In Falsus Tools)](https://www.desmos.com/calculator/fbyae8c2un)

[UI 设计稿](https://mastergo.com/goto/WONSik65?page_id=M&file=204973649365725)

# 项目架构

[Kotlin](https://kotlinlang.org/)<br>
现代化简洁多平台编程语言

[Gradle](https://gradle.org/)<br>
深度可定制版本管理及构建工具

[Compose Multiplatform](https://kotlinlang.org/compose-multiplatform/)<br>
Kotlin Multiplatform 声明式 UI 框架

[Nucleus](https://github.com/NucleusFramework/Nucleus)<br>
Compose Desktop 框架, 提供基于 Rust Tao 的窗口层, 使用 Electron Builder 的丰富打包格式, 自动更新及操作系统集成

[kotlinx-serialization](https://github.com/kotlin/kotlinx.serialization)<br>
Kotlin Multiplatform 序列化, 用于 JSON 文件解析

[haze](https://github.com/chrisbanes/haze)<br>
Compose Multiplatform 背景模糊

[multiplatform-settings](https://github.com/russhwolf/multiplatform-settings)<br>
Kotlin Multiplatform 配置保存读取

[FileKit](https://github.com/vinceglb/FileKit)<br>
Kotlin/Compose Multiplatform 文件操作(读写, 选取/保存)

[github-workflows-kt](https://github.com/typesafegithub/github-workflows-kt)<br>
Kotlin Scripting GitHub Actions 工作流文件生成

# 特别感谢

[REDDRAGON-HL/InFalsus-SaveData-Parser](https://github.com/REDDRAGON-HL/InFalsus-SaveData-Parser)<br>
提供存档文件解析(由 ChatGPT 将 Python 转换为 Kotlin)

[ATres001/InFalsusRating](https://github.com/ATres001/InFalsusRating)<br>
提供检测当前游戏场景思路(由 ChatGPT 生成内存读取及查找内存地址)