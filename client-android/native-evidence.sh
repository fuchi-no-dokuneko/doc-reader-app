#!/usr/bin/env bash
set -u
adb="$1"; serial="$2"; evidence="$3"
timeout 8 "$adb" -s "$serial" logcat -d -s NativeUi:I TestRunner:E AndroidRuntime:E '*:S' > "$evidence/logcat.txt"
timeout 8 "$adb" -s "$serial" exec-out screencap -p > "$evidence/final-screen.png"
for fixture in empty styled; do
  timeout 8 "$adb" -s "$serial" exec-out run-as app.docreader cat "files/$fixture.png" > "$evidence/$fixture.png"
done
