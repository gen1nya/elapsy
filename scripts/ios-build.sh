#!/usr/bin/env bash
# Syncs the project to the build Mac, regenerates the Xcode project and builds the app for the
# simulator. Prints only errors and the final status.
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."

scripts/mac.sh bash -c '
  set -o pipefail
  cd iosApp
  xcodegen generate --quiet
  mkdir -p build
  xcodebuild -project Elapsy.xcodeproj -scheme Elapsy -configuration Debug \
    -destination "generic/platform=iOS Simulator" -derivedDataPath build/DerivedData \
    ARCHS=arm64 ONLY_ACTIVE_ARCH=NO build > build/build.log 2>&1 || true
  grep -E "error:|BUILD (SUCCEEDED|FAILED)|\*\* " build/build.log | sort -u | head -60
'
