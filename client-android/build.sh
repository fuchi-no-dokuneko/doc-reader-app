#!/usr/bin/env bash
set -euo pipefail
app_dir="$(cd -- "$(dirname -- "$0")" && pwd)"
tool_dir="$app_dir/.local-tool-app"
if [ ! -x "$tool_dir/gradle/bin/gradle" ] || [ ! -e "$tool_dir/android-sdk" ]; then
  "$app_dir/../install-local-build.sh"
fi
if [ -x "$app_dir/../.local-tool/jdk/bin/java" ]; then
  export JAVA_HOME="$app_dir/../.local-tool/jdk"
fi
export ANDROID_HOME="$tool_dir/android-sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export ANDROID_USER_HOME="$tool_dir/android-user"
export GRADLE_USER_HOME="$tool_dir/gradle-home"
if [ -d "$tool_dir/dependency-cache/modules-2" ]; then
  export GRADLE_RO_DEP_CACHE="$tool_dir/dependency-cache"
fi
export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:-} -Djava.net.preferIPv4Stack=true"
cd "$app_dir/.."
if [ "$#" -eq 0 ]; then set -- :client-android:assembleDebug :client-android:testDebugUnitTest; fi
exec "$tool_dir/gradle/bin/gradle" --no-daemon "$@"
