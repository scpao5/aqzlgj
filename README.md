# 暗区指令工具

> [!IMPORTANT]
> **本仓库已停止维护，项目已迁移至 [github.com/scpao5/aqzlgj-kotlin](https://github.com/scpao5/aqzlgj-kotlin)**
> 新版采用 Kotlin + Compose 重写，包含悬浮窗、分类搜索、主题适配等完整功能，请前往新仓库获取最新版本。



[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Android](https://img.shields.io/badge/Android-7.0%2B-brightgreen)](https://developer.android.com/about/versions/android-7.0)
[![Release](https://img.shields.io/github/v/release/scpao5/aqzlgj)](https://github.com/scpao5/aqzlgj/releases)

**暗区指令工具** 是一款为《暗区突围》开发的辅助工具，提供悬浮窗指令分类、搜索、一键复制和执行功能。支持 Root 和普通广播两种执行模式，自动适配用户环境。

## 📱 功能特性

- ✅ **六大分类指令**：刀皮类、战术装备、钥匙类、针剂类、操作指令、大杂烩（2万+条指令）
- 🔍 **全局搜索**：按名称或指令内容快速定位指令
- 📋 **一键复制**：点击复制指令文本
- ⚡ **一键执行**：支持 Root（`su -c`）和普通广播两种模式，自动适配
- 🪟 **游戏内悬浮窗**：无需切换应用，直接在游戏中操作

## 📥 下载安装

### 预编译 APK（推荐）
从 [Releases](https://github.com/scpao5/aqzlgj/releases) 页面下载最新版 `.apk` 文件，直接安装即可。

### 从源码编译
```bash
git clone https://github.com/scpao5/aqzlgj.git
cd aqzlgj
# 使用 Android Studio 或 AIDE 打开项目
# 构建 Debug/Release APK
```

🚀 使用前准备

必须条件

· Android 7.0 (API 24) 或更高版本
· 悬浮窗权限（首次启动自动申请）

执行模式说明

模式 条件 说明
Root 模式 设备已 Root 优先使用 su -c am broadcast 执行指令
普通广播模式 无 Root 直接通过 sendBroadcast 发送指令

游戏端需实现 android.intent.action.RUN 广播接收器，解析 cmd 参数执行对应指令。

🎮 使用方法

1. 打开应用，点击「开启悬浮」按钮启动悬浮窗。
2. 悬浮窗展开后，点击分类标签查看对应指令列表。
3. 点击指令卡片即可执行（或复制）。
4. 搜索框输入关键词快速过滤指令。

📁 数据文件

应用内置指令数据存放于 assets/*.txt 文件中，格式为：

```
指令名称|指令内容
```

支持通过修改这些文件自定义指令（需重新编译 APK）。

当前已包含分类：

· 刀皮类.txt
· 战术装备含食物.txt
· 钥匙类.txt
· 针剂类.txt
· 操作指令.txt
· 大杂烩.txt（2万+行）

⚠️ 注意事项

· 执行指令需游戏端存在对应的广播接收器。
· 本工具仅用于学习和辅助目的，请遵守游戏相关规则。

🤝 贡献

欢迎提交 Issue 和 Pull Request。请确保代码符合 GPL-3.0 许可证规范。

📄 许可证

本项目采用 GNU General Public License v3.0 开源协议。

👤 作者

· scpao5（是白白吖）
· QQ：771217201
· 项目地址：https://github.com/scpao5/aqzlgj

---

如果觉得这个工具有帮助，别忘了给个 Star ⭐ 支持一下！