# Sudoku You

Sudoku You is a free and open-source Sudoku app for Android, focused on offline play, careful reasoning, and human-style logical hints.

The app works without network access, advertising, accounts, analytics, or tracking. Games, settings, statistics, and crash reports are stored locally.

## Features

- Generate classic 9×9 puzzles locally with a unique solution.
- Easy, Medium, Hard, and Brutal difficulty levels.
- Import custom puzzles with format and unique-solution validation.
- Automatic game saves, recent/completed game history, and per-difficulty statistics.
- Pencil marks, automatic candidates, undo, redo, erase, and restart.
- Step-by-step logical hints with explanations and board markings.
- Basic and advanced techniques, including subsets, fish, wings, coloring, chains, unique rectangles, AIC, and forcing chains.
- Advanced annotations for cells and candidates, including colors, frames, and solid or dashed links.
- Import and export using Susser, multiline, pencilmark, Sukaku, Excel/TSV, OpenSudoku, and HoDoKu formats.
- Material 3 interface with dynamic colors, custom accents, multiple palettes, light/dark modes, AMOLED black, and board highlighting.
- English and Simplified Chinese, with an in-app language selector.
- Local-only crash history and an in-app third-party license viewer.

Sudoku You requires Android 12 (API 31) or later.

## Screenshots

<p align="center">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="280" alt="Sudoku You home screen">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="280" alt="Sudoku You game screen">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" width="280" alt="Sudoku You logical hint screen">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" width="280" alt="Sudoku You about screen">
</p>

## Build from source

Prerequisites:

- JDK 17
- Android SDK Platform 37
- Android SDK Build Tools

On Linux or macOS:

```shell
./gradlew assembleDebug
./gradlew test
```

On Windows:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat test
```

Release builds are intentionally unsigned when built from the repository. Keep release signing keys and credentials outside version control.

## Privacy

Sudoku You does not request Android's network permission and does not automatically upload crash reports or other app data. See [PRIVACY.md](PRIVACY.md) for details.

## Contributing

Bug reports and contributions are welcome. See [CONTRIBUTING.md](CONTRIBUTING.md) before submitting a change.

## License

Sudoku You is licensed under the [GNU General Public License v3.0 or later](LICENSE). Third-party notices are listed in [NOTICE](NOTICE).

Copyright © 2026 Galaxy-rio.

---

## 简体中文

Sudoku You 是一款自由开源的 Android 数独应用，专注于完全离线游玩、认真推理和拟人化逐步提示。

应用不申请网络权限，不包含广告、账户、分析或追踪功能。游戏、设置、统计和崩溃报告均保存在本地。

### 主要功能

- 在本机生成保证唯一解的经典 9×9 数独。
- 简单、中等、困难和残酷四种难度。
- 导入自定义题目，并检查格式和唯一解。
- 自动保存、最近/已完成对局历史，以及按难度统计。
- 候选数、自动候选数、撤销、重做、擦除与重新开始。
- 带文字解释和盘面标记的逐步逻辑提示。
- 支持数集、鱼、翼、染色、链、唯一矩形、AIC 和强制链等进阶技巧。
- 支持格子/候选数着色、框选以及实线或虚线链接等高级标注。
- 支持 Susser、多行文本、铅笔候选盘、Sukaku、Excel/TSV、OpenSudoku 和 HoDoKu 格式的导入导出。
- Material 3 界面，支持动态配色、自定义强调色、多种调色板、浅色/深色/AMOLED 模式和盘面高亮。
- 英语和简体中文，可在应用内单独选择语言。
- 本地崩溃历史和第三方开源许可证查看器。

Sudoku You 需要 Android 12（API 31）或更高版本。构建方式、隐私说明和许可证请参阅上方对应章节。
