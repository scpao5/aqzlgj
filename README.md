# 暗区指令工具

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Android](https://img.shields.io/badge/Android-7.0%2B-brightgreen)](https://developer.android.com/about/versions/android-7.0)
[![Release](https://img.shields.io/github/v/release/scpao5/aqzlgj)](https://github.com/scpao5/aqzlgj/releases)

**暗区指令工具** 是一款为《暗区突围》设计的辅助工具，提供便捷的指令分类、搜索、一键复制和执行功能。通过集成 Shizuku 框架，应用可获得 shell 权限，向游戏发送广播指令，实现快速调用。

## 📱 功能特性

- ✅ **五大分类指令**：刀皮类、战术装备、钥匙类、针剂类、操作指令，支持动态编辑
- 🔍 **全局搜索**：按名称或指令内容快速定位
- 📋 **一键复制**：点击复制指令文本，供手动使用
- ⚡ **一键执行**（需提权）：通过 Shizuku + rish 发送广播，自动执行指令
- 🛠️ **自定义 Shell 命令**：在设置中可执行任意 shell 命令（高级用户）
- 🔐 **权限检测**：实时显示 Shizuku 和 Root 状态，支持重新提权

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
· 已安装并启动 Shizuku 应用（下载地址）

启动 Shizuku 服务（二选一）

· 无线调试（推荐）：手机开启开发者选项 → 无线调试 → 配对码 → Shizuku 内启动
· Root 设备：直接在 Shizuku 内点击启动

启动后，暗区指令工具会自动检测并请求授权，授权成功即可使用执行功能。

🎮 使用方法

1. 打开应用，点击底部分类按钮查看对应指令列表。
2. 每个指令卡片包含 📋 复制 和 ▶ 执行 按钮。
3. 点击搜索框，输入关键词（指令名称或内容）快速过滤。
4. 如需执行自定义命令，进入 设置 → 执行 Shell 命令。

📁 数据文件

应用内置指令数据，存放于 assets/*.txt 文件中，格式为 名称|指令。您可以通过修改这些文件自定义指令（需要重新编译 APK）。

⚠️ 注意事项

· 执行指令依赖 Shizuku 服务，请确保其已启动并授权。
· 如果提权失败，请检查 Shizuku 是否运行，并在设置中手动“重新部署 Rish”。
· 本工具仅用于学习和辅助目的，请遵守游戏相关规则。

🤝 贡献

欢迎提交 Issue 和 Pull Request。请确保代码符合 GPL-3.0 许可证规范。

📄 许可证

本项目采用 GNU General Public License v3.0 开源协议。

👤 作者

· scpao5 - 初始开发
· 项目地址：https://github.com/scpao5/aqzlgj

---

如果觉得这个工具有帮助，别忘了给个 Star ⭐ 支持一下！