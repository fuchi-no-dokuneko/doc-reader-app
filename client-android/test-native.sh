#!/usr/bin/env bash
set -euo pipefail
app_dir="$(cd -- "$(dirname -- "$0")" && pwd)"
tool_dir="$app_dir/.local-tool-app"
export ANDROID_HOME="$tool_dir/runtime-sdk" ANDROID_SDK_ROOT="$tool_dir/runtime-sdk"
export ANDROID_USER_HOME="$tool_dir/android-user" ANDROID_EMULATOR_HOME="$tool_dir/android-user"
export ANDROID_AVD_HOME="$tool_dir/avd"
export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:-} -Djava.net.preferIPv4Stack=true"
evidence="$app_dir/../fan-out/native"
mkdir -p "$evidence" "$ANDROID_AVD_HOME" "$ANDROID_USER_HOME"
adb="$ANDROID_HOME/platform-tools/adb"
serial="${DOC_READER_NATIVE_SERIAL:-emulator-5580}"
emulator_pid=''
cleanup() {
  if [ -n "$emulator_pid" ]; then kill "$emulator_pid" 2>/dev/null || true; fi
}
trap cleanup EXIT
if [ -z "${DOC_READER_NATIVE_SERIAL:-}" ]; then
  "$app_dir/install-runtime.sh"
  if "$adb" -s "$serial" get-state >/dev/null 2>&1; then
    echo 'Dedicated emulator port is in use; select an existing test emulator explicitly.' >&2
    exit 1
  fi
  if [ ! -f "$ANDROID_AVD_HOME/docreader-api28.ini" ]; then
    "$ANDROID_HOME/cmdline-tools/latest/bin/avdmanager" create avd \
      -n docreader-api28 -k 'system-images;android-28;default;x86_64' \
      -p "$ANDROID_AVD_HOME/docreader-api28.avd" -d pixel_2 <<< no
  fi
  acceleration=off
  if [ -r /dev/kvm ] && [ -w /dev/kvm ]; then acceleration=on; fi
  "$ANDROID_HOME/emulator/emulator" -avd docreader-api28 -port 5580 -accel "$acceleration" \
    -no-window -no-audio -no-boot-anim -no-snapshot -no-metrics -gpu swiftshader_indirect \
    -feature -Vulkan -memory 1536 -cores 2 -dns-server 8.8.8.8 > "$evidence/emulator.log" 2>&1 &
  emulator_pid=$!
fi
for attempt in {1..240}; do
  if [ "$(timeout 4 "$adb" -s "$serial" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = 1 ]; then break; fi
  if [ "$attempt" = 240 ]; then echo 'Android did not boot.' >&2; exit 1; fi
  sleep 2
done
"$adb" -s "$serial" shell input keyevent KEYCODE_WAKEUP
"$adb" -s "$serial" shell wm dismiss-keyguard
"$adb" -s "$serial" install -r "$app_dir/build/outputs/apk/debug/client-android-debug.apk"
"$adb" -s "$serial" install -r "$app_dir/build/outputs/apk/androidTest/debug/client-android-debug-androidTest.apk"
timeout 300 "$adb" -s "$serial" shell am instrument -w -r \
  app.docreader.test/androidx.test.runner.AndroidJUnitRunner | tee "$evidence/instrumentation.txt"
grep -Eq '^OK \([1-9][0-9]* tests?\)' "$evidence/instrumentation.txt"
for fixture in empty styled; do
  "$adb" -s "$serial" exec-out run-as app.docreader cat "files/$fixture.png" > "$evidence/$fixture.png"
done
