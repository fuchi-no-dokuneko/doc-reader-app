#!/usr/bin/env bash
set -euo pipefail
app_dir="$(cd -- "$(dirname -- "$0")" && pwd)"
tool_dir="$app_dir/.local-tool-app"
sdk_dir="$tool_dir/runtime-sdk"
base_sdk="$tool_dir/android-sdk"
export ANDROID_HOME="$sdk_dir" ANDROID_SDK_ROOT="$sdk_dir"
export ANDROID_USER_HOME="$tool_dir/android-user"
export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:-} -Djava.net.preferIPv4Stack=true"
mkdir -p "$sdk_dir/cmdline-tools" "$ANDROID_USER_HOME"
if [ ! -d "$sdk_dir/cmdline-tools/latest" ]; then
  cli_dir="$base_sdk/cmdline-tools/latest"
  [ -d "$cli_dir" ] || cli_dir="$base_sdk/cmdline-tools/19.0"
  cp -a "$cli_dir" "$sdk_dir/cmdline-tools/latest"
fi
for component in emulator platform-tools; do
  if [ ! -e "$sdk_dir/$component" ] && [ -d "$base_sdk/$component" ]; then
    ln -s "$(readlink -f "$base_sdk/$component")" "$sdk_dir/$component"
  fi
done
manager="$sdk_dir/cmdline-tools/latest/bin/sdkmanager"
"$manager" --sdk_root="$sdk_dir" --licenses < <(yes) > "$tool_dir/runtime-licenses.log"
packages=()
[ -x "$sdk_dir/emulator/emulator" ] || packages+=(emulator)
[ -x "$sdk_dir/platform-tools/adb" ] || packages+=(platform-tools)
image_dir="$sdk_dir/system-images/android-28/default/x86_64"
[ -f "$image_dir/system.img" ] || packages+=('system-images;android-28;default;x86_64')
if [ "${#packages[@]}" -gt 0 ]; then
  "$manager" --sdk_root="$sdk_dir" "${packages[@]}"
fi
