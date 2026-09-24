# Build notes · 建置說明 · 构建说明

Pinned build tools: Gradle 9.5.1, Android Gradle Plugin 9.2.0, SDK 36,
build-tools 36.0.0; Kotlin 2.3.20, KSP 2.3.10; Java 21 on this PC. App-specific installations and caches
live in `client-android/.local-tool-app`; shared fallback Java lives in
`.local-tool`. Setup uses IPv4 and never invokes sudo.

固定建置工具：Gradle 9.5.1、AGP 9.2.0、SDK 36、build-tools 36.0.0；本機使用 Java 21。
應用程式工具與快取置於 `.local-tool-app`，共用 Java 備援置於根目錄 `.local-tool`。
安裝使用 IPv4，不會呼叫 sudo。

固定构建工具：Gradle 9.5.1、AGP 9.2.0、SDK 36、build-tools 36.0.0；本机使用 Java 21。
应用工具与缓存放在 `.local-tool-app`，共用 Java 备用工具放在根目录 `.local-tool`。
安装使用 IPv4，不会调用 sudo。

```bash
./install-local-build.sh
./gradlew :client-android:assembleDebug :client-android:testDebugUnitTest :client-android:lintDebug
```

The Ubuntu system-package script is optional and must be reviewed and run
manually with sudo by the administrator. It has not been executed here.
No emulator or adb is required. JUnit and Robolectric run on the host JVM.
Robolectric reuses a local SDK jar when available; otherwise its Maven cache
is inside `.local-tool-app/maven`. PDFium test fixtures retain their licenses.

Ubuntu 系統套件腳本為選用，須由管理員檢閱後手動以 sudo 執行，本次未執行。
以上指令不需模擬器或 adb。

Ubuntu 系统软件包脚本为可选项，须由管理员检查后手动用 sudo 执行，本次未执行。
以上命令不需要模拟器或 adb。

References · 參考 · 参考:

- [Android build compatibility](https://developer.android.com/build/releases/agp-9-2-0-release-notes)
- [Document access](https://developer.android.com/training/data-storage/shared/documents-files)
- [External intents](https://developer.android.com/training/basics/intents/filters)
- [PDF rendering](https://developer.android.com/reference/android/graphics/pdf/PdfRenderer)
