#!/usr/bin/env bash
# Run manually with sudo. / 請由管理員手動使用 sudo 執行。
set -euo pipefail
if [ "$(id -u)" -ne 0 ]; then
  echo 'Run with sudo / 請使用 sudo 執行' >&2
  exit 1
fi
apt-get -o Acquire::ForceIPv4=true update
apt-get -o Acquire::ForceIPv4=true install -y curl unzip make cmake openjdk-21-jdk-headless
