#!/usr/bin/env bash
set -euo pipefail
app_dir="$(cd -- "$(dirname -- "$0")" && pwd)"
tool_dir="$app_dir/.local-tool-app"
mkdir -p "$tool_dir"
if [ ! -x "$tool_dir/gradle/bin/gradle" ]; then
  existing_gradle="$HOME/install/opt/gradle-9.5.1"
  if [ -x "$existing_gradle/bin/gradle" ]; then
    ln -s "$existing_gradle" "$tool_dir/gradle"
  else
    curl -4 -fL --connect-timeout 20 --max-time 600 \
      https://services.gradle.org/distributions/gradle-9.5.1-bin.zip -o "$tool_dir/gradle.zip"
    unzip -q "$tool_dir/gradle.zip" -d "$tool_dir"
    mv "$tool_dir/gradle-9.5.1" "$tool_dir/gradle"
    rm "$tool_dir/gradle.zip"
  fi
fi
if [ ! -d "$tool_dir/dependency-cache/modules-2" ] && [ -d "$HOME/.gradle/caches/modules-2" ]; then
  mkdir -p "$tool_dir/dependency-cache"
  cp -a --reflink=auto "$HOME/.gradle/caches/modules-2" "$tool_dir/dependency-cache/"
  rm -f "$tool_dir/dependency-cache/modules-2/modules-2.lock" "$tool_dir/dependency-cache/modules-2/gc.properties"
fi
sdk_dir="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/install/opt/android-sdk}}"
if [ -f "$sdk_dir/platforms/android-36/android.jar" ] && [ -d "$sdk_dir/build-tools/36.0.0" ]; then
  if [ ! -e "$tool_dir/android-sdk" ]; then ln -s "$sdk_dir" "$tool_dir/android-sdk"; fi
else
  "$app_dir/install-sdk.sh"
fi
echo 'Build tools ready / 建置工具已就緒'
