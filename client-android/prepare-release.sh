#!/usr/bin/env bash
set -euo pipefail
mkdir -p release
apk=(client-android/build/outputs/apk/release/*.apk)
bundle=(client-android/build/outputs/bundle/release/*.aab)
test "${#apk[@]}" -eq 1
test "${#bundle[@]}" -eq 1
version="${GITHUB_REF_NAME//\//-}"
cp "${apk[0]}" "release/doc-reader-${version}-signed.apk"
cp "${bundle[0]}" "release/doc-reader-${version}-signed.aab"
(cd release && sha256sum ./*.apk ./*.aab > SHA256SUMS.txt)
