#!/usr/bin/env bash
# Archives the iOS app on the build Mac and uploads it to App Store Connect (TestFlight).
# Signing needs the Mac's login keychain; pass its password in MAC_KEYCHAIN_PASSWORD.
# Bump CURRENT_PROJECT_VERSION in iosApp/project.yml before every upload.
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."
: "${MAC_KEYCHAIN_PASSWORD:?set MAC_KEYCHAIN_PASSWORD to the Mac login password}"

scripts/mac.sh env KEYCHAIN_PASSWORD="$MAC_KEYCHAIN_PASSWORD" bash -c '
  set -eo pipefail
  security unlock-keychain -p "$KEYCHAIN_PASSWORD" ~/Library/Keychains/login.keychain-db
  cd iosApp
  xcodegen generate --quiet
  rm -rf build/Elapsy.xcarchive build/export
  filter() { grep -E "error:|Error Domain|(ARCHIVE|EXPORT) (SUCCEEDED|FAILED)|Upload|\*\* " | sort -u; }
  xcodebuild archive -project Elapsy.xcodeproj -scheme Elapsy -configuration Release \
    -destination "generic/platform=iOS" -derivedDataPath build/DerivedData \
    -archivePath build/Elapsy.xcarchive -allowProvisioningUpdates 2>&1 | filter
  sed "s|<string>export</string>|<string>upload</string>|" Config/ExportOptions.plist > build/UploadOptions.plist
  xcodebuild -exportArchive -archivePath build/Elapsy.xcarchive -exportPath build/export \
    -exportOptionsPlist build/UploadOptions.plist -allowProvisioningUpdates 2>&1 | filter
'
