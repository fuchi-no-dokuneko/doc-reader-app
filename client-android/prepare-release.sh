#!/usr/bin/env bash
set -euo pipefail
mkdir -p release
apk=(client-android/build/outputs/apk/release/*.apk)
bundle=(client-android/build/outputs/bundle/release/*.aab)
test "${#apk[@]}" -eq 1
test "${#bundle[@]}" -eq 1
version="${RELEASE_VERSION:-${GITHUB_REF_NAME//\//-}}"
if [[ ! "$version" =~ ^v[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "Release version must look like v1.2.3, got: $version" >&2
  exit 1
fi
cp "${apk[0]}" "release/doc-reader-${version}-signed.apk"
cp "${bundle[0]}" "release/doc-reader-${version}-signed.aab"
(cd release && sha256sum ./*.apk ./*.aab > SHA256SUMS.txt)
