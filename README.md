# Doc Reader · 文件閱讀器 · 文件阅读器

Offline Android reader, Android 8.0+. Kotlin MVVM, Room and DataStore.
No account or network permission. Open files in the app or from a file manager.

[Project page](https://fuchi-no-dokuneko.github.io/doc-reader-app/) ·
[Releases](https://github.com/fuchi-no-dokuneko/doc-reader-app/releases)

離線 Android 閱讀器，支援 Android 8.0 以上；採用 Kotlin MVVM、Room、DataStore。
無須帳號或網路權限，可從應用程式或檔案管理員開啟文件。

离线 Android 阅读器，支持 Android 8.0 以上；采用 Kotlin MVVM、Room、DataStore。
无需账号或网络权限，可从应用或文件管理器打开文档。

## Formats · 格式 · 格式

PDF (passwords, selection, search, link previews), EPUB with CSS,
Markdown, Jupyter `.ipynb` / `.ipybn`, DOC/DOCX, XLS/XLSX, PPT/PPTX,
RTF, CSV, JSON/JSONL, YAML, `.config`, dotfiles and common source languages.
Office files use a content reading view. Notebooks show cells and saved outputs.

[Edit source · 編輯程式檔 · 编辑源文件](client-android/doc/editing.md): live syntax colors.

支援上述格式、中文編碼偵測與手動選擇。Office 以內容閱讀檢視呈現；
筆記本顯示儲存格與已保存輸出，程式碼支援上色及結構折疊。

支持上述格式、中文编码检测与手动选择。Office 以内容阅读视图呈现；
笔记本显示单元格与已保存输出，代码支持着色及结构折叠。

## Reading · 閱讀 · 阅读

Independent tabs, split panes, scroll/paged/precision modes, page jumps,
light/dark/sepia themes, typography, chapters, bookmarks and highlights.
Long-press a PDF internal link for a preview. Tap a source structure to fold it.
Select Reading → Linked images to grant access to a Markdown image folder.

獨立分頁、左右分割、三種閱讀模式、跳頁、主題、排版、章節、書籤與標記。
長按 PDF 內部連結預覽；點選程式結構折疊。閱讀選單可授權連結圖片資料夾。

独立分页、左右分割、三种阅读模式、跳页、主题、排版、章节、书签与标记。
长按 PDF 内部链接预览；点击代码结构折叠。阅读菜单可授权链接图片文件夹。

## Build · 建置 · 构建

```bash
./install-local-build.sh
./gradlew :client-android:assembleDebug :client-android:testDebugUnitTest :client-android:lintDebug
```

APK: `client-android/build/outputs/apk/debug/client-android-debug.apk`

Tools/caches stay local to the repo; installed tools may be reused.
Host tests require no emulator or adb. See [build notes](client-android/doc/build.md)
and [verification](client-android/doc/verification.txt).

工具／快取置於專案，可沿用已安裝工具；主機測試不需模擬器或 adb。
工具／缓存放在项目中，可复用已安装工具；主机测试无需模拟器或 adb。
