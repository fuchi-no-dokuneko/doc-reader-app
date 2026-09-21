# Doc Reader · 文件閱讀器 · 文件阅读器

An offline Android reader for PDF, TXT, and basic Markdown. Open documents
inside the app, tap them in a file manager, or share them to Doc Reader.
Android 8.0 and newer. No account or network permission.

離線 Android 閱讀器，支援 PDF、TXT 與基本 Markdown。可在應用程式內選取文件，
或從檔案管理員點選／分享至 Doc Reader。支援 Android 8.0 以上，無須帳號或網路權限。

离线 Android 阅读器，支持 PDF、TXT 与基本 Markdown。可在应用内选择文件，
或从文件管理器点击／分享至 Doc Reader。支持 Android 8.0 以上，无需账号或网络权限。

## Build · 建置 · 构建

```bash
./install-local-build.sh
./gradlew :client-android:assembleDebug :client-android:testDebugUnitTest
```

APK: `client-android/build/outputs/apk/debug/client-android-debug.apk`

Setup reuses installed tools. Missing tools and build caches stay in the repo.
The administrative Ubuntu installer is provided for manual use; it is not run
by local setup. See [build notes](client-android/doc/build.md).

優先使用現有工具；缺少的工具與建置快取留在專案內。Ubuntu 管理員安裝腳本僅供手動執行。

优先使用现有工具；缺少的工具与构建缓存留在项目内。Ubuntu 管理员安装脚本仅供手动执行。

## Reading · 閱讀 · 阅读

PDF: page navigation, page jump, pinch/double-tap zoom. Text: selection, search,
font size. Both: remembered position and system/light/dark appearance.
The 12 most recent files are copied into private app storage for offline use.
Removing a reading copy leaves the original unchanged.

PDF 支援換頁、跳頁與縮放；文字支援選取、搜尋與字級調整。兩者均記憶閱讀位置，
並支援系統／淺色／深色外觀。最近 12 份文件保存於應用程式私人空間，移除副本不影響原檔。

PDF 支持翻页、跳页与缩放；文字支持选择、搜索与字号调整。两者均记忆阅读位置，
并支持系统／浅色／深色外观。最近 12 份文件保存在应用私有空间，移除副本不影响原文件。

Limits: PDF 50 MB; UTF-8/UTF-16 text 4 MB. See `todo.txt` for format limits.

限制：PDF 50 MB；UTF-8／UTF-16 文字 4 MB。格式限制請見 `todo.txt`。

限制：PDF 50 MB；UTF-8／UTF-16 文本 4 MB。格式限制请见 `todo.txt`。
