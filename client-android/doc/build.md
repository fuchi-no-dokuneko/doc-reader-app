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
Host test classes use separate JVMs to isolate Android/DataStore static state.
Robolectric reuses a local SDK jar when available; otherwise its Maven cache
is inside `.local-tool-app/maven`. PDFium test fixtures retain their licenses.

Ubuntu 系統套件腳本為選用，須由管理員檢閱後手動以 sudo 執行，本次未執行。
以上指令不需模擬器或 adb。

Ubuntu 系统软件包脚本为可选项，须由管理员检查后手动用 sudo 执行，本次未执行。
以上命令不需要模拟器或 adb。

Native release gate · 原生發布驗證 · 原生发布验证:

```bash
./gradlew :client-android:assembleDebug :client-android:assembleDebugAndroidTest
./client-android/test-native.sh
```

The gate creates a dedicated Android 9 emulator without sudo and saves logs/screenshots
in `fan-out/native`. Set `DOC_READER_NATIVE_SERIAL=emulator-5580` to reuse that test emulator.
CI and releases require the picker, CSS/HTML, external intent and Room migration checks.
發布驗證使用專用 Android 9 模擬器，保存紀錄與畫面；CI 及發布均須通過。
发布验证使用专用 Android 9 模拟器，保存记录与画面；CI 及发布均须通过。

References · 參考 · 参考:

- [Android build compatibility](https://developer.android.com/build/releases/agp-9-2-0-release-notes)
- [Document access](https://developer.android.com/training/data-storage/shared/documents-files)
- [External intents](https://developer.android.com/training/basics/intents/filters)
- [PDF rendering](https://developer.android.com/reference/android/graphics/pdf/PdfRenderer)
