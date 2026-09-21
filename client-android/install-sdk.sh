#!/usr/bin/env bash
set -euo pipefail
app_dir="$(cd -- "$(dirname -- "$0")" && pwd)"
tool_dir="$app_dir/.local-tool-app"
sdk_dir="$tool_dir/android-sdk"
mkdir -p "$sdk_dir/cmdline-tools"
if [ ! -x "$sdk_dir/cmdline-tools/19.0/bin/sdkmanager" ]; then
  curl -4 -fL --connect-timeout 20 --max-time 600 \
    https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip \
    -o "$tool_dir/android-tools.zip"
  unzip -q "$tool_dir/android-tools.zip" -d "$tool_dir/android-unpack"
  mv "$tool_dir/android-unpack/cmdline-tools" "$sdk_dir/cmdline-tools/19.0"
  rmdir "$tool_dir/android-unpack"
  rm "$tool_dir/android-tools.zip"
fi
export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:-} -Djava.net.preferIPv4Stack=true"
export ANDROID_USER_HOME="$tool_dir/android-user"
set +o pipefail
yes | "$sdk_dir/cmdline-tools/19.0/bin/sdkmanager" --sdk_root="$sdk_dir" --licenses >/dev/null
set -o pipefail
"$sdk_dir/cmdline-tools/19.0/bin/sdkmanager" --sdk_root="$sdk_dir" 'platforms;android-36' 'build-tools;36.0.0'
