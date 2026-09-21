#!/usr/bin/env bash
set -euo pipefail
repo_dir="$(cd -- "$(dirname -- "$0")" && pwd)"
mkdir -p "$repo_dir/.local-tool"
if ! command -v javac >/dev/null && [ ! -x "${JAVA_HOME:-}/bin/javac" ]; then
  tool_dir="$repo_dir/.local-tool"
  if [ ! -x "$tool_dir/jdk/bin/javac" ]; then
    curl -4 -fL --connect-timeout 20 --max-time 600 \
      'https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.7%2B6/OpenJDK21U-jdk_x64_linux_hotspot_21.0.7_6.tar.gz' \
      -o "$tool_dir/jdk.tar.gz"
    mkdir -p "$tool_dir/jdk"
    tar -xzf "$tool_dir/jdk.tar.gz" -C "$tool_dir/jdk" --strip-components=1
    rm "$tool_dir/jdk.tar.gz"
  fi
  export JAVA_HOME="$tool_dir/jdk"
  export PATH="$JAVA_HOME/bin:$PATH"
fi
exec "$repo_dir/client-android/install-local-app-build.sh"
