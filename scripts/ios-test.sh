#!/usr/bin/env bash
# Syncs the project to the build Mac and runs the iOS UI tests on a simulator.
#   scripts/ios-test.sh                       # all tests on "iPhone 17"
#   scripts/ios-test.sh "iPhone 17" testName  # one test
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."
DEVICE="${1:-iPhone 17}"
ONLY="${2:-}"

scripts/mac.sh env DEVICE="$DEVICE" ONLY="$ONLY" bash -c '
  set -o pipefail
  cd iosApp
  xcodegen generate --quiet
  mkdir -p build
  args=()
  [ -n "$ONLY" ] && args+=("-only-testing:ElapsyUITests/ElapsyUITests/$ONLY")
  # Without "-collect-test-diagnostics never" xcodebuild runs "simctl diagnose" after the tests,
  # which keeps it busy for up to ten more minutes. The full log stays in build/test.log.
  xcodebuild test -project Elapsy.xcodeproj -scheme Elapsy \
    -destination "platform=iOS Simulator,name=$DEVICE" -derivedDataPath build/DerivedData \
    -collect-test-diagnostics never \
    "${args[@]}" > build/test.log 2>&1 || true
  grep -E "error:|Test Case .*(passed|failed)|XCTAssert|failed -|Executed [0-9]+ tests|\*\* TEST" build/test.log \
    | sed -E "s/^.*Test Case/Test Case/" | awk "!seen[\$0]++" | head -80
'
